package org.f1x.v1;

import org.f1x.api.session.SessionStatus;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;

import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;

import java.util.TimerTask;
import org.f1x.api.session.FixSessionListener;

/** Timer responsible for sending HEARTBEATs from our side, and TEST request of we do not receive HEARTBEATs from other side */
class SessionMonitoringTask<M extends IMessageParser, B extends MessageBuilder> extends TimerTask {

    private static final GFLog LOGGER = GFLogFactory.getLog(SessionMonitoringTask.class);

    private final FixCommunicator<? extends M, ? extends B, ? extends FixSessionListener<? extends M, ? extends B>> communicator;
    private final int heartbeatInterval;

    public SessionMonitoringTask(FixCommunicator<? extends M, ? extends B, ? extends FixSessionListener<? extends M, ? extends B>> communicator) {
        this.communicator = communicator;
        heartbeatInterval = communicator.getSettings().getHeartBeatIntervalSec() * 1000;
    }

    @Override
    public void run() {
        final long currentTime = communicator.timeSource.currentTimeMillis();
        if (communicator.getSessionStatus() == SessionStatus.ApplicationConnected)
            checkInbound(currentTime);

        if (communicator.getSessionStatus() == SessionStatus.ApplicationConnected)
            checkOutbound(currentTime);
    }

    /** Check when we received last message from other side (doSend TEST if that happen long time ago) */
    private void checkInbound(long currentTime) {
        long lastReceivedMessageTimestamp = communicator.getSessionState().getLastReceivedMessageTimestamp();
        if (lastReceivedMessageTimestamp < currentTime - heartbeatInterval) {
            LOGGER.debug().append("Haven't heard from the other side for a while. Sending TEST(1) message to validate connection.").commit();
            try {
                //TODO: Other than sending Test request we need to add a logic that will force socket disconnect if we don't hear back
                communicator.sendTestRequest("Are you there?");
            } catch (Throwable e) {
                LOGGER.warn().append("Error sending TEST(1):").append(e).commit();
            }
        }
    }

    /** Check when we sent last message to other side (doSend HEARTBEAT if that happened long time ago) */
    private void checkOutbound(long currentTime) {
        long lastSentMessageTimestamp = communicator.getSessionState().getLastSentMessageTimestamp();
        if (lastSentMessageTimestamp < currentTime - heartbeatInterval) {
            LOGGER.debug().append("Connection is idle. Sending HEARTBEAT(0) to confirm connection.").commit();
            try {
                communicator.sendHeartbeat(null);
            } catch (Throwable e) {
                LOGGER.warn().append("Error sending HEARTBEAT(0):").append(e).commit();
            }
        }
    }

}
