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
import org.f1x.api.FixVersion;
import org.f1x.api.session.SessionID;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.MessageParser;
import org.f1x.api.message.Tools;
import org.f1x.api.message.fields.*;
import org.f1x.api.message.types.ByteEnumLookup;
import org.f1x.api.session.SessionStatus;
import org.f1x.api.FixInitiatorSettings;
import org.f1x.v1.FixSessionInitiator;
import org.f1x.api.message.IMessageParser;

import java.io.IOException;
import org.f1x.api.session.InitiatorFixSessionListener;

/** A sample of FIX initiator that sends new orders every N seconds */
public class SimpleFixInitiator extends FixSessionInitiator<InitiatorFixSessionListener> {
    private final MessageBuilder mb;

    public SimpleFixInitiator(String host, int port, SessionID sessionID) {
        super(host, port, FixVersion.FIX44, sessionID, new FixInitiatorSettings());

        mb = createMessageBuilder();
    }

    public void sendNewOrder (long orderId) throws IOException {
        assert getSessionStatus() == SessionStatus.ApplicationConnected;
        synchronized (mb) {
            mb.clear();
            mb.setMessageType(MsgType.ORDER_SINGLE);
            mb.add(FixTags.ClOrdID, orderId);
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
        }
    }

    @Override
    protected void processInboundAppMessage(CharSequence msgType, int msgSeqNum, boolean possDup, final IMessageParser parser) throws IOException {
        if (Tools.equals(MsgType.EXECUTION_REPORT, msgType)) {
            processInboundExecutionReport(parser);
        } else
            super.processInboundAppMessage(msgType, msgSeqNum, possDup, parser);
    }

    private static final ByteEnumLookup<OrdStatus> ordStatusLookup = new ByteEnumLookup<>(OrdStatus.class);
    private static final ByteEnumLookup<ExecType> execTypeLookup = new ByteEnumLookup<>(ExecType.class);

    @SuppressWarnings("unused")
    private void processInboundExecutionReport(MessageParser parser) {
        long clOrdId = -1;
        OrdStatus ordStatus = null;
        ExecType execType = null;
        double lastQty = Double.NaN;
        double lastPrice = Double.NaN;
        double leavesQty = Double.NaN;

        while (parser.next()) {
            switch (parser.getTagNum()) {
                case FixTags.ClOrdID:
                    clOrdId = parser.getIntValue();
                    break;
                case FixTags.OrderID:
                    CharSequence orderId = parser.getCharSequenceValue();
                    break;
                case FixTags.OrdStatus:
                    ordStatus = ordStatusLookup.get(parser.getByteValue());
                    break;
                case FixTags.ExecType:
                    execType = execTypeLookup.get(parser.getByteValue());
                    break;
                case FixTags.LastPx:
                    lastPrice = parser.getDoubleValue();
                    break;
                case FixTags.LastQty:
                    lastQty = parser.getDoubleValue();
                    break;
                case FixTags.LeavesQty:
                    leavesQty = parser.getDoubleValue();
                    break;
            }
        }
    }

    protected void start () throws InterruptedException, IOException {
        Thread thread = new Thread(this);
        thread.start();

        while(true) {
            Thread.sleep(100);
            if (getSessionStatus() == SessionStatus.ApplicationConnected)
                break;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
                LOGGER.info().append("Exiting...").commit();
            }
        });

        long orderId = 1;
        while(true) {
            sendNewOrder(orderId++);
            Thread.sleep(1000);
        }

        //thread.join();
    }

    public static void main (String [] args) throws InterruptedException, IOException {
        final String host = (args.length > 0) ? args[0] : "localhost"; //192.168.1.105";
        final int port = (args.length > 1) ? Integer.parseInt(args[1]) : 2508;
        new SimpleFixInitiator(host, port, new SessionIDBean("CLIENT", "SERVER")).start();
    }

}
