package org.f1x.v1;

import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.session.FixSessionListener;

/**
 *
 * @author niels.kowalski
 */
public class DefaultSessionMonitoringTask extends SessionMonitoringTask<IMessageParser, MessageBuilder> {

    public DefaultSessionMonitoringTask(FixCommunicator<? extends IMessageParser, ? extends MessageBuilder, ? extends FixSessionListener<? extends IMessageParser, ? extends MessageBuilder>> communicator) {
        super(communicator);
    }

}
