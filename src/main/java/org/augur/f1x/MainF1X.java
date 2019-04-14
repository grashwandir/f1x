package org.augur.f1x;

import org.f1x.api.FixVersion;
import org.f1x.api.message.fields.MDEntryType;
import org.f1x.log.file.LogUtils;
import org.f1x.store.InMemoryMessageStore;
import org.f1x.store.MessageStore;
import org.f1x.store.SafeMessageStore;

/**
 *
 * @author niels.kowalski
 */
public class MainF1X {

    public static void main(String[] args) {
        LogUtils.configure();

        final AugurInitiatorFixSessionConfig settings = new AugurInitiatorFixSessionConfig("ENNK_MD", "NKowalski", "CME", null);
        //must be superior to heartbeat
        settings.setSocketTimeout(60000);
        settings.setHeartBeatIntervalSec(30);
        settings.setHeartbeatCheckIntervalMs(100);
        settings.setResetSequenceNumbersOnEachLogon(true);
        settings.setSocketTcpNoDelay(true);
        settings.setMaxOutboundMessageSize(8192);
        settings.setMaxInboundMessageSize(8192);
        settings.setFixVersion(FixVersion.FIX42);
        settings.setHost("fixsecurityinfo-ext-uat-cert.trade.tt");
        settings.setPort(11503);
//        settings.setHost("127.0.0.1");
//        settings.setPort(3333);
        settings.setPassword("12345678");
        settings.setFixVersion(FixVersion.FIX42);

        final MessageStore messageStore = new SafeMessageStore(new InMemoryMessageStore(8192));

        final AugurFixSessionInitiator<AugurInitiatorFixSessionConfig> session = new AugurFixSessionInitiator<>(settings);
        session.setMessageStore(messageStore);

        session.mdBuilder.addInstrumentById("ES", "ESM9", "98", "CME", null);
        session.mdBuilder.addMDEntryTypes(MDEntryType.BID, MDEntryType.OFFER, MDEntryType.TRADE);
        //X7HGAHJFAOCC
//35=V;263=1;264=0;265=1;266=Y;146=1;55=ES;48=ESM9;22=98;207=CME;262=70504965;146=1;55=ES;48=ESM9;22=98;207=CME;10=203;
//35=V;263=1;264=1;265=1;267=1;269=Y;146=1;48=6722315072511943291;22=96;207=CME
//35=V;263=1;264=1;265=1;267=3;269=0;269=1;269=2;146=1;55=ES;48=ES2430R9;22=5;207=CME

//[2019-03-29 21:02:25.268 TRACE 	] ENNK_MD <<< 8=FIX.4.2;9=00216;35=X;49=CME;56=ENNK_MD;34=103;52=20190329-20:02:25.127;262=72118758;268=1;279=1;269=1;55=ES;167=FUT;200=201906;541=20190621;205=21;18211=M;48=4712986832916820417;207=CME;100=XCME;461=F;15=USD;270=284375;271=83;290=1;10=017;
session.run();
//        new Thread(session).start();
    }

}
