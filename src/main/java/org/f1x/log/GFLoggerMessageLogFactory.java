package org.f1x.log;

import org.f1x.api.session.SessionID;

public class GFLoggerMessageLogFactory implements MessageLogFactory {

    private final GFLoggerMessageLog INSTANCE;

    public GFLoggerMessageLogFactory(final LogFormatter formatter) {
        this.INSTANCE = new GFLoggerMessageLog(formatter);
    }

    public GFLoggerMessageLogFactory() {
        this(null);
    }

    //TODO: refactor to take sessionID into account
    @Override
    public MessageLog create(SessionID sessionID) {
        return INSTANCE;
    }
}
