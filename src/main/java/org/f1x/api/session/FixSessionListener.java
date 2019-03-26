package org.f1x.api.session;

import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.MessageParser;
import org.f1x.api.message.fields.MsgType;

public interface FixSessionListener<M extends MessageParser, B extends MessageBuilder> extends SessionListener<M, B> {

    void onLogon(final M msg);

    void onLogout(final M msg);

    void onHeartbeat(final M msg);

    void onReject(final M msg);

    void onApplicationMessage(CharSequence msgType, int msgSeqNum, boolean possDup, M parser);

    boolean beforeApplicationMessageSent(final MsgType msgType, final B messageBuilder);

}
