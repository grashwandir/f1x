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

package org.f1x.tools;

import org.f1x.SessionIDBean;
import org.f1x.api.FixAcceptorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.session.SessionID;
import org.f1x.v1.FixSessionAcceptor;
import org.f1x.v1.SingleSessionAcceptor;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.session.AcceptorFixSessionListener;

import java.io.IOException;
import org.f1x.log.file.LogUtils;

/** Receives inbound FIX messages and does nothing else */
public class NullServer extends SingleSessionAcceptor {

    public NullServer(int bindPort, SessionID sessionID, FixAcceptorSettings settings) {
        this(null, bindPort, sessionID, settings);
    }

    public NullServer(String host, int bindPort, SessionID sessionID, FixAcceptorSettings settings) {
        super(host, bindPort, new NullServerSessionAcceptor(FixVersion.FIX44, sessionID, settings));
    }

    private static class NullServerSessionAcceptor extends FixSessionAcceptor<AcceptorFixSessionListener> {

        public NullServerSessionAcceptor(FixVersion fixVersion, SessionID sessionID, FixAcceptorSettings settings) {
            super(fixVersion, sessionID, settings);
        }

        @Override
        protected void processInboundAppMessage(CharSequence msgType, int msgSeqNum, boolean possDup, IMessageParser parser) throws IOException {
            // do nothing
        }
    }

    public static void main (String [] args) throws InterruptedException, IOException {
        LogUtils.configure();

        int port = Integer.parseInt(args[0]);
        String host = (args.length > 1) ? args[1] : null;

        LOGGER.info().append("Null Server : ").append(port).commit();

        FixAcceptorSettings settings = new FixAcceptorSettings();
        settings.setSocketSendBufferSize(256*1024);
        settings.setSocketRecvBufferSize(256*1024);
        settings.setSocketTcpNoDelay(true);
        final NullServer server = new NullServer(host, port, new SessionIDBean("CLIENT", "SERVER"), settings);

        final Thread acceptorThread = new Thread(server, "NullServer");
        acceptorThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info().append("Exiting...").commit();
                server.close();
            }
        });
    }
}
