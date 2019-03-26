package org.f1x.util.parse;

/**
 *
 * @author niels.kowalski
 */
public class Value implements CharSequence {

    public byte[] bytes;
    public int length;
    public int start;

    public boolean isEmpty() {
        return this.length == 0;
    }

    public int getInt() {
//        return TextUtil.parseInt(this, 0, this.length);
        return NumbersParser.parseInt(bytes, start, length);
    }

    public long getLong() {
//        return TextUtil.parseLong(this, 0, this.length);
        return NumbersParser.parseLong(bytes, start, length);
    }

    public float getFloat() {
//        return TextUtil.parseFloat(this, 0, this.length);
        return NumbersParser.parseFloat(bytes, start, length);
    }

    public double getDouble() {
        return NumbersParser.parseDouble(bytes, start, length);
    }

    public boolean getBoolean() {
        return this.length > 0 && this.bytes[this.start] == 89;
    }

    @Override
    public int length() {
        return this.length;
    }

    public Value set(final Value from) {
        this.bytes = from.bytes;
        this.length = from.length;
        this.start = from.start;
        return this;
    }

    @Override
    public char charAt(final int index) {
        return (char) this.bytes[this.start + index];
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new String(this.bytes, this.start + start, end - start);
    }

    @Override
    public String toString() {
        return (this.bytes == null) ? null : new String(this.bytes, this.start, this.length);
    }
}
