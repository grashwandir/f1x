package org.f1x.api.session;

import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;

public interface InitiatorFixSessionListener extends FixSessionListener<IMessageParser, MessageBuilder> {

    void beforeLogonSent(final MessageBuilder messageBuilder);
}
