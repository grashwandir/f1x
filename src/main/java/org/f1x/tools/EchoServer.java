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
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.FixTags;
import org.f1x.api.session.SessionID;
import org.f1x.log.NullLogFactory;
import org.f1x.v1.FixSessionAcceptor;
import org.f1x.v1.SingleSessionAcceptor;
import org.f1x.v1.schedule.SessionSchedule;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.session.AcceptorFixSessionListener;

import java.io.IOException;
import org.f1x.log.file.LogUtils;

/** Simple FIX acceptor that echos back all inbound application messages */
public class EchoServer extends SingleSessionAcceptor {

    public EchoServer(int bindPort, SessionID sessionID, FixAcceptorSettings settings) {
        this(null, bindPort, sessionID, settings, null);
    }

    public EchoServer(int bindPort, SessionID sessionID, FixAcceptorSettings settings, SessionSchedule schedule) {
        this(null, bindPort, sessionID, settings, schedule);
    }

    public EchoServer(String host, int bindPort, SessionID sessionID, FixAcceptorSettings settings, SessionSchedule schedule) {
        super(host, bindPort, createAcceptor(sessionID, settings, schedule));
    }

    private static EchoServerSessionAcceptor createAcceptor(SessionID sessionID, FixAcceptorSettings settings, SessionSchedule schedule) {
        EchoServerSessionAcceptor acceptor = new EchoServerSessionAcceptor(FixVersion.FIX44, sessionID, settings);
        acceptor.setSessionSchedule(schedule);
        return acceptor;
    }


    private static class EchoServerSessionAcceptor extends FixSessionAcceptor<AcceptorFixSessionListener> {
        private final MessageBuilder mb;

        public EchoServerSessionAcceptor(FixVersion fixVersion, SessionID sessionID, FixAcceptorSettings settings) {
            super(fixVersion, sessionID, settings);
            mb = createMessageBuilder();

            setMessageLogFactory(new NullLogFactory());
        }

        @Override
        protected void processInboundAppMessage(final CharSequence msgType, int msgSeqNum, boolean possDup, final IMessageParser parser) throws IOException {
            super.processInboundAppMessage(msgType, msgSeqNum, possDup, parser);
            mb.clear();
            mb.setMessageType(msgType.toString());

            while (parser.next()) {
                int tag = parser.getTagNum();
                if (tag != FixTags.MsgSeqNum) {
                    mb.add(tag, parser.getCharSequenceValue());
                }
            }
            doSend(mb); // Echo it back!
        }
    }

    public static void main (String [] args) throws InterruptedException, IOException {
        LogUtils.configure();
        if (args.length == 0)
            throw new IllegalArgumentException("Expecting one or two arguments: port and [optionally] host");
        int port = Integer.parseInt(args[0]);
        String host = (args.length > 1) ? args[1] : null;

        LOGGER.info().append("Echo Server : ").append(port).commit();


        FixAcceptorSettings settings = new FixAcceptorSettings();

        final EchoServer server = new EchoServer(host, port, new SessionIDBean("SERVER", "CLIENT"), settings, null);
        final Thread acceptorThread = new Thread(server, "EchoServer");
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
