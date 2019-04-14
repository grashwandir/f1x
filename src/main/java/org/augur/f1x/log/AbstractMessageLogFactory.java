package org.augur.f1x.log;

import org.f1x.api.session.SessionID;
import org.f1x.log.LogFormatter;
import org.f1x.log.MessageLog;
import org.f1x.log.MessageLogFactory;

/**
 *
 * @author niels.kowalski
 */
public abstract class AbstractMessageLogFactory implements MessageLogFactory {

    @Override
    public MessageLog create(final SessionID sessionID) {
        return create(new LogFormatterFix(sessionID));
    }

    protected abstract MessageLog create(final LogFormatter formatter);

//    protected abstract Logger createLogger(String string);

}
