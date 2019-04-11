/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.f1x.api.session;

import org.f1x.api.FixSettings;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.MessageParser;
import org.f1x.api.message.fields.MsgType;

import java.io.IOException;

public interface FixSession<M extends MessageParser, B extends MessageBuilder, T extends SessionListener<M, B>> extends Runnable {

    void setEventListener(T eventListener);

    SessionID getSessionID();

    FixSettings getSettings();

    SessionStatus getSessionStatus();

    B createMessageBuilder();

//    void send (MessageBuilder mb) throws IOException;
    void send(B messageBuilder) throws IOException;

    void send(final MsgType msgType, B messageBuilder) throws IOException;

    void send(final CharSequence msgType, B messageBuilder) throws IOException;

    /**
     * Terminate socket connection immediately (no LOGOUT message is sent if
     * session is in process). Session enters
     * {@link org.f1x.api.session.SessionStatus#Disconnected} state. Initiator
     * will try to re-connect after little delay (delay is configurable, subject
     * to session schedule).
     */
    void disconnect(CharSequence cause);

    /**
     * Send LOGOUT but do not drop socket connection. Session enters
     * {@link org.f1x.api.session.SessionStatus#InitiatedLogout} state.
     *
     * @param cause Logout cause, can be null.
     */
    void logout(CharSequence cause);

    /**
     * Send LOGOUT (if needed) and terminate socket connection.
     */
    void close(/*long timeout*/);
}
