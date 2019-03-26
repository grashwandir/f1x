package org.augur.f1x;

import java.util.logging.Level;
import org.augur.f1x.log.JULMessageLogFactory;
import org.f1x.api.FixInitiatorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.FixTags;
import org.f1x.api.session.InitiatorFixSessionAdaptor;
import org.f1x.api.session.SessionID;
import org.f1x.v1.FixSessionInitiator;
import org.f1x.api.session.InitiatorFixSessionListener;

/**
 *
 * @author niels.kowalski
 * @param <C>
 */
public class AugurFixSessionInitiator<C extends FixInitiatorSettings> extends FixSessionInitiator<InitiatorFixSessionListener> {

    private String password = null;

    public AugurFixSessionInitiator(String host, int port, final FixVersion fixVersion, final SessionID sessionID, final C settings) {
        super(host, port, fixVersion, sessionID, settings);
        setup();
    }

    public AugurFixSessionInitiator(String host, int port, final FixVersion fixVersion, final SessionID sessionID) {
        super(host, port, fixVersion, sessionID);
        setup();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void setup() {
        setMessageLogFactory(new JULMessageLogFactory(Level.INFO));
        setEventListener(new InitiatorFixSessionAdaptor() {
            @Override
            public void beforeLogonSent(MessageBuilder messageBuilder) {
                if (password != null) {
                    messageBuilder.add(FixTags.RawDataLength, password.length());
                    messageBuilder.add(FixTags.RawData, password);
                }
            }
        });
    }
}
