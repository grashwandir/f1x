package org.f1x.api.session;

import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;

/**
 *
 * @author niels.kowalski
 */
public interface AcceptorFixSessionListener extends FixSessionListener<IMessageParser, MessageBuilder> {

}
