package org.f1x.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import org.augur.f1x.log.LogFormatterFix;
import org.f1x.api.FixException;
import org.f1x.api.message.IEntry;
import org.f1x.api.message.IInputMessage;
import org.f1x.api.message.IWalkable;
import org.f1x.api.message.IWalker;
import org.f1x.io.InputChannel;
import org.f1x.log.GFLoggerMessageLogFactory;
import org.f1x.log.MessageLog;
import org.f1x.log.file.LogUtils;
import org.f1x.util.AsciiUtils;

public class InputStreamParser {

    public static final MessageLog LOGGER;

    static {
        LOGGER = (new GFLoggerMessageLogFactory(new LogFormatterFix("IN", "OUT"))).create(null);
    }
    public static final int CHECKSUM_LENGTH = 7; // length("10=123|") --check sum always expressed using 3 digits
    public static final int MIN_FIX_MESSAGE_LENGTH = 63; // min message example: 8=FIX.4.?|9=??|35=?|34=?|49=?|56=?|52=YYYYMMDD-HH:MM:SS|10=???|

//    private final InputStream input;
    private byte[] buffer;
    private int position;
    private int bytesRead;
    private int maxLength;

    public InputStreamParser(final byte[] buffer) {
//        this.input = input;
        this.buffer = buffer;
        maxLength = 0;
        bytesRead = 0;
        position = 0;
    }

    public InputStreamParser(final int initialBufferSize) {
        this(new byte[initialBufferSize]);
    }

    public InputStreamParser() {
        this(4096);//65535
    }

    public static void main(String[] args) throws Exception {
        testWalker();
    }

    private static void testWalker() throws IOException {
        final String PATTERN1 = " IN <<< ";
        final String PATTERN2 = "ENNK_MD <<< ";
        LogUtils.configure();
//        FIXLogFormatter formatter = new FIXLogFormatter("IN", "OUT");
        //======================================================================`
        int bit = 1;
        int length = 101;
        System.out.println("length=" + length + ", bit=" + bit);
        length = length >>> bit;
        System.out.println("length >>> bit = " + length);
        //======================================================================

        int test = 1;
        System.out.println("bef: " + (++test));
        test = 1;
        System.out.println("aft: " + (test++));
        //======================================================================

        final IInputMessage message = new MessageWalkable();

        final IWalker walker = new IWalker() {

            StringBuilder sb = null;
            int i = 0;

            @Override
            public void onWalkBegin(final IWalkable msg) throws FixException {
                sb = new StringBuilder();
            }

            @Override
            public boolean onField(final IWalkable msg, final IEntry value) throws FixException {
                sb.append(value.getTagNum()).append("=").append(value.getString());//.append(value.getString());
//                LOGGER.info(new StringBuilder("CHECK: ").append(i++).append(" ").append(sb.toString()).toString());
                return true;
            }

            @Override
            public void onWalkFinish(final IWalkable msg) throws FixException {
                sb = null;
            }
        };

        final File testFile = new File("/Users/niels.kowalski/tt-2019-03-29-copy.log");

        try (FileReader reader = new FileReader(testFile); BufferedReader buffReader = new BufferedReader(reader, 2048)) {

            final InputStream input = new InputStream() {
                int index = -1;
                byte[] bytes = null;

                @Override
                public int read() throws IOException {
                    while (bytes == null) {
                        final String line = buffReader.readLine();
                        if (line == null) {
                            return -1;
                        }
                        if (line.startsWith(PATTERN1)) {
                            index = PATTERN1.length();
                        } else if (line.startsWith(PATTERN2)) {
                            index = PATTERN2.length();
                        } else {
                            continue;
                        }
                        LOGGER.log(true, line);
                        bytes = line.getBytes();
                    }
                    final byte b = bytes[index];
                    index++;
                    if (index == bytes.length) {
                        bytes = null;
                    }
                    if (b == ';') {
                        return AsciiUtils.SOH;
                    } else {
                        return b;
                    }
                }
            }; // end of anonymous class InputStream instance

            final MessageWalkable msgParser = new MessageWalkable();
            final InputStreamParser parser = new InputStreamParser();

            while (parser.next(null, msgParser)) {
                message.walk(walker);
            }
            LOGGER.log(true, "FINISHED!");
        }
    }
    //==========================================================================
    //                              WALKING
    //==========================================================================

