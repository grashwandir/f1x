package org.f1x.api.session;

import org.f1x.v1.FixSessionAcceptor;

public interface SessionManager<T extends AcceptorFixSessionListener> {

    void addSession(FixSessionAcceptor<T> acceptor);

    FixSessionAcceptor<T> removeSession(SessionID sessionID);

    FixSessionAcceptor<T> getSession(SessionID sessionID);

    FixSessionAcceptor<T> lockSession(SessionID sessionID) throws FailedLockException;

    FixSessionAcceptor<T> unlockSession(SessionID sessionID);

    void close();

}
