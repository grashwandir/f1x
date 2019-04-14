package org.augur.f1x.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.f1x.api.session.SessionID;
import org.f1x.log.LogFormatter;
import org.f1x.log.MessageLog;

/**
 *
 * @author niels.kowalski
 */
public class JULMsgLog implements MessageLog {

//    private static final ThreadLocal<JULLogEntry> ENTRY;
    protected static final java.util.logging.Level INTERNAL_LVL = java.util.logging.Level.ALL;
    protected final LogFormatter formatter;
    protected java.util.logging.Level lvl = java.util.logging.Level.INFO;
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private final java.util.logging.Logger logger;

    JULMsgLog(final java.util.logging.Logger logger, final LogFormatter formatter) {
        this.logger = logger;
        this.formatter = formatter;
    }

    JULMsgLog(final java.util.logging.Logger logger, final SessionID sessionID) {
        this(logger, new LogFormatterFix(sessionID));
    }

//    static {
//        ENTRY = new ThreadLocal<JULLogEntry>() {
//            @Override
//            protected JULLogEntry initialValue() {
//                return new JULLogEntry();
//            }
//        };
//    }

    @Override
    public void log(boolean isInbound, byte[] buffer, int offset, int length) {
        os.reset();
        try {
            final int count = formatter.log(isInbound, buffer, offset, length, os);
            logger.log(lvl, os.toString("US-ASCII"));
        } catch (IOException ex) {
            if (logger.isLoggable(INTERNAL_LVL)) {
                logger.log(INTERNAL_LVL, "Formatting log entry", ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
    }
}
