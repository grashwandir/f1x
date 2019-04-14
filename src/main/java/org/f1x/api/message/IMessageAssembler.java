package org.f1x.api.message;

import java.io.IOException;
import org.f1x.api.message.fields.MsgType;
import org.f1x.api.session.SessionID;
import org.f1x.io.OutputChannel;
import org.f1x.store.MessageStore;

/**
 *
 * @author niels.kowalski
 * @param <T>
 */
public interface IMessageAssembler<T extends IOutputMessage> {

    void send(SessionID sessionID, int msgSeqNum, T messageBuilder, MessageStore messageStore, long sendingTime, final OutputChannel out) throws IOException;

//    void send(SessionID sessionID, long msgSeqNum, MessageBuilder messageBuilder, IMessageStore messageStore, long sendingTime, IOutputChannel out) throws IOException;
//
//    void send(final SessionID sessionID, long msgSeqNum, final T messageBuilder, final IMessageStore messageStore, final long sendingTime, final IOutputChannel out) throws IOException;

    T prepareMessage(final MsgType msgType);
}
