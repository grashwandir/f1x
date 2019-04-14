package org.augur.f1x;

import java.io.IOException;
import org.augur.f1x.log.LogFormatterFix;
import org.f1x.api.FixVersion;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.FixTags;
import org.f1x.api.message.fields.MsgType;
import org.f1x.api.session.InitiatorFixSessionAdaptor;
import org.f1x.api.session.InitiatorFixSessionListener;
import org.f1x.api.session.SessionID;
import org.f1x.api.session.SessionStatus;
import org.f1x.log.GFLoggerMessageLogFactory;
import org.f1x.log.GFLoggerMessageLogInfo;
import org.f1x.v1.FixSessionInitiator;

/**
 *
 * @author niels.kowalski
 * @param <C>
 */
public class AugurFixSessionInitiator<C extends AugurInitiatorFixSessionConfig> extends FixSessionInitiator<InitiatorFixSessionListener> {

//    private final static GFLoggerMessageLogInfo APP_LOGGER = new GFLoggerMessageLogInfo("org.augur.f1x", new LogFormatterFix(" IN", "OUT"));

    final MarketDataRequest.Builder mdBuilder;

    public AugurFixSessionInitiator(String host, int port, final FixVersion fixVersion, final SessionID sessionID, final C settings) {
        super(host, port, fixVersion, sessionID, settings);
        this.mdBuilder = MarketDataRequest.Builder.create(null);
        setMessageLogFactory(new GFLoggerMessageLogFactory(new LogFormatterFix(sessionID)));
        setEventListener(new InitiatorFixSessionAdaptor() {
            @Override
            public void beforeLogonSent(MessageBuilder messageBuilder) {
                if (settings.getPassword() != null) {
                    messageBuilder.add(FixTags.RawDataLength, settings.getPassword().length());
                    messageBuilder.add(FixTags.RawData, settings.getPassword());
                }
            }

            @Override
            public void onError(Exception ex) {
//                System.out.println("onError");
                ex.printStackTrace();
            }

            @Override
            public void beforeMessageSent(MsgType msgType, MessageBuilder messageBuilder) {
//                System.out.println("beforeMessageSent");
            }

            @Override
            public void onMessage(CharSequence msgType, IMessageParser msg) {
//                System.out.println("onMessage");
            }

            @Override
            public boolean beforeApplicationMessageSent(MsgType msgType, MessageBuilder messageBuilder) {
//                System.out.println("beforeApplicationMessageSent");
                return super.beforeApplicationMessageSent(msgType, messageBuilder); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onApplicationMessage(CharSequence msgType, int msgSeqNum, boolean possDup, IMessageParser parser) {
//                System.out.println("onApplicationMessage");
                CharSequence cs = parser.describe();
//                APP_LOGGER.log(true, cs);
                switch (msgType.toString()) {
                    case "a": {
                        while (parser.next()) {
                            switch (parser.getTagNum()) {
                                case FixTags.HeartBtInt:
                                    if (parser.getIntValue() != settings.getHeartBeatIntervalSec()) {
                                    }
                                default:
                                    break;
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onReject(IMessageParser msg) {
//                System.out.println("onReject");
            }

            @Override
            public void onHeartbeat(IMessageParser msg) {
//                System.out.println("onHeartbeat");
            }

            @Override
            public void onLogout(IMessageParser msg) {
//                System.out.println("onLogout");
            }

            @Override
            public void onLogon(IMessageParser msg) {
//                System.out.println("onLogon");
            }

            @Override
            public void onStatusChanged(SessionID sessionID, SessionStatus oldStatus, SessionStatus newStatus) {
//                System.out.println("status changed: " + sessionID.getTargetCompId() + ": " + oldStatus + " -> " + newStatus);
                switch (newStatus) {
                    case Disconnected:
                    case SocketConnected:
                    case InitiatedLogon:
                    case ReceivedLogon:
                    case InitiatedLogout:
                        break;
                    case ApplicationConnected: {
                        try {
                            send(mdBuilder.build(AugurFixSessionInitiator.this));
                        } catch (IOException ex) {
                            LOGGER.error().append("Sending MD Request: ").append(ex).commit();
                        }
                    }
                    break;
                    default:
                        throw new AssertionError(newStatus.name());
                }
            }
        });
    }

    public AugurFixSessionInitiator(String host, int port, final FixVersion fixVersion, final SessionID sessionID) {
        this(host, port, fixVersion, sessionID, null);
    }

    public AugurFixSessionInitiator(final C settings) {
        this(settings.getHost(), settings.getPort(), settings.getFixVersion(), settings.getSessionID(), settings);
    }

}
