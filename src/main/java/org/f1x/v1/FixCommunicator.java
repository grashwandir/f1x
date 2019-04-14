/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.f1x.v1;

import org.f1x.api.FixParserException;
import org.f1x.api.FixSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.EncryptMethod;
import org.f1x.api.message.fields.FixTags;
import org.f1x.api.message.fields.MsgType;
import org.f1x.api.message.fields.SessionRejectReason;
import org.f1x.api.session.FixSession;
import org.f1x.api.session.SessionID;
import org.f1x.api.session.SessionState;
import org.f1x.api.session.SessionStatus;
import org.f1x.io.InputChannel;
import org.f1x.io.LoggingOutputChannel;
import org.f1x.io.OutputChannel;
import org.f1x.log.MessageLog;
import org.f1x.log.MessageLogFactory;
import org.f1x.log.file.LogUtils;
import org.f1x.store.EmptyMessageStore;
import org.f1x.store.MessageStore;
import org.f1x.store.SafeMessageStore;
import org.f1x.util.AsciiUtils;
import org.f1x.util.ByteArrayReference;
import org.f1x.util.RealTimeSource;
import org.f1x.util.TimeSource;
import org.f1x.util.timer.GlobalTimer;
import org.f1x.v1.schedule.SessionSchedule;
import org.f1x.v1.state.MemorySessionState;

import org.gflogger.GFLog;
import org.gflogger.GFLogEntry;
import org.gflogger.GFLogFactory;
import org.gflogger.Loggable;

import java.io.IOException;
import java.net.SocketException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.f1x.api.FixException;
import org.f1x.api.message.IEntry;
import org.f1x.api.message.IWalkable;
import org.f1x.api.message.IWalker;
import org.f1x.api.message.id.IdentifierGenerator;
import org.f1x.api.message.id.SimpleIdentifierGenerator;
import org.f1x.api.session.FixSessionListener;

/**
 * Common networking code for FIX Acceptor and FIX Initiator
 *
 * @param <M>
 * @param <B>
 * @param <T>
 */
public abstract class FixCommunicator<M extends IMessageParser, B extends MessageBuilder, T extends FixSessionListener<M, B>> implements FixSession<M, B, T>, Loggable {

    private static final int MIN_FIX_MESSAGE_LENGTH = 63; // min message example: 8=FIX.4.?|9=??|35=?|34=?|49=?|56=?|52=YYYYMMDD-HH:MM:SS|10=???|
    static final int CHECKSUM_LENGTH = 7; // length("10=123|") --check sum always expressed using 3 digits

    protected static final GFLog LOGGER = GFLogFactory.getLog(FixCommunicator.class);

    protected T eventListener;

    private final FixSettings settings;
    private MessageLogFactory messageLogFactory;
    private MessageLog messageLog;
    protected SessionState sessionState;

    // Defined during initialization
    private volatile InputChannel in;
    private volatile OutputChannel out;

    protected MessageStore messageStore;
    protected SessionSchedule schedule;

    // used by receiver thread only
    private final M parserForResend;
    private final ByteArrayReference msgTypeForResend = new ByteArrayReference();
    private final byte[] messageBufferForResend;
    private final B messageBuilderForResend;

    private final AtomicReference<SessionStatus> status = new AtomicReference<>(SessionStatus.Disconnected);

    /**
     * This flag is set when communicator is going though graceful disconnect
     * initiated by this side. We continue to process inbound requests but do
     * not make new attempts to establish socket connections. Typically inbound
     * LOGOUT in this state causes disconnect.
     */
    protected volatile boolean closeInProgress = false;

    // used by receiver thread only
//    private final DefaultM parser = new DefaultM();
    private final M parser;
    private final byte[] inboundMessageBuffer;
    private final ByteArrayReference msgType = new ByteArrayReference();
    private final byte[] beginString;

    // Used by inbound message processing thread only
    private final ByteArrayReference temporaryByteArrayReference = new ByteArrayReference();

    // Used by senders
    private final B sessionMessageBuilder;
    private final RawMessageAssembler messageAssembler;

    protected final TimeSource timeSource;
    private final AtomicReference<TimerTask> sessionMonitoringTask = new AtomicReference<>();
    private final AtomicReference<TimerTask> sessionEndTask = new AtomicReference<>();
    protected final IdentifierGenerator identifierGenerator = new SimpleIdentifierGenerator();

    private final Object sendLock = new Object();

    //==========================================================================
    //                              CONSTRUCTORS
    //==========================================================================
    protected FixCommunicator(M parser, M parserForResend, B sessionMessageBuilder, B messageBuilderForResend, FixVersion fixVersion, FixSettings settings) {
        this(parser, parserForResend, sessionMessageBuilder, messageBuilderForResend, fixVersion, settings, RealTimeSource.INSTANCE);
    }

    protected FixCommunicator(M parser, M parserForResend, B sessionMessageBuilder, B messageBuilderForResend, FixVersion fixVersion, FixSettings settings, TimeSource timeSource) {
        this.settings = settings;
        // logging message if there is a logFactory
//        this.logInboundMessages = settings.isLogInboundMessages();
//        this.logOutboundMessages = settings.isLogOutboundMessages();
//        if (logInboundMessages || logOutboundMessages)
//            messageLogFactory = new FileMessageLogFactory(settings.getLogDirectory());
//        else
//            messageLogFactory = null;
        messageAssembler = new RawMessageAssembler(fixVersion, settings.getMaxOutboundMessageSize(), settings.isSendRequiresConnect());
        inboundMessageBuffer = new byte[settings.getMaxInboundMessageSize()];
        messageBufferForResend = new byte[settings.getMaxOutboundMessageSize()];
        this.beginString = AsciiUtils.getBytes(fixVersion.getBeginString());
        this.timeSource = timeSource;
        this.parser = parser;
        this.parserForResend = parserForResend;
        this.sessionMessageBuilder = sessionMessageBuilder;
        this.messageBuilderForResend = messageBuilderForResend;
    }