    public boolean next(final InputChannel input, final MessageWalkable parser) throws IOException {
        parser.reset();
        int startPosition = position;
        while (true) {
            if (bytesRead == 0) {
                bytesRead = input.read(buffer, position, buffer.length - position);
                if (bytesRead == -1) {
                    return false;
                }
            } else {
                final int remainingMsg = parser.parse(buffer, startPosition, position, bytesRead);
                final int parsedLength = parser.length();

                if (parsedLength > maxLength) {
                    maxLength = parsedLength;
                }

                if (remainingMsg == 0) {
                    position = 0;
                    bytesRead = 0;
                    return true;
                }

                if (remainingMsg > 0) {
                    position += bytesRead - remainingMsg;
                    bytesRead = remainingMsg;
                    return true;
                }

                final int halfBuff = Math.max(buffer.length >>> 1, 1);
                final int leftLen = Math.abs(maxLength - parsedLength);
                final int fullLen = parsedLength + leftLen;
                final byte[] to = (fullLen >= halfBuff) ? new byte[fullLen << 1] : buffer;
                // should append if:
                // - no bytes read by parser
                // - bytes read but message process not finished
                // move processed part of buffer to beginning
                System.arraycopy(buffer, startPosition, to, 0, parsedLength);
                buffer = to;
                startPosition = 0;
                position = parsedLength;
                bytesRead = 0;
            }
        }
    }

    //==========================================================================
    //                                 GARBAGE
    //==========================================================================
//    private boolean nextOriginal(final IMessageParser message) throws IOException {
//        message.reset();
//        int startPosition = position;
//        while (true) {
//            if (bytesRead == 0) {
//                bytesRead = input.read(buffer, position, buffer.length - position);
//                if (bytesRead == -1) {
//                    return false;
//                }
//            } else {
//                final int leftAfterParse = message.parse(buffer, startPosition, position, bytesRead);
//                final int parsedLen = message.length();
//                if (parsedLen > maxLength) {
//                    maxLength = parsedLen;
//                }
//                if (leftAfterParse == 0) {
//                    position = 0;
//                    bytesRead = 0;
//                    return true;
//                }
//                if (leftAfterParse > 0) {
//                    position += bytesRead - leftAfterParse;
//                    bytesRead = leftAfterParse;
//                    return true;
//                }
//                final int halfBuff = Math.max(buffer.length >>> 1, 1);
//                final int leftLen = Math.abs(maxLength - parsedLen);
//                final int fullLen = parsedLen + leftLen;
//                final byte[] to = (fullLen >= halfBuff) ? new byte[fullLen << 1] : buffer;
//                System.arraycopy(buffer, startPosition, to, 0, parsedLen);
//                buffer = to;
//                startPosition = 0;
//                position = parsedLen;
//                bytesRead = 0;
//            }
//        }
//    }
//
//        static void testIterator() throws IOException {
//        LogUtils.configure();
//        //======================================================================`
//
//        final File testFile = new File("/Users/niels.kowalski/tt-2019-03-29-copy.log");
//
//        try (FileReader reader = new FileReader(testFile); BufferedReader buffReader = new BufferedReader(reader, 2048)) {
//
//            final InputStream input = new InputStream() {
//                int index = -1;
//                byte[] bytes = null;
//
//                @Override
//                public int read() throws IOException {
//                    while (bytes == null) {
//                        final String line = buffReader.readLine();
//                        LOGGER.debug(line);
//                        if (line == null) {
//                            return -1;
//                        }
//                        if (line.startsWith(PATTERN1)) {
//                            index = PATTERN1.length();
//                        } else if (line.startsWith(PATTERN2)) {
//                            index = PATTERN2.length();
//                        } else {
//                            continue;
//                        }
//
//                        bytes = line.getBytes();
//                    }
//                    final byte b = bytes[index];
//                    index++;
//                    if (index == bytes.length) {
//                        bytes = null;
//                    }
//                    if (b == ';') {
//                        return AsciiUtils.SOH;
//                    } else {
//                        return b;
//                    }
//                }
//            }; // end of anonymous class InputStream instance
//
//            final InputStreamParser parser = new InputStreamParser(input);
//            final MessageIterator it = parser.iterator;
//
//            while (parser.next()) {
//                final StringBuilder sb = new StringBuilder("PARSED: ");
//                try {
//                    while (it.hasNext()) {
//                        final IEntry entry = it.next();
//                        sb.append(entry.toString()).append(";");
////                        System.out.println(sb);
//                    }
////                    System.out.println("");
//                } finally {
//                    LOGGER.log(true, sb.toString());
//                }
//            }
//            LOGGER.info("FINISHED!");
//        }
//    }
}
