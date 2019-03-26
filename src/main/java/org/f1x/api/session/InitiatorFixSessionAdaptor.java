package org.f1x.api.session;

import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.MsgType;

/**
 *
 * @author niels.kowalski
 */
public class InitiatorFixSessionAdaptor implements InitiatorFixSessionListener {

    @Override
    public void beforeLogonSent(MessageBuilder messageBuilder) {
    }

    @Override
    public void onLogon(IMessageParser msg) {
    }

    @Override
    public void onLogout(IMessageParser msg) {
    }

    @Override
    public void onHeartbeat(IMessageParser msg) {
    }

    @Override
    public void onReject(IMessageParser msg) {
    }

    @Override
    public void onApplicationMessage(CharSequence msgType, int msgSeqNum, boolean possDup, IMessageParser parser) {
    }

    @Override
    public boolean beforeApplicationMessageSent(MsgType msgType, MessageBuilder messageBuilder) {
        return true;
    }

    @Override
    public void onStatusChanged(SessionID sessionID, SessionStatus oldStatus, SessionStatus newStatus) {
    }

    @Override
    public void onMessage(CharSequence msgType, IMessageParser msg) {
    }

    @Override
    public void beforeMessageSent(MsgType msgType, MessageBuilder messageBuilder) {
    }

    @Override
    public void onError(Exception ex) {
    }

}
