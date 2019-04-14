package org.f1x.api.message;

/**
 * Extends standard java.lang.Appendable with with a few additional methods. In
 * addition standard methods do not throw java.io.IOException.
 */
public interface IAppendableValue_1 extends Appendable {

    /**
     * Appends the specified character sequence to this
     * <tt>IAppendableValue</tt>.
     *
     * NOTE: CharSequence may contain ASCII characters only.
     *
     * <p>
     * Depending on which class implements the character sequence
     * <tt>csq</tt>, the entire sequence may not be appended. For instance, if
     * <tt>csq</tt> is a {@link java.nio.CharBuffer} then the subsequence to
     * append is defined by the buffer's position and limit.
     *
     * @param csq The character sequence to append. If <tt>csq</tt> is
     * <tt>null</tt>, then the four characters <tt>"null"</tt> are appended to
     * this IAppendableValue.
     *
     * @return A reference to this <tt>IAppendableValue</tt>
     */
    @Override
    IAppendableValue_1 append(CharSequence csq);

    /**
     * Appends a subsequence of the specified character sequence to this
     *  <tt>IAppendableValue</tt>.
     *
     * NOTE: CharSequence may contain ASCII characters only.
     *
     * <p>
     * An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the
     * same way as the invocation
     *
     *
     * <pre>
     *     out.append(csq.subSequence(start, end)) </pre>
     *
     * @param csq The character sequence from which a subsequence will be
     * appended. If <tt>csq</tt> is <tt>null</tt>, then characters will be
     * appended as if <tt>csq</tt> contained the four characters
     * <tt>"null"</tt>.
     *
     * @param start The index of the first character in the subsequence
     *
     * @param end The index of the character following the last character in the
     * subsequence
     *
     * @return A reference to this <tt>IAppendableValue</tt>
     */
    @Override
    IAppendableValue_1 append(CharSequence csq, int start, int end);

    /**
     * Appends the specified ASCII character to this <tt>IAppendableValue</tt>.
     *
     */
    @Override
    IAppendableValue_1 append(char c);

    IAppendableValue_1 append(byte c);

    IAppendableValue_1 append(int value);

    IAppendableValue_1 append(long value);

    IAppendableValue_1 append(double value);

    /**
     * Appends FIX tag separator (ASCII SOH character).
     */
    void end();
}
