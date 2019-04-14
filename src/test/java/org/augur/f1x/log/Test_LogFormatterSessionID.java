package org.augur.f1x.log;

import org.f1x.SessionIDBean;
import org.f1x.api.session.SessionID;
import org.f1x.log.GFLoggerMessageLogFactory;
import org.f1x.log.LogFormatter;
import org.f1x.log.MessageLog;
import org.f1x.util.AsciiUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author niels.kowalski
 */
@Ignore
public class Test_LogFormatterSessionID {


    private final SessionID sessionID;
    private final LogFormatter logFormatter;
    private final GFLoggerMessageLogFactory logFactory;
    private final MessageLog log;

    private final static String MSG_LOGON = "8=FIX.4.4|9=70|35=A|34=1|49=SENDER|52=20140522-12:07:39.554|56=RECEIVER|141=Y|108=30|10=020|";

    public Test_LogFormatterSessionID() {
        sessionID = new SessionIDBean("SENDER", "TARGET");
        logFormatter = new LogFormatterFix(sessionID);
        logFactory = new GFLoggerMessageLogFactory(logFormatter);
        log = logFactory.create(null);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLog() throws Exception {
        final byte[] bytes = AsciiUtils.getBytes(new StringBuilder(MSG_LOGON.length() + 1).append(MSG_LOGON.replace('|', (char) 0x01)).append('\n').toString());
        logFormatter.log(true, bytes, 0, bytes.length, System.out);
        System.out.println("DONE");
        System.out.flush();
        //fail("The test case is a prototype.");
    }

}
