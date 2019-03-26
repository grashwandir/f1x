package org.augur.f1x.log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.f1x.log.LogFormatter;

/**
 *
 * @author niels.kowalski
 */
public class LogFormatterFix implements LogFormatter {

    public static final Charset CHARSET = StandardCharsets.US_ASCII;

    private final byte[] prefix;

    public LogFormatterFix(String prefix) {
        this.prefix = prefix.getBytes(CHARSET);
    }

    @Override
    public int log(boolean isInbound, byte[] buffer, int offset, int length, final OutputStream os) throws IOException {
        final int totalLength = length + prefix.length;
        os.write(prefix, 0, prefix.length);
        for (int i = 0; i < length; i++) {
            byte b = buffer[offset + i];
            if (b == 1) {
                b = 59;
            }
            os.write(b);
        }
        return totalLength;
    }

}
