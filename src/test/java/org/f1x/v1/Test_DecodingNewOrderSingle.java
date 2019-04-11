package org.f1x.v1;

import org.f1x.util.AsciiUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import quickfix.InvalidMessage;
import quickfix.Message;

public final class Test_DecodingNewOrderSingle {

    private static final int WARMUP = 20_000;
    private static final int N = 1_000_000;

    private static final String SAMPLE = "8=FIX.4.4|9=196|35=D|34=78|49=A12345B|50=2DEFGH4|52=20140603-11:53:03.922|56=COMPARO|57=G|142=AU,SY|1=AU,SY|11=4|21=1|38=50|40=2|44=400.5|54=1|55=OC|58=NIGEL|59=0|60=20140603-11:53:03.922|107=AOZ3 C02000|167=OPT|10=116|".replace('|', '\u0001');

    @Test
    public void testF1X() {
        System.out.println("Average time " + getDecodingTimeF1X() + " ns. per decoding");
    }

    @Test
    public void testQuickFIXJ() throws InvalidMessage {
        System.out.println("Average time " + getDecodingTimeQuickFIXJ() + " ns. per decoding");
    }

    @Test
    public void benchmarkWithQuickFIX() throws InvalidMessage {
        long f1xDecoding = getDecodingTimeF1X();
        System.out.println("Average F1X encoding time: " + f1xDecoding + " ns. per message");

        long qfixDecoding = getDecodingTimeQuickFIXJ();
        System.out.println("Average QuickFIX encoding time: " + qfixDecoding + " ns. per message");

        double expectedAdvantage = 3.0; // actually expect to be 3.9x better
        Assert.assertTrue (f1xDecoding*expectedAdvantage < qfixDecoding);
    }


    private static long getDecodingTimeF1X() {
        byte[] msg = AsciiUtils.getBytes(SAMPLE);
        DefaultMessageParser parser = new DefaultMessageParser();

        for (int i = 0; i < WARMUP; i++)
            decode(msg, parser);

        long start = System.nanoTime();
        for (int i = 0; i < N; i++)
            decode(msg, parser);

        long end = System.nanoTime();

        return (end - start) / N;
    }

    private static long getDecodingTimeQuickFIXJ() throws InvalidMessage {
        Message msg = new Message();

        for (int i = 0; i < WARMUP; i++)
            decode(SAMPLE, msg);

        long start = System.nanoTime();
        for (int i = 0; i < N; i++)
            decode(SAMPLE, msg);

        long end = System.nanoTime();

        return (end - start) / N;
    }

    private static void decode(String msgData, Message msg) throws InvalidMessage {
        msg.clear();
        msg.fromString(msgData, null, false);
    }

    private static int decode(byte[] msgData, DefaultMessageParser parser) {
        parser.set(msgData, 0, msgData.length);
        int dummy = 0;

        while (parser.next())
            dummy += parser.getCharSequenceValue().length();

        return dummy;
    }

    public static void main(String[] args) throws Exception {
        Test_DecodingNewOrderSingle test = new Test_DecodingNewOrderSingle();
        while (true) {
            test.testF1X();
            test.testQuickFIXJ();
        }
    }


}
