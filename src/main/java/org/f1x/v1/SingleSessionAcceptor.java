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

import org.f1x.api.session.AcceptorFixSessionListener;

import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

public class SingleSessionAcceptor extends AbstractSessionAcceptor {

    protected static final int CONNECTION_QUEUE_SIZE = 1;

    private final FixSessionAcceptor<AcceptorFixSessionListener> acceptor;

    public SingleSessionAcceptor(String host, int port, FixSessionAcceptor<AcceptorFixSessionListener> acceptor) {
        super(host, port, CONNECTION_QUEUE_SIZE);
        this.acceptor = acceptor;
    }

    @Override
    protected void processConnection(Socket socket) {
        try {
            acceptor.connect(socket);
            acceptor.run();
        } catch (Exception e) {
            LOGGER.warn().append("Error processing inbound connection: ").append(e.getMessage()).append(e).commit();
            closeSocket(socket);
        }
    }

    @Override
    public synchronized void close() {
        if (active) {
            super.close();
            acceptor.close();
        }
    }

}
