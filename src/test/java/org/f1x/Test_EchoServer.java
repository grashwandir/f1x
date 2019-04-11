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
package org.f1x;

import org.f1x.api.FixAcceptorSettings;
import org.f1x.api.FixInitiatorSettings;
import org.f1x.api.FixVersion;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.Tools;
import org.f1x.api.message.fields.*;
import org.f1x.api.session.FixSession;
import org.f1x.api.session.SessionID;
import org.f1x.api.session.SessionStatus;
import org.f1x.tools.EchoServer;
import org.f1x.v1.FixSessionInitiator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.f1x.api.message.IMessageParser;
import org.f1x.api.session.InitiatorFixSessionListener;

/** Verifies that simple FIX server and client can exchange messages */
public class Test_EchoServer extends  TestCommon {
    private static final String INITIATOR_SENDER_ID = "INITIATOR";
    private static final String ACCEPTOR_SENDER_ID = "ACCEPTOR";


    /** Client and server connect, exchange 15 messages and disconnect */
    @Test(timeout = 120000)
    public void simpleMessageLoop() throws InterruptedException, IOException {

        final EchoServer server = new EchoServer(7890, new SessionIDBean(ACCEPTOR_SENDER_ID, INITIATOR_SENDER_ID), new FixAcceptorSettings());
        final EchoServerClient client = new EchoServerClient ("localhost", 7890, new SessionIDBean(INITIATOR_SENDER_ID, ACCEPTOR_SENDER_ID), 3);


        final Thread acceptorThread = new Thread(server, "EchoServer");
        acceptorThread.start();

        final Thread initiatorThread = new Thread(client, "EchoClient");
        initiatorThread.start();

        if ( ! client.messageCount.await(15, TimeUnit.SECONDS))
            Assert.fail("Communication failed (timed out waiting for echo)");

        Assert.assertEquals(SessionStatus.ApplicationConnected, client.getSessionStatus());

        normalClose(client, server);
    }

    /** Test verifies automatic re-connection after abrupt disconnect */
    @Test(timeout = 120000)
    public void loginAfterDisconnect() throws InterruptedException, IOException {

        final EchoServer server = new EchoServer(7890, new SessionIDBean(ACCEPTOR_SENDER_ID, INITIATOR_SENDER_ID), new FixAcceptorSettings());
        final FixSessionInitiator<InitiatorFixSessionListener> client = new FixSessionInitiator<>("localhost", 7890, FixVersion.FIX44, new SessionIDBean(INITIATOR_SENDER_ID, ACCEPTOR_SENDER_ID), new FixInitiatorSettings());


        final Thread acceptorThread = new Thread(server, "Server");
        acceptorThread.start();

        final Thread initiatorThread = new Thread(client, "Client");
        initiatorThread.start();


        if ( ! spinWaitSessionStatus(client, SessionStatus.ApplicationConnected, 15000))
            Assert.fail("Timed out waiting for the first FIX session to establish");

        client.disconnect("*** Reconnect Test ***");

        if ( ! spinWaitSessionStatus(client, SessionStatus.Disconnected, 15000))
            Assert.fail("Timed out waiting for the FIX session to go down");

        if ( ! spinWaitSessionStatus(client, SessionStatus.ApplicationConnected, 35000))
            Assert.fail("Timed out waiting for the FIX session to re-establish");

        client.disconnect("End of test");
        client.close();
        server.close();
    }


    /** Test verifies automatic re-connection after abrupt disconnect */
    @Test(timeout = 120000)
    public void repeatedDisconnect() throws InterruptedException, IOException {

        final EchoServer server = new EchoServer(7890, new SessionIDBean(ACCEPTOR_SENDER_ID, INITIATOR_SENDER_ID), new FixAcceptorSettings());
        final FixSessionInitiator<InitiatorFixSessionListener> client = new FixSessionInitiator<>("localhost", 7890, FixVersion.FIX44, new SessionIDBean(INITIATOR_SENDER_ID, ACCEPTOR_SENDER_ID), new FixInitiatorSettings());


        final Thread acceptorThread = new Thread(server, "Server");
        acceptorThread.start();

        final Thread initiatorThread = new Thread(client, "Client");
        initiatorThread.start();


        if ( ! spinWaitSessionStatus(client, SessionStatus.ApplicationConnected, 15000))
            Assert.fail("Timed out waiting for the first FIX session to establish");

        client.disconnect("*** Reconnect Test ***");

        if ( ! spinWaitSessionStatus(client, SessionStatus.Disconnected, 15000))
            Assert.fail("Timed out waiting for the FIX session to go down");

        if ( ! spinWaitSessionStatus(client, SessionStatus.ApplicationConnected, 35000))
            Assert.fail("Timed out waiting for the FIX session to re-establish");

        client.disconnect("End of test");
        client.close();
        server.close();
    }

