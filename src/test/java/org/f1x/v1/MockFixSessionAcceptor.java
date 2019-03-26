package org.f1x.v1;

import org.f1x.api.FixAcceptorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.session.AcceptorFixSessionListener;
import org.f1x.api.session.SessionID;

/**
 *
 * @author niels.kowalski
 */
public class MockFixSessionAcceptor extends FixSessionAcceptor<AcceptorFixSessionListener> {

    public MockFixSessionAcceptor(FixVersion fixVersion, SessionID sessionID, FixAcceptorSettings settings) {
        super(fixVersion, sessionID, settings);
    }

}
