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
package org.f1x.v1;

import org.f1x.api.FixSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.session.SessionID;
import org.f1x.api.session.SessionStatus;
import org.f1x.io.InputChannel;
import org.f1x.io.OutputChannel;
import org.f1x.util.TimeSource;
import org.f1x.api.session.FixSessionListener;

class TestFixCommunicator extends FixCommunicator<IMessageParser, MessageBuilder, FixSessionListener<IMessageParser, MessageBuilder>> {

    private final SessionID sessionID;

    public TestFixCommunicator(SessionID sessionID, TimeSource timeSource, InputChannel in, OutputChannel out) {
        this(new FixSettings(), sessionID, timeSource, in, out);
    }

    public TestFixCommunicator(FixSettings settings, SessionID sessionID, TimeSource timeSource, InputChannel in, OutputChannel out) {
//        super(FixVersion.FIX44, new FixSettings(), timeSource);
        super(
                new DefaultMessageParser(),
                new DefaultMessageParser(),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                FixVersion.FIX44,
                settings,
                timeSource);
        this.sessionID = sessionID;

        connect(in, out);
        setSessionStatus(SessionStatus.ApplicationConnected);
    }

    public TestFixCommunicator(SessionID sessionID, TimeSource timeSource) {
        this(new FixSettings(), sessionID, timeSource);
    }

    public TestFixCommunicator(FixSettings settings, SessionID sessionID, TimeSource timeSource) {
//        super(FixVersion.FIX44, new FixSettings(), timeSource);
        super(
                new DefaultMessageParser(),
                new DefaultMessageParser(),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                FixVersion.FIX44,
                settings,
                timeSource);
        this.sessionID = sessionID;
    }

    @Override
    public SessionID getSessionID() {
        return sessionID;
    }

    @Override
    public void run() {
    }

    @Override
    public MessageBuilder createMessageBuilder() {
        final FixSettings settings = getSettings();
        return new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision());
    }

    @Override
    protected void beforeLogonSent(MessageBuilder messageBuilder) {
    }
}
