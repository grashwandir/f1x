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

import org.f1x.api.message.IMessageParser;
import org.f1x.api.FixSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.session.SessionStatus;
import org.f1x.io.InputChannel;
import org.f1x.io.InputStreamChannel;
import org.f1x.io.OutputChannel;
import org.f1x.io.OutputStreamChannel;
import org.f1x.api.message.MessageBuilder;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import org.f1x.api.session.FixSessionListener;

public abstract class FixSocketCommunicator<T extends FixSessionListener<IMessageParser, MessageBuilder>> extends FixCommunicator<IMessageParser, MessageBuilder, T> {

    public FixSocketCommunicator(FixVersion fixVersion, FixSettings settings) {
        super(
                new DefaultMessageParser(),
                new DefaultMessageParser(),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision()),
                fixVersion,
                settings);
    }

    protected void connect (Socket socket) throws IOException {
        if (LOGGER.isInfoEnabled()) {
            SocketAddress address = socket.getRemoteSocketAddress();
            LOGGER.info().append("Connected to ").append(address).commit();
        }

        final FixSettings settings = getSettings();

        socket.setTcpNoDelay(settings.isSocketTcpNoDelay());
        socket.setKeepAlive(settings.isSocketKeepAlive());
        socket.setSoTimeout(settings.getSocketTimeout());
        socket.setSendBufferSize(settings.getSocketSendBufferSize());
        socket.setReceiveBufferSize(settings.getSocketRecvBufferSize());

        setSessionStatus(SessionStatus.SocketConnected);
        connect(getInputChannel(socket), getOutputChannel(socket));
    }

    protected InputChannel getInputChannel (Socket socket) throws IOException {
        return new InputStreamChannel(socket.getInputStream());
    }

    protected OutputChannel getOutputChannel (Socket socket) throws IOException {
        return new OutputStreamChannel(socket.getOutputStream());
    }

    @Override
    public MessageBuilder createMessageBuilder() {
        final FixSettings settings = getSettings();
        return new ByteBufferMessageBuilder(settings.getMaxOutboundMessageSize(), settings.getDoubleFormatterPrecision());
    }

}
