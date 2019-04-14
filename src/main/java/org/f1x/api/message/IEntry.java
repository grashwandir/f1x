package org.f1x.api.message;

import org.f1x.util.ByteArrayReference;


/**
 *
 * @author niels.kowalski
 * @param <T>
 */
public interface IEntry {

    /**
     * @return current tag number (processed by last call to {@link #next()}
     */
    int getTagNum();

    /**
     * @return value of current tag interpreted as Boolean ('Y' = true; 'N' =
     * false}.
     */
    boolean getBoolean();

    void getByteSequence(final ByteArrayReference seq);

    byte getByte();

    /**
     * @return value of current tag as CharSequence. Note: caller must save
     * result because returned object will be reused for other tags. (Flyweight
     * pattern).
     */
//    CharSequence getCharSequence();
    /**
     * @return value of current tag interpreted as double number. Some loss of
     * precision may occur when converting fixed-point number to floating point
     * result.
     */
    double getDouble();

    float getFloat();

    /**
     * @return value of current tag interpreted as INT32 number.
     */
    int getInt();

    /**
     * @return value of current tag interpreted as LocalMktDate (in YYYYMMDD
     * format). Result is UTC timestamps which will have specified
     * year/month/day in local timezone
     */
    long getLocalMktDate();

    /**
     * @return value of current tag interpreted as LocalMktDate (in YYYYMMDD
     * format). Result is a decimal number with digits matching YYYYMMDD.
     */
    int getLocalMktDate2();

    /**
     * @return value of current tag interpreted as INT64 number.
     */
    long getLong();

    /**
     * Appends current value to given string builder
     *
     * @param appendable
     */
//    void getStringBuilder(StringBuilder appendable);

    /**
     * @return value of current tag as String (WARNING: Allocates memory!)
     */
    String getString();

    /**
     * @return value of current tag interpreted as UTC Date (in YYYYMMDD format)
     */
    long getUTCDateOnly();

    /**
     * @return number of millisecond since midnight if current tag contains
     * time-of-day or UTCTimeOnly (in either HH:MM:SS (whole seconds) or
     * HH:MM:SS.sss (milliseconds) format)
     */
    int getUTCTimeOnly();

    /**
     * @return value of current tag interpreted as UTC Time and Date combination
     * (in either YYYYMMDD-HH:MM:SS (whole seconds) or YYYYMMDD-HH:MM:SS.sss
     * (milliseconds) format)
     */
    long getUTCTimestamp();

    /**
     * @param constant * @return true if value of the last processed tag equals
     * to given byte array
     * @return
     */
    boolean isValueEquals(final byte[] constant);
}