    //TODO: Test reconnect after socket kill

    //TODO: Test Scheduled disconnect

    private boolean spinWaitSessionStatus(FixSession<?, ?, ?> session, SessionStatus expectedStatus, long timeout) throws InterruptedException {
        final long timeoutTime = System.currentTimeMillis() + timeout;
        while (true) {
            if (session.getSessionStatus() == expectedStatus)
                return true;

            if (System.currentTimeMillis() > timeoutTime)
                return false;
            Thread.yield();
        }

    }

    private void normalClose (EchoServerClient client, EchoServer server) throws InterruptedException {
        Assert.assertEquals(SessionStatus.ApplicationConnected, client.getSessionStatus());

        client.close();

        client.awaitDisconnect();

        Assert.assertEquals(SessionStatus.Disconnected, client.getSessionStatus());

        server.close();
    }



    private static class EchoServerClient extends FixSessionInitiator<InitiatorFixSessionListener> {
        private final CountDownLatch messageCount;
        private final MessageBuilder mb;
        private final Object disconnectedSignal = new Object();

        public EchoServerClient(String host, int port, SessionID sessionID, int numberOfMessagesToSend) {
            super(host, port, FixVersion.FIX44, sessionID, new FixInitiatorSettings());

            messageCount = new CountDownLatch(numberOfMessagesToSend);
            mb = createMessageBuilder();
        }

        public void sendMessage () {
            assert getSessionStatus() == SessionStatus.ApplicationConnected;
            try {
                mb.clear();
                mb.setMessageType(MsgType.ORDER_SINGLE);
                mb.add(FixTags.ClOrdID, 123);
                mb.add(FixTags.HandlInst, HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE);
                mb.add(FixTags.OrderQty, 1);
                mb.add(FixTags.OrdType, OrdType.LIMIT);
                mb.add(FixTags.Price, 1.43);
                mb.add(FixTags.Side, Side.BUY);
                mb.add(FixTags.Symbol, "EUR/USD");
                mb.add(FixTags.SecurityType, SecurityType.FOREIGN_EXCHANGE_CONTRACT);
                mb.add(FixTags.TimeInForce, TimeInForce.DAY);
                mb.add(76, "MARKET-FEED-SIM");
                mb.add(FixTags.ExDestination, "#CANCEL-AFTER-OPEN");
                mb.addUTCTimestamp(FixTags.TransactTime, System.currentTimeMillis());
                doSend(mb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onSessionStatusChanged(SessionStatus oldStatus, SessionStatus newStatus) {
            super.onSessionStatusChanged(oldStatus, newStatus);
            switch (newStatus) {
                case ApplicationConnected:
                    final int cnt = (int)messageCount.getCount();
                    for (int i = 0; i < cnt; i++)
                        sendMessage();
                    break;
                case Disconnected:
                    synchronized (disconnectedSignal) {
                        disconnectedSignal.notify();
                    }
                    break;
            }
        }

        void awaitDisconnect() throws InterruptedException {
            while(getSessionStatus() != SessionStatus.Disconnected) {
                synchronized (disconnectedSignal) {
                    disconnectedSignal.wait();
                }
            }
        }

        @Override
        protected void processInboundAppMessage(CharSequence msgType, int msgSeqNum, boolean possDup, final IMessageParser parser) throws IOException {
            if (Tools.equals(MsgType.ORDER_SINGLE, msgType)) {
                messageCount.countDown();
            } else {
                super.processInboundAppMessage(msgType, msgSeqNum, possDup, parser);
            }
        }

    }

}
