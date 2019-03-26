package org.augur.f1x.log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.f1x.api.session.SessionID;
import org.f1x.log.LogFormatter;
import org.f1x.util.AsciiUtils;

/**
 *
 * @author niels.kowalski
 */
public class LogFormatterSessionID implements LogFormatter {

    public static final Charset CHARSET = StandardCharsets.US_ASCII;
    public static final byte[] PROMPT_IN = " <<< ".getBytes(CHARSET);
    public static final byte[] PROMPT_OUT = " >>> ".getBytes(CHARSET);
    private static final byte[] LINE_SEPARATOR = System.lineSeparator().getBytes(CHARSET);

    private final byte[] promptIn;
    private final byte[] promptOut;

    public LogFormatterSessionID(final byte[] prefix) {
        promptIn = new byte[PROMPT_IN.length + prefix.length];
        promptOut = new byte[PROMPT_OUT.length + prefix.length];
        for (int i = 0; i < promptIn.length; i++) {
            if (i < prefix.length) {
                promptIn[i] = prefix[i];
            } else {
                promptIn[i] = PROMPT_IN[i - prefix.length];
            }
        }
        for (int i = 0; i < promptOut.length; i++) {
            if (i < prefix.length) {
                promptOut[i] = prefix[i];
            } else {
                promptOut[i] = PROMPT_OUT[i - prefix.length];
            }
        }
    }

    public LogFormatterSessionID(final CharSequence prefix) {
        this(prefix.toString().getBytes(CHARSET));
    }

    public LogFormatterSessionID(final SessionID sessionID) {
        this(sessionID.getTargetCompId());
    }

    @Override
    public int log(boolean isInbound, byte[] buffer, int offset, int length, final OutputStream os) throws IOException {
        final byte[] prompt = isInbound ? promptIn : promptOut;
        final int totalLength = length + prompt.length + LINE_SEPARATOR.length;
        os.write(prompt, 0, prompt.length);
        for (int i = 0; i < length; i++) {
            byte b = buffer[offset + i];
            if (b == AsciiUtils.SOH) {
                b = 59;
            } else if (b == AsciiUtils.NL) {
                b = AsciiUtils.PIPE;
            }
            os.write(b);
        }
//        os.write(LINE_SEPARATOR, 0, LINE_SEPARATOR.length);
        return totalLength;
    }

}
