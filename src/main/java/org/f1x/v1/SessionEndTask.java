package org.f1x.v1;

import org.f1x.api.session.FixSession;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;

import java.util.TimerTask;
import org.f1x.api.session.FixSessionListener;

final class SessionEndTask extends TimerTask {

    private final FixSession<? extends IMessageParser, ? extends MessageBuilder, ? extends FixSessionListener<? extends IMessageParser, ? extends MessageBuilder>> session;

    public SessionEndTask(FixSession<? extends IMessageParser, ? extends MessageBuilder, ? extends FixSessionListener<? extends IMessageParser, ? extends MessageBuilder>> session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            FixCommunicator.LOGGER.info().append("Scheduled end time for FIX session ").append(session.getSessionID()).commit();
            session.logout("Scheduled end time");
        } catch (Throwable e) {
            FixCommunicator.LOGGER.warn().append("Error occurred during ending session").commit();
        }
    }
}
