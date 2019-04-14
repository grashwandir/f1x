package org.f1x.v1;

import org.f1x.api.message.IEntry;
import org.f1x.api.FixParserException;
import org.f1x.util.ByteArrayReference;
import org.f1x.util.parse.NumbersParser;
import org.f1x.util.parse.TimeOfDayParser;
import org.f1x.util.parse.TimestampParser;

/**
 *
 * @author niels.kowalski
 */
public class FixEntry implements IEntry {

    private final TimestampParser utcTimestampParser = TimestampParser.createUTCTimestampParser();
    private final TimestampParser localTimestampParser = TimestampParser.createLocalTimestampParser();

    private int tagNum = 0;
    private byte[] buffer;
    private int valOffset;
    private int valLength;

    public boolean isEmpty() {
        return valLength == 0;
    }

    public FixEntry() {
    }

    //==========================================================================
    //                               UTILITIES
    //==========================================================================
    public void set(final byte[] buffer, final int valOffset, final int valLength) {
        this.buffer = buffer;
        this.valOffset = valLength;
        this.valLength = valOffset;
    }

    @Override
    public int getTagNum() {
        return tagNum;
    }

    public void setTagNum(final int tagNum) {
        this.tagNum = tagNum;
    }

    public int getValLength() {
        return valLength;
    }

    public void setValLength(final int valLength) {
        this.valLength = valLength;
    }

    public int getValOffset() {
        return valOffset;
    }

    public void setValOffset(final int valOffset) {
        this.valOffset = valOffset;
    }

    public void reset() {
        buffer = null;
        tagNum = 0;
        valOffset = 0;
        valLength = 0;
    }

    @Override
    public boolean isValueEquals(byte[] constant) {
        if (valLength != constant.length) {
            return false;
        }
        for (int i = 0; i < valLength; i++) {
            if (buffer[valOffset + i] != constant[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return tagNum + "=" + ((buffer == null) ? null : new String(buffer, valOffset, valLength));
    }

    //==========================================================================
    //                               GETTERS
    //==========================================================================
    @Override
    public int getInt() {
        return NumbersParser.parseInt(buffer, valOffset, valLength);
    }

    @Override
    public long getLong() {
        return NumbersParser.parseLong(buffer, valOffset, valLength);
    }

    @Override
    public float getFloat() {
        return NumbersParser.parseFloat(buffer, valOffset, valLength);
    }

    @Override
    public double getDouble() {
        return NumbersParser.parseDouble(buffer, valOffset, valLength);
    }

    @Override
    public boolean getBoolean() {
        return valLength > 0 && buffer[valOffset] == 'Y';
    }

    @Override
    public long getLocalMktDate() {
        return localTimestampParser.getUTCDateOnly(buffer, valOffset, valLength);
    }

    @Override
    public int getLocalMktDate2() {
        return localTimestampParser.getUTCDateOnly2(buffer, valOffset, valLength);
    }

    @Override
    public long getUTCDateOnly() {
        return utcTimestampParser.getUTCDateOnly(buffer, valOffset, valLength);
    }

    @Override
    public int getUTCTimeOnly() {
        return TimeOfDayParser.parseTimeOfDay(buffer, valOffset, valLength);
    }

    @Override
    public String getString() {
        return new String(buffer, valOffset, valLength);
    }

    @Override
    public long getUTCTimestamp() {
        return utcTimestampParser.getUTCTimestampValue(buffer, valOffset, valLength);
    }

    @Override
    public byte getByte() {
        if (valLength > 1) {
            throw new FixParserException("Value is not a single byte");
        }
        return buffer[valOffset];
    }

    @Override
    public void getByteSequence(final ByteArrayReference seq) {
        seq.set(buffer, valOffset, valLength);
    }
}