    @Override
    public void setEventListener(T eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public abstract SessionID getSessionID();

    @Override
    public FixSettings getSettings() {
        return settings;
    }

    @Override
    public SessionStatus getSessionStatus() {
        return status.get();
    }

    public void setMessageLogFactory(MessageLogFactory messageLogFactory) {
        this.messageLogFactory = messageLogFactory;
    }

    public void setSessionState(SessionState sessionState) {
        this.sessionState = sessionState;
    }

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public void setSessionSchedule(SessionSchedule schedule) {
        this.schedule = schedule;
        if (schedule != null) {
            LOGGER.info().append("Session ").append(this).append(" schedule: ").append(schedule).commit();
        } else {
            LOGGER.info().append("Session ").append(this).append(" will have 'run-forever' schedule").commit();
        }
    }

    //@Deprecated // TODO: Switch to use CAS version
    protected void setSessionStatus(SessionStatus newStatus) {
        final SessionStatus oldStatus = this.status.get();
        if (oldStatus == newStatus) {
            LOGGER.warn().append(this).append(" Already in the status ").append(newStatus).commit();
        } else {
            this.status.set(newStatus);
            onSessionStatusChanged(oldStatus, newStatus);
        }
    }

    /**
     * Attempts to change state from expected current state to given one (CAS)
     *
     * @param expectedStatus
     * @param newStatus
     * @return
     */
    protected boolean setSessionStatus(SessionStatus expectedStatus, SessionStatus newStatus) {
        assert expectedStatus != newStatus;

        boolean result = this.status.compareAndSet(expectedStatus, newStatus);
        if (result) {
            onSessionStatusChanged(expectedStatus, newStatus);
        }
        return result;
    }

    protected void onSessionStatusChanged(final SessionStatus oldStatus, final SessionStatus newStatus) {
        SessionID sessionID = getSessionID();
        LOGGER.info().append("Session ").append(this).append(" changed status ").append(oldStatus).append(" => ").append(newStatus).commit();
        if (eventListener != null) {
            try {
                eventListener.onStatusChanged(sessionID, oldStatus, newStatus);
            } catch (Throwable e) {
                LOGGER.error().append(this).append(" Error notifying about status change ").append(e).commit();
            }
        }
    }

    protected final void assertSessionStatus(SessionStatus expectedStatus) {
        final SessionStatus actualStatus = getSessionStatus();
        if (actualStatus != expectedStatus) {
            throw new IllegalStateException("Expecting " + expectedStatus + " status instead of " + actualStatus);
        }
    }

    protected final void assertSessionStatus2(SessionStatus expectedStatus1, SessionStatus expectedStatus2) {
        final SessionStatus actualStatus = getSessionStatus();
        if (actualStatus != expectedStatus1 && actualStatus != expectedStatus2) {
            throw new IllegalStateException("Expecting " + expectedStatus1 + " or " + expectedStatus2 + " status instead of " + actualStatus);
        }
    }

    protected void init() {
        if (sessionState == null) {
            sessionState = new MemorySessionState();
        }

        messageStore = messageStore == null
                ? EmptyMessageStore.getInstance()
                : new SafeMessageStore(messageStore);
    }

    protected void destroy() {
    }

    //==========================================================================
    //                    STREAM PARSING
    //==========================================================================
    final IWalker walker = new IWalker() {

        StringBuilder sb = null;

        @Override
        public void onWalkBegin(final IWalkable msg) throws FixException {
            sb = new StringBuilder();
        }

        @Override
        public boolean onField(final IWalkable msg, final IEntry value) throws FixException {
            sb.append(value.getTagNum()).append("=").append(value.getString());
//                LOGGER.info(new StringBuilder("CHECK: ").append(i++).append(" ").append(sb.toString()).toString());
            return true;
        }

        @Override
        public void onWalkFinish(final IWalkable msg) throws FixException {
            sb = null;
        }
    };

    /**
     * Process inbound messages until session ends
     *
     * @param logonBuffer buffer containing session LOGON message and may be
     * some other messages or a part of them (can be null)
     * @param length actual number of bytes that should be consumed from
     * logonBuffer
     * @return
     */
    protected final boolean processInboundMessages2() {
        LOGGER.info().append(this).append("Processing FIX Session").commit();
        boolean normalExit = false;
        try {
            final MessageWalkable message = new MessageWalkable();
            final InputStreamParser streamParser = new InputStreamParser();
            while (in != null) {
                if(streamParser.next(in, message)){
                    message.walk(walker);
                } else {
                    if (closeInProgress) {
                        disconnect("No socket data"); // TODO: disconnect?
                        break;
                    }
                    throw ConnectionProblemException.NO_SOCKET_DATA;
                }
//                if (this.logger.isLoggable(Level.FINEST)) {
//                    this.logger.log(Level.FINEST, "<<<{0}", message);
//                }
//                if (eventListener != null) {
//                    eventListener.onMessage(message);
//                }
//                if (!onMessage(message)) {
//                    return;
//                }
            }
//            throw new IOException("Unexpected end of the stream. The connection might be closed by the other side.");
//            int offset = 0;
//            while (in != null) {
//                int bytesRead = in.read(inboundMessageBuffer, offset, inboundMessageBuffer.length - offset);
//                if (bytesRead <= 0) {
//                    if (closeInProgress) {
//                        disconnect("No socket data"); // TODO: disconnect?
//                        break;
//                    }
//                    throw ConnectionProblemException.NO_SOCKET_DATA;
//                } else {
//                    offset = processInboundMessages(offset + bytesRead);
//                }
//            }
            LOGGER.info().append(this).append("Finishing FIX session").commit();
            normalExit = true;
//        } catch (InvalidFixMessageException e) {
//            errorProcessingMessage("Protocol Error", e, false);
        } catch (ConnectionProblemException e) {
            errorProcessingMessage("Connection Problem", e, false);
        } catch (SocketException e) {
            errorProcessingMessage("Socket Error (Other side disconnected?)", e, false);
        } catch (Exception e) {
            errorProcessingMessage("General error", e, true);
        }

        assertSessionStatus(SessionStatus.Disconnected);
        return normalExit;
    }

    //==========================================================================
    //                    STREAM PARSING ORIGINAL
    //==========================================================================
    /**
     * Process inbound messages until session ends
     *
     * @return
     */
    protected final boolean processInboundMessages() {
        return processInboundMessages(null, 0);
    }

    /**
     * Process inbound messages until session ends
     *
     * @param logonBuffer buffer containing session LOGON message and may be
     * some other messages or a part of them (can be null)
     * @param length actual number of bytes that should be consumed from
     * logonBuffer
     * @return
     */
    protected final boolean processInboundMessages(byte[] logonBuffer, int length) {
        LOGGER.info().append(this).append("Processing FIX Session").commit();
        boolean normalExit = false;
        try {
            int offset = 0;
            if (logonBuffer != null) {
                System.arraycopy(logonBuffer, 0, inboundMessageBuffer, 0, length);
                offset = processInboundMessages(length);
            }

            while (in != null) {
                int bytesRead = in.read(inboundMessageBuffer, offset, inboundMessageBuffer.length - offset);
                if (bytesRead <= 0) {
                    if (closeInProgress) {
                        disconnect("No socket data"); // TODO: disconnect?
                        break;
                    }
                    throw ConnectionProblemException.NO_SOCKET_DATA;
                } else {
                    offset = processInboundMessages(offset + bytesRead);
                }
            }
            LOGGER.info().append(this).append("Finishing FIX session").commit();
            normalExit = true;
        } catch (InvalidFixMessageException e) {
            errorProcessingMessage("Protocol Error", e, false);
        } catch (ConnectionProblemException e) {
            errorProcessingMessage("Connection Problem", e, false);
        } catch (SocketException e) {
            errorProcessingMessage("Socket Error (Other side disconnected?)", e, false);
        } catch (Exception e) {
            errorProcessingMessage("General error", e, true);
        }

        assertSessionStatus(SessionStatus.Disconnected);
        return normalExit;
    }

    protected void errorProcessingMessage(String errorText, Exception e, boolean logStackTrace) {
        //TODO:listener on error
        //if (active) {
        if (logStackTrace) {
            LOGGER.error().append(this).append(errorText).append(" : ").append(e).commit();
        } else {
            LOGGER.error().append(this).append(errorText).append(" : ").append(e.getMessage()).commit();
        }
        disconnect(errorText);
        //}
    }

    protected int processInboundMessages(final int bytesRead) throws IOException, InvalidFixMessageException, ConnectionProblemException {
        assert bytesRead > 0;
        int messageStart = 0;
        int readMessageLength;
        while ((readMessageLength = bytesRead - messageStart) >= MIN_FIX_MESSAGE_LENGTH) {

            parser.set(inboundMessageBuffer, messageStart, readMessageLength);

            // All FIX messages begin with 3 required tags: BeginString, BodyLength, and MsgType.
            FixCommunicatorHelper.parseBeginString(parser, beginString);
            final int bodyLength = FixCommunicatorHelper.parseBodyLength(parser);
            final int msgTypeStart = parser.getOffset();

            final int lengthOfBeginStringAndBodyLength = msgTypeStart - messageStart;
            final int messageLength = lengthOfBeginStringAndBodyLength + bodyLength + CHECKSUM_LENGTH; // BodyLength is the number of characters in the message following the BodyLength field up to, and including, the delimiter immediately preceding the CheckSum tag ("10=123|")
            FixCommunicatorHelper.checkMessageLength(messageLength, inboundMessageBuffer.length);

            if (readMessageLength < messageLength) {
                break; // retry after we read full message in the buffer
            }
            parser.set(inboundMessageBuffer, msgTypeStart, bodyLength);

            if (!parser.next()) {
                throw InvalidFixMessageException.MISSING_MSG_TYPE;
            }

            parser.getByteSequence(msgType);

            if (messageLog != null) {
                messageLog.log(true, inboundMessageBuffer, messageStart, messageLength);
            }

            final int msgSeqNum = FixCommunicatorHelper.findMsgSeqNum(parser);

            // set parser limit to consume single message
            parser.set(inboundMessageBuffer, messageStart, messageLength);

            processInboundMessage(parser, msgType, msgSeqNum);

            messageStart += messageLength; // go to next message
        }

        // Move remaining part at the beginning of buffer
        int remainingSize = bytesRead - messageStart;
        if (remainingSize > 0 && messageStart != 0) {
            System.arraycopy(inboundMessageBuffer, messageStart, inboundMessageBuffer, 0, remainingSize);
        }
        return remainingSize;
    }
    //==========================================================================
    //                           CONNECTION
    //==========================================================================

    protected void connect(InputChannel in, OutputChannel out) {
        this.messageLog = (messageLogFactory != null) ? messageLogFactory.create(getSessionID()) : null;
        this.in = in;
        this.out = (messageLog != null) ? new LoggingOutputChannel(messageLog, out) : out;
    }

    /**
     * Send LOGOUT but do not drop socket connection (session enters
     * {@link org.f1x.api.session.SessionStatus#InitiatedLogout} state)
     */
    @Override
    public void logout(CharSequence cause) {
        sendLogout(cause);
    }

    /**
     * Terminate socket connection immediately (no LOGOUT message is sent if
     * session is in process). Session enters
     * {@link org.f1x.api.session.SessionStatus#Disconnected} state. Initiator
     * will try to re-connect after little delay (delay is configurable, subject
     * to session schedule).
     *
     * @param cause
     */
    @Override
    public void disconnect(CharSequence cause) {
        LOGGER.info().append(this).append("FIX Disconnect due to ").append(cause).commit();

        setSessionStatus(SessionStatus.Disconnected);
        final long now = timeSource.currentTimeMillis();
        sessionState.setLastConnectionTimestamp(now);
        try {
            if (in != null) { //TODO: Volatile
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }

            if (messageLog != null) {
                messageLog.close();
                messageLog = null;
            }
        } catch (IOException e) {
            LOGGER.warn().append(this).append("Error closing socket: ").append(e).commit();
        }
    }

    /**
     * Logout current session (if needed) and terminate socket connection.
     */
    @Override
    public void close() {
        this.closeInProgress = true;
        sendLogout("Goodbye");
    }

    /**
     * Sends a message using next sequence number. This message is persisted in
     * message store
     *
     * @param messageBuilder
     * @throws java.io.IOException
     */
    protected void doSend(MessageBuilder messageBuilder) throws IOException {
        final long now = timeSource.currentTimeMillis();
        synchronized (sendLock) {
            int msgSeqNum = sessionState.consumeNextSenderSeqNum();
            messageAssembler.send(getSessionID(), msgSeqNum, messageBuilder, messageStore, now, out);
        }
        sessionState.setLastSentMessageTimestamp(now);
    }

    @Override
    public void send(B messageBuilder) throws IOException {
        //TODO:optimize
        send(messageBuilder.getMessageType(), messageBuilder);
    }

    @Override
    public void send(final MsgType msgType, B messageBuilder) throws IOException {
        beforeMessageSent(msgType, messageBuilder);
        doSend(messageBuilder);
    }

    @Override
    public void send(final CharSequence msgType, B messageBuilder) throws IOException {
        beforeMessageSent(msgType, messageBuilder);
        doSend(messageBuilder);
    }

    public void send(final GenericRequest request) throws IOException {
        beforeMessageSent(request.getMsgType(), (B) request.getMessage());
        //TODO: synchronize id generation
        request.setId((int) identifierGenerator.next());
        doSend(request.getMessage());
        toString();
    }

    protected abstract void beforeLogonSent(final B messageBuilder);

    /**
     * Resend a message with given sequence number. The message is not persisted
     * in message store.
     *
     * @param messageBuilder
     * @param forcedMsgSeqNum
     * @throws java.io.IOException
     */
    protected void resend(B messageBuilder, int forcedMsgSeqNum) throws IOException {
        final long now = timeSource.currentTimeMillis();
        synchronized (sendLock) {
            messageAssembler.send(getSessionID(), forcedMsgSeqNum, messageBuilder, null, now, out);
        }
        sessionState.setLastSentMessageTimestamp(now);
    }

    /**
     * * @param forceResetSequenceNumbers pass true to force sequence numbers
     * reset, otherwise implementation will rely on
     * {@link org.f1x.api.FixSettings#isResetSequenceNumbersOnEachLogon()}
     * @param forceResetSequenceNumbers
     * @throws java.io.IOException
     */
    protected void sendLogon(boolean forceResetSequenceNumbers) throws IOException {
        if (!forceResetSequenceNumbers) {
            forceResetSequenceNumbers = settings.isResetSequenceNumbersOnEachLogon();
        }

        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.LOGON);
            sessionMessageBuilder.add(FixTags.EncryptMethod, EncryptMethod.NONE_OTHER);
            sessionMessageBuilder.add(FixTags.HeartBtInt, settings.getHeartBeatIntervalSec());
            sessionMessageBuilder.add(FixTags.ResetSeqNumFlag, forceResetSequenceNumbers);

            if (settings.isLogonWithNextExpectedMsgSeqNum()) {
                sessionMessageBuilder.add(FixTags.NextExpectedMsgSeqNum, sessionState.getNextTargetSeqNum());
            }

            sessionMessageBuilder.add(FixTags.MaxMessageSize, settings.getMaxInboundMessageSize());
            beforeLogonSent(sessionMessageBuilder);
            synchronized (sendLock) {
                if (forceResetSequenceNumbers) {
                    sessionState.setNextSenderSeqNum(1);
                    //TODO:check
//                    sessionState.resetNextSeqNums();
                    messageStore.clean();
                }

                doSend(sessionMessageBuilder);
            }
        }
    }

    private void beforeMessageSent(final MsgType msgType, final B messageBuilder) {
        if (eventListener != null) {
            eventListener.beforeMessageSent(msgType, messageBuilder);
        }
    }

    private void beforeMessageSent(final CharSequence msgType, final B messageBuilder) {
        //TODO: optimize
        beforeMessageSent(MsgType.parse(msgType.toString()), messageBuilder);
    }

    protected void sendLogout(CharSequence cause) {
        if (setSessionStatus(SessionStatus.ApplicationConnected, SessionStatus.InitiatedLogout)) {
            LOGGER.info().append(this).append("Initiating LOGOUT: ").append(cause).commit();

            try {
                synchronized (sessionMessageBuilder) {
                    sessionMessageBuilder.clear();
                    sessionMessageBuilder.setMessageType(MsgType.LOGOUT);
                    if (cause != null) {
                        sessionMessageBuilder.add(FixTags.Text, cause);
                    }
                    send(MsgType.LOGOUT, sessionMessageBuilder);
                }
            } catch (IOException e) {
                LOGGER.warn().append(this).append("Error logging out from FIX session: ").append(e).commit();
            }
        } else {
            LOGGER.info().append(this).append("Skipping LOGOUT (Not connected)").commit();
        }
    }

    /**
     * Sends FIX Heartbeat(0) message
     *
     * @param testReqId required when heartbeat is sent in response to
     * TestRequest(1)
     * @throws java.io.IOException
     */
    protected void sendHeartbeat(CharSequence testReqId) throws IOException {
        assertSessionStatus(SessionStatus.ApplicationConnected);
        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.HEARTBEAT);
            if (testReqId != null) {
                sessionMessageBuilder.add(FixTags.TestReqID, testReqId);
            }
            send(MsgType.HEARTBEAT, sessionMessageBuilder);
        }
    }

    /**
     * Sends FIX TestRequest(1) message.
     *
     * @param testReqId Verifies that the opposite application is generating the
     * heartbeat as the result of Test Request (1) and not a normal timeout. The
     * opposite application includes the TestReqID (112) in the resulting
     * Heartbeat(0). Any string can be used as the TestReqID (112) (one
     * suggestion is to use a timestamp string).
     * @throws java.io.IOException
     */
    protected void sendTestRequest(CharSequence testReqId) throws IOException {
        assertSessionStatus(SessionStatus.ApplicationConnected);
        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.TEST_REQUEST);
            sessionMessageBuilder.add(FixTags.TestReqID, testReqId);
            send(MsgType.TEST_REQUEST, sessionMessageBuilder);
        }
    }

    /**
     * @param rejectedMsgSeqNum MsgSeqNum(34) of rejected message This method
     * sends FIX Reject(3).
     * @param rejectReason optional reject reason
     * @param text optional explanation message
     * @throws java.io.IOException
     */
    protected void sendReject(int rejectedMsgSeqNum, SessionRejectReason rejectReason, CharSequence text) throws IOException {
        assertSessionStatus(SessionStatus.ApplicationConnected);
        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.REJECT);
            sessionMessageBuilder.add(FixTags.RefSeqNum, rejectedMsgSeqNum);

            if (rejectReason != null) {
                sessionMessageBuilder.add(FixTags.SessionRejectReason, rejectReason);
            }

            if (text != null) {
                sessionMessageBuilder.add(FixTags.Text, text);
            }
            send(MsgType.REJECT, sessionMessageBuilder);
        }
    }

    /**
     * This method sends ResendRequest(2).
     *
     * @param beginSeqNo start of range to resend (inclusive)
     * @param endSeqNo end of range to resend (inclusive). Zero means infinity
     * (resend up to the latest).
     * @throws java.io.IOException
     */
    protected void sendResendRequest(int beginSeqNo, int endSeqNo) throws IOException {

        LOGGER.warn().append(this).append("Requesting RESEND from ").append(beginSeqNo).append(" to ").append(endSeqNo).commit();

        assertSessionStatus2(SessionStatus.ApplicationConnected, SessionStatus.InitiatedLogout);
        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.RESEND_REQUEST);

            sessionMessageBuilder.add(FixTags.BeginSeqNo, beginSeqNo);
            sessionMessageBuilder.add(FixTags.EndSeqNo, endSeqNo - 1);
            send(MsgType.RESEND_REQUEST, sessionMessageBuilder);
        }

    }

    /**
     * Sends SequenceReset(4) in response to ResendRequest when resending a
     * range of administrative messages or when resending actual application
     * messages is not appropriate (e.g. stale messages).
     *
     * @param msgSeqNum message sequence number of this message
     * @param newSeqNo new sequence number
     * @throws java.io.IOException
     */
    protected void sendGapFill(int msgSeqNum, int newSeqNo) throws IOException {
        assertSessionStatus2(SessionStatus.ApplicationConnected, SessionStatus.InitiatedLogout);

        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.SEQUENCE_RESET);

            sessionMessageBuilder.add(FixTags.PossDupFlag, true);
            sessionMessageBuilder.add(FixTags.NewSeqNo, newSeqNo);
            sessionMessageBuilder.add(FixTags.GapFillFlag, true);
            resend(sessionMessageBuilder, msgSeqNum);
        }
    }

    /**
     * Sends SequenceReset(4) in response to ResendRequest when resending a
     * range of administrative messages or when resending actual application
     * messages is not appropriate (e.g. stale messages). Sets sender message
     * sequence num to newSeqNo
     *
     * @param newSeqNo new sequence number
     * @throws java.io.IOException
     */
    protected void sendSequenceReset(int newSeqNo) throws IOException {
        assertSessionStatus(SessionStatus.ApplicationConnected);
        synchronized (sessionMessageBuilder) {
            sessionMessageBuilder.clear();
            sessionMessageBuilder.setMessageType(MsgType.SEQUENCE_RESET);

            sessionMessageBuilder.add(FixTags.NewSeqNo, newSeqNo);
            sessionMessageBuilder.add(FixTags.GapFillFlag, false);

            synchronized (sendLock) {
                sessionState.setNextSenderSeqNum(newSeqNo - 1);
                send(MsgType.SEQUENCE_RESET, sessionMessageBuilder); // In reset mode MsgSeqNum should be ignored
            }
        }
    }

    protected void processInboundMessage(M parser, CharSequence msgType, int msgSeqNumX) throws IOException, InvalidFixMessageException, ConnectionProblemException {
        final long now = timeSource.currentTimeMillis();
        sessionState.setLastReceivedMessageTimestamp(now); //? maybe extract from message SendingTime(52) field?
        SessionStatus currentStatus = getSessionStatus();
        switch (currentStatus) {
            case ApplicationConnected:
            case InitiatedLogout:
                processInSessionMessage(msgSeqNumX, msgType, parser);
                break;
            case SocketConnected:
                if (FixCommunicatorHelper.isLogon(msgType)) {
                    processInboundLogon(msgSeqNumX, parser);
                } else {
                    throw InvalidFixMessageException.EXPECTING_LOGON_MESSAGE;
                }
                break;
            case InitiatedLogon:
                if (FixCommunicatorHelper.isLogon(msgType)) {
                    processInboundLogon(msgSeqNumX, parser);
                } else if (FixCommunicatorHelper.isLogout(msgType)) {
                    processInboundLogout(msgSeqNumX, parser);
                } else {
                    throw InvalidFixMessageException.EXPECTING_LOGON_MESSAGE;
                }
                break;
            default:
                LOGGER.warn().append(this).append("Received unexpected message (35=").append(msgType).append(") in status ").append(currentStatus).commit();
        }
        //TODO: listener call before or after process?
        if (eventListener != null) {
            eventListener.onMessage(msgType, parser);
        }
    }

    private void processInSessionMessage(int msgSeqNumX, CharSequence msgType, M parser) throws IOException, InvalidFixMessageException, ConnectionProblemException {
        boolean processed = true;
        if (msgType.length() == 1) { // All session-level messages have MsgType expressed using single char
            switch (msgType.charAt(0)) {
                case AdminMessageTypes.LOGON:
                    processInboundLogon(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.LOGOUT:
                    processInboundLogout(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.HEARTBEAT:
                    processInboundHeartbeat(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.TEST:
                    processInboundTestRequest(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.RESEND:
                    processInboundResendRequest(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.REJECT:
                    processInboundReject(msgSeqNumX, parser);
                    break;
                case AdminMessageTypes.RESET:
                    processInboundSequenceReset(msgSeqNumX, parser);
                    break;
                default:
                    processed = false;
            }
        } else {
            processed = false;
        }
        if (!processed) {
            _processInboundAppMessage(msgType, msgSeqNumX, parser);
        }
    }

    /**
     * @param msgSeqNumX message sequence number (negative for messages that
     * have PossDupFlag=Y).
     */
    private void _processInboundAppMessage(CharSequence msgType, int msgSeqNumX, M parser) throws IOException, InvalidFixMessageException {
        LOGGER.debug().append(this).append("Processing inbound message with type: ").append(msgType).commit();

        final boolean possDup;
        if (msgSeqNumX > 0) { // PossDupFlag=N
            int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
            if (!checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum)) //Let's imagine we expected MsgSeqNum=5 but received 10
            {
                sendResendRequest(expectedTargetSeqNum, msgSeqNumX - 1);   //This will doSend ResendRequest(5, 0) and set currentResendEndSeqNo=9
            }
            sessionState.setNextTargetSeqNum(msgSeqNumX + 1);
            possDup = false;
        } else {
            msgSeqNumX = -msgSeqNumX;
            possDup = true;
        }

        processInboundAppMessage(msgType, msgSeqNumX, possDup, parser);
    }

    /**
     *
     * @param msgType type of the message [tag MsgType(35)].
     * @param msgSeqNum message sequence number [tag MsgSeqNum(34)].
     * @param possDup <code>true</code> if this message is marked as a duplicate
     * using tag PossDupFlag(43)
     * @param parser
     * @throws java.io.IOException
     */
    protected void processInboundAppMessage(CharSequence msgType, int msgSeqNum, boolean possDup, M parser) throws IOException {
        assert msgSeqNum > 0; // by default do nothing
        if (eventListener != null) {
            eventListener.onApplicationMessage(msgType, msgSeqNum, possDup, parser);
        }
    }

    /**
     * Handle inbound LOGON message depending on FIX session role
     * (acceptor/initator) and current status
     *
     * @param msgSeqNum
     * @param parser
     * @throws java.io.IOException
     * @throws org.f1x.v1.InvalidFixMessageException
     * @throws org.f1x.v1.ConnectionProblemException
     */
    protected void processInboundLogon(int msgSeqNum, M parser) throws IOException, InvalidFixMessageException, ConnectionProblemException {
        LOGGER.debug().append(this).append("Processing inbound LOGON(A)").commit();

        if (msgSeqNum < 0) {
            LOGGER.warn().append(this).append("Received LOGON(A) message with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNum).commit();
            return;
        }

        boolean heartbeatIntervalPresent = false;
        boolean resetSeqNum = false;
//        System.out.println("processInboundLogon(" + parser.describe() + ")");
        while (parser.next()) {
//            System.out.println("1-processInboundLogon(" + parser.toString() + ")");
            switch (parser.getTagNum()) {
                case FixTags.HeartBtInt:
                    if (parser.getIntValue() != settings.getHeartBeatIntervalSec()) {
                        throw ConnectionProblemException.HEARTBEAT_INTERVAL_MISMATCH; //TODO: Allow initiator to override heartbeat interval for acceptor
                    }
                    heartbeatIntervalPresent = true;
                    break;
                case FixTags.ResetSeqNumFlag:
                    resetSeqNum = parser.getBooleanValue();
                    //if (getSessionStatus() != SessionStatus.ApplicationConnected) //TODO: Unless we are dealing with In-Session sequence reset
                    if (resetSeqNum && msgSeqNum != 1) {
                        throw InvalidFixMessageException.MSG_SEQ_NUM_MUST_BE_ONE;
                    }

                    break;
                default:
                    processCustomLogonTag(parser);
            }
        }

        if (!heartbeatIntervalPresent) {
            throw InvalidFixMessageException.NO_HEARTBEAT_INTERVAL;
        }

        //TODO: This is wrong 141=Y means that both sides should reset sequence numbers
        if (resetSeqNum) {
            sessionState.setNextTargetSeqNum(1);
//            sessionState.resetNextSeqNums();
        }

        final SessionStatus currentStatus = getSessionStatus();
        if (currentStatus == SessionStatus.ApplicationConnected && !resetSeqNum) {
            throw InvalidFixMessageException.IN_SESSION_LOGON_MESSAGE_WITHOUT_MSG_SEQ_RESET_NOT_EXPECTED;
        }

        int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
        boolean expectedTargetMsgSeqNum = checkTargetMsgSeqNum(msgSeqNum, expectedTargetSeqNum);
        sessionState.setNextTargetSeqNum(msgSeqNum + 1);

        switch (currentStatus) {
            case SocketConnected:
                setSessionStatus(SessionStatus.ReceivedLogon);
                sendLogon(resetSeqNum);
                setSessionStatus(SessionStatus.ApplicationConnected);
                break;
            case InitiatedLogon:
                setSessionStatus(SessionStatus.ApplicationConnected);
                break;
            case ApplicationConnected:
                sendLogon(resetSeqNum);
                break;
            default:
                LOGGER.warn().append(this).append("Unexpected LOGON(A) in status: ").append(currentStatus).commit();
                return;
        }

        // *After* sending a Logon confirmation back, doSend a ResendRequest
        if (!expectedTargetMsgSeqNum) {
            sendResendRequest(expectedTargetSeqNum, msgSeqNum);
        }

    }

    /**
     * Can be used to validate custom logon tags (e.g. Password(443) tag)
     *
     * @param parser
     */
    protected void processCustomLogonTag(M parser) {
        // by default does nothing
    }

    protected void processInboundLogout(int msgSeqNumX, M parser) throws IOException, InvalidFixMessageException {
        LOGGER.debug().append(this).append("Processing inbound LOGOUT(5)").commit();

        if (msgSeqNumX < 0) {
            LOGGER.warn().append(this).append("Received LOGOUT(5) message with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNumX).commit();
            return;
        }

        int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
        boolean expectedTargetMsgSeqNum = checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum);

        temporaryByteArrayReference.clear();
        while (parser.next()) {
            if (parser.getTagNum() == FixTags.Text) {
                parser.getByteSequence(temporaryByteArrayReference);
                break;
            }
        }

        LOGGER.info().append(this).append("LOGOUT(5) received: ").append(temporaryByteArrayReference).commit();

        final SessionStatus currentStatus = getSessionStatus();
        switch (currentStatus) {
            case ApplicationConnected:
                sessionState.setNextTargetSeqNum(msgSeqNumX + 1);
                // If a message gap was detected, issue a ResendRequest to retrieve all missing messages followed by a Logout message which serves as a confirmation of the logout request.
                // DO NOT terminate the session.  The initiator of the Logout sequence has responsibility to terminate the session.
                // This allows the Logout initiator to respond to any ResendRequest message.
                if (!expectedTargetMsgSeqNum) {
                    sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
                }
                sendLogout("Responding to LOGOUT(5) request");
                if (expectedTargetMsgSeqNum) {
                    setSessionStatus(SessionStatus.SocketConnected);
                }
                break;
            case InitiatedLogout:
                if (expectedTargetMsgSeqNum) {
                    sessionState.consumeNextTargetSeqNum();
                }
                // If this side was the initiator of the Logout sequence,
                // then this is a Logout confirmation and the session should be immediately terminated upon receipt.
                disconnect("Both sides exchanged LOGOUT(5)");
                break;
            case InitiatedLogon:
                if (expectedTargetMsgSeqNum) {
                    sessionState.consumeNextTargetSeqNum();
                }
                disconnect("LOGON(A) rejected");
                break;
            default:
                LOGGER.info().append(this).append("Unexpected LOGOUT(5) message in status: ").append(currentStatus).commit();
                break;
        }
    }

    protected void processInboundHeartbeat(int msgSeqNumX, M parser) throws InvalidFixMessageException, IOException {
        LOGGER.debug().append(this).append("Processing Inbound HEARTBEAT(0)").commit();

        if (msgSeqNumX < 0) {
            LOGGER.warn().append(this).append("Received HEARTBEAT(0) message with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNumX).commit();
            return;
        }

        int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
        if (!checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum)) {
            sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
        }

        sessionState.setNextTargetSeqNum(msgSeqNumX + 1);
    }

    protected void processInboundTestRequest(int msgSeqNumX, M parser) throws IOException, InvalidFixMessageException {
        LOGGER.debug().append("Processing inbound TEST(1) request").commit();

        if (msgSeqNumX < 0) {
            LOGGER.warn().append(this).append("Received TEST(1) request with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNumX).commit();
            return;
        }

        int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
        boolean expectedTargetMsgSeqNum = checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum);
        sessionState.setNextTargetSeqNum(msgSeqNumX + 1);
        if (expectedTargetMsgSeqNum) {

            temporaryByteArrayReference.clear();
            while (parser.next()) {
                if (parser.getTagNum() == FixTags.TestReqID) {
                    parser.getByteSequence(temporaryByteArrayReference);
                    break;
                }
            }

            if (temporaryByteArrayReference.length() == 0) {
                sendReject(msgSeqNumX, SessionRejectReason.REQUIRED_TAG_MISSING, "Missing TestReqID(112)");
            } else {
                sendHeartbeat(temporaryByteArrayReference);
            }
        } else {
            sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
        }
    }

    private void processInboundResendRequest(int msgSeqNumX, M parser) throws IOException, InvalidFixMessageException {
        LOGGER.debug().append("Processing inbound RESEND(2) request").commit();

        int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();

        if (msgSeqNumX < 0) {
            LOGGER.warn().append(this).append("Received RESEND(2) message with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNumX).commit();
            return;
        }

        boolean expectedTargetMsgSeqNum = checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum);
        sessionState.setNextTargetSeqNum(msgSeqNumX + 1);

        int beginSeqNo = -1;
        int endSeqNo = -1;
        while (parser.next()) {
            switch (parser.getTagNum()) {
                case FixTags.BeginSeqNo:  // required
                    beginSeqNo = parser.getIntValue();
                    break;
                case FixTags.EndSeqNo:  // required
                    endSeqNo = parser.getIntValue();
                    break;
            }
        }

        // If message gap is detected, perform the Resend processing first, followed by a ResendRequest of  your own in order to fill the incoming message gap.
        if (beginSeqNo == -1) {
            sendReject(msgSeqNumX, SessionRejectReason.REQUIRED_TAG_MISSING, "Missing BeginSeqNo(7)");
        } else if (beginSeqNo == 0) {
            sendReject(msgSeqNumX, SessionRejectReason.VALUE_IS_INCORRECT, "Invalid BeginSeqNo(7)");
        } else if (endSeqNo == -1) {
            sendReject(msgSeqNumX, SessionRejectReason.REQUIRED_TAG_MISSING, "Missing EndSeqNo(16)");
        } else {
            resendMessages(beginSeqNo, endSeqNo != 0 ? endSeqNo : (sessionState.getNextSenderSeqNum() - 1));
        }

        if (!expectedTargetMsgSeqNum) {
            sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
        }
    }

    protected void resendMessages(int beginSeqNo, int endSeqNo) throws IOException {
        if (beginSeqNo > endSeqNo) {
            LOGGER.warn().append(this).append("Resending messages was skipped: beginSeqNo > endSeqNo").commit();
            return;
        }

        MessageStore.MessageStoreIterator iterator = messageStore.iterator(beginSeqNo, endSeqNo);

        int msgSeqNumOfLastResentMessage = beginSeqNo - 1;
        int msgSeqNum;
        while ((msgSeqNum = iterator.next(messageBufferForResend)) > 0) {
            if (resend(messageBufferForResend, msgSeqNum, msgSeqNumOfLastResentMessage)) {
                msgSeqNumOfLastResentMessage = msgSeqNum;
            }
        }

        if (msgSeqNumOfLastResentMessage < endSeqNo) {
            sendGapFill(msgSeqNumOfLastResentMessage + 1, endSeqNo + 1);
        }
    }

    /**
     * @return true if message was resent otherwise false
     */
    private boolean resend(byte[] message, int msgSeqNum, int msgSeqNumOfLastResentMessage) throws IOException {
        try {
            parserForResend.set(message, 0, message.length);

            FixCommunicatorHelper.parseBeginString(parserForResend, beginString);
            final int bodyLength = FixCommunicatorHelper.parseBodyLength(parserForResend);

            final int lengthOfBeginStringAndBodyLength = parserForResend.getOffset();
            final int messageLength = lengthOfBeginStringAndBodyLength + bodyLength + CHECKSUM_LENGTH;

            if (!parserForResend.next()) {
                throw InvalidFixMessageException.MISSING_MSG_TYPE;
            }

            parserForResend.getByteSequence(msgTypeForResend);

            parserForResend.set(message, 0, messageLength);

            messageBuilderForResend.clear();
            if (!onMessageResend(msgTypeForResend, parserForResend, messageBuilderForResend)) {
                return false;
            }

            int msgSeqNumGap = msgSeqNum - msgSeqNumOfLastResentMessage;
            if (msgSeqNumGap > 1) {
                sendGapFill(msgSeqNumOfLastResentMessage + 1, msgSeqNum);
            }

            resend(messageBuilderForResend, msgSeqNum);
            return true;
        } catch (InvalidFixMessageException | FixParserException e) {
            LOGGER.warn().append(this).append("Got invalid message #").append(msgSeqNum).append(" from message store : ").append(e).commit();
            return false;
        }
    }

    /**
     * @param msgType
     * @param parser of resending message
     * @param messageBuilder that will be sent
     * @return true if a message requires the resending otherwise false
     */
    protected boolean onMessageResend(CharSequence msgType, M parser, MessageBuilder messageBuilder) {
        if (!isResendRequired(msgType)) {
            return false;
        }

        messageBuilder.setMessageType(msgType);
        messageBuilder.add(FixTags.PossDupFlag, true);
        while (parser.next()) {
            int tagNum = parser.getTagNum();
            switch (tagNum) {
                case FixTags.MsgType:
                case FixTags.MsgSeqNum:
                case FixTags.BeginString:
                case FixTags.BodyLength:
                case FixTags.SenderCompID:
                case FixTags.SenderSubID:
                case FixTags.TargetCompID:
                case FixTags.TargetSubID:
                case FixTags.CheckSum:
                    break;
                case FixTags.SendingTime:
                    messageBuilder.add(FixTags.OrigSendingTime, parser.getCharSequenceValue());
                    break;
                default:
                    messageBuilder.add(tagNum, parser.getCharSequenceValue());
            }
        }

        return true;
    }

    /**
     * @param msgType
     * @return true if a message with this msgType requires the resending
     * otherwise false
     */
    protected boolean isResendRequired(CharSequence msgType) {
        return !AdminMessageTypes.isAdmin(msgType) || msgType.charAt(0) == AdminMessageTypes.REJECT; // do not resend Admin message unless it is a REJECT(3)
    }

    private void processInboundSequenceReset(int msgSeqNumX, M parser) throws IOException, InvalidFixMessageException {
        LOGGER.debug().append(this).append("Processing inbound Sequence Reset").commit();

        boolean isGapFill = false;
        int newSeqNum = -1;
        while (parser.next()) {
            switch (parser.getTagNum()) {
                case FixTags.NewSeqNo:  // required
                    newSeqNum = parser.getIntValue();
                    break;
                case FixTags.GapFillFlag:
                    isGapFill = parser.getBooleanValue();
                    break;
            }
        }

        if (msgSeqNumX < 0) {
            // Normal for gap fill to have PossDupFlag=Y
            if (isGapFill) {
                LOGGER.debug().append(this).append("Ignoring GapFill from").append(-msgSeqNumX).append(" to ").append(newSeqNum).commit();
            } else {
                LOGGER.info().append(this).append("Received RESET message with PossDupFlag=Y - ignoring. MsgSeqNum ").append(-msgSeqNumX).commit();
            }
            return;
        }

        LOGGER.info().append(this).append("Processing inbound message sequence reset to ").append(newSeqNum).commit();
        //noinspection StatementWithEmptyBody
        if (isGapFill) {
            int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
            if (!checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum)) {
                sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
            }
        } else {
            // If message gap is detected Ignore the incoming sequence number.
            // The NewSeqNo field of the SeqReset message will contain the sequence number of the next message to be transmitted.
        }

        try {
            if (newSeqNum <= sessionState.getNextTargetSeqNum()) {
                throw InvalidFixMessageException.RESET_BELOW_CURRENT_SEQ_LARGE;
            }

            sessionState.setNextTargetSeqNum(newSeqNum);
        } catch (InvalidFixMessageException e) {
            sendReject(msgSeqNumX, SessionRejectReason.INCORRECT_DATA_FORMAT_FOR_VALUE, e.getMessage());
        }
    }

    protected void processInboundReject(int msgSeqNumX, M parser) throws InvalidFixMessageException, IOException {
        LOGGER.debug().append(this).append("Processing inbound REJECT(3)").commit();

        // Skip sequence number checking if we are dealing with GapFill
        if (msgSeqNumX > 0) {
            int expectedTargetSeqNum = sessionState.getNextTargetSeqNum();
            if (!checkTargetMsgSeqNum(msgSeqNumX, expectedTargetSeqNum)) {
                sendResendRequest(expectedTargetSeqNum, msgSeqNumX);
            }
            sessionState.setNextTargetSeqNum(msgSeqNumX + 1);
        }

        int refSeqNum = -1;
        temporaryByteArrayReference.clear();
        while (parser.next()) {
            switch (parser.getTagNum()) {
                case FixTags.RefSeqNum:
                    refSeqNum = parser.getIntValue();
                    break;
                case FixTags.Text:
                    parser.getByteSequence(temporaryByteArrayReference);
                    break;
            }
        }

        // NOTE: some brokers use session-level REJECT to reject abnormal app-level messages
        //This default implementation simply log REJECT messages:
        if (temporaryByteArrayReference.length() != 0) {
            LOGGER.warn().append(this).append("Received REJECT(3):").append(refSeqNum).append(": ").append(temporaryByteArrayReference).commit();
        } else {
            LOGGER.warn().append(this).append("Received REJECT(3):").append(refSeqNum).commit();
        }

    }

    /// Timers
    /**
     * Schedules a timer to finish current FIX session according to FIX Session
     * Schedule
     *
     * @param timeout
     */
    protected void scheduleSessionEnd(long timeout) {
        SessionEndTask timer = new SessionEndTask(this);
        GlobalTimer.getInstance().schedule(timer, timeout);

        sessionEndTask.set(timer);
    }

    /**
     * Cancels a timer that was defined to finish current FIX session (if
     * defined)
     */
    protected void unscheduleSessionEnd() {
        TimerTask timer = sessionEndTask.getAndSet(null);
        if (timer != null) {
            timer.cancel();
        }
    }

    protected synchronized void scheduleSessionMonitoring() {
        int checkIntervalMs = settings.getHeartbeatCheckIntervalMs();
        if (checkIntervalMs > 0) {
            if (sessionMonitoringTask.get() == null) {
                DefaultSessionMonitoringTask timer = new DefaultSessionMonitoringTask(this);
                GlobalTimer.getInstance().schedule(timer, checkIntervalMs, checkIntervalMs);
                sessionMonitoringTask.set(timer);
            } else {
                LOGGER.warn().append(this).append("Monitoring task already defined").commit();
            }
        }
    }

    protected synchronized void unscheduleSessionMonitoring() {
        TimerTask timer = sessionMonitoringTask.getAndSet(null);
        if (timer != null) {
            timer.cancel();
        }
    }

    SessionState getSessionState() {
        return sessionState;
    }

    TimeSource getTimeSource() {
        return timeSource;
    }

    /**
     * <p>
     * Check message sequence number of inbound message. This method is not
     * called for inbound messages that have PossDupFlag(43)=Y.</p>
     *
     * <p>
     * If the incoming message has a sequence number less than expected and the
     * PossDupFlag is not set, it indicates a serious error. It is strongly
     * recommended that the session be terminated and manual intervention be
     * initiated. Default implementation throws TARGET_MSG_SEQ_NUM_LESS_EXPECTED
     * exception in this case. </p>
     *
     * @param actual
     * @param expected
     * @return true if actual sequence number match expected or false if
     * Communicator should issue RESEND(2) request from <tt>expected</tt> till
     * <tt>actual</tt>.
     * @throws org.f1x.v1.InvalidFixMessageException
     * @throws java.io.IOException
     */
    protected boolean checkTargetMsgSeqNum(int actual, int expected) throws InvalidFixMessageException, IOException {
        if (actual < expected) {
            throw InvalidFixMessageException.TARGET_MSG_SEQ_NUM_LESS_EXPECTED;
        }

        return actual == expected;
    }

    @Override
    public void appendTo(GFLogEntry entry) {
        LogUtils.log(getSessionID(), entry);
    }
}
