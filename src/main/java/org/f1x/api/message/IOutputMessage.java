package org.f1x.api.message;

import org.f1x.api.message.fields.MsgType;
import org.f1x.api.message.types.ByteEnum;
import org.f1x.api.message.types.IntEnum;
import org.f1x.api.message.types.StringEnum;
import org.f1x.util.ByteArrayReference;

/**
 *
 * @author niels.kowalski
 */
public interface IOutputMessage {

    CharSequence getMessageType();

    IOutputMessage setMessageType(MsgType msgType);

    IOutputMessage setMessageType(CharSequence msgType);

    /**
     * Appends ASCII CharSequence tag value pair. Use
     * {@link #addRaw(int, byte[], int, int)} for non-ASCII content and don't
     * forget to specify MessageEncoding(347).
     *
     * @param value tag value as ASCII text (cannot be null)
     */
    IOutputMessage add(int tag, CharSequence value);

    /**
     * Appends ASCII CharSequence tag value pair. Use
     * {@link #addRaw(int, byte[], int, int)} for non-ASCII content and don't
     * forget to specify MessageEncoding(347).
     *
     * @param value tag value as ASCII text (cannot be null)
     * @param start The index of the first character in the subsequence
     * @param end The index of the character following the last character in the
     * subsequence
     */
    IOutputMessage add(int tag, CharSequence value, int start, int end);

    IOutputMessage add(int tag, long value);

    IOutputMessage add(int tag, int value);

    /**
     * Appends given double value formatted with default precision and rounded-up
     */
    IOutputMessage add(int tag, double value);

    /**
     * Appends given double value formatted with given precision rounded-up
     */
    IOutputMessage add(int tag, double value, int precision);

    /**
     * Appends given double value formatted with given precision rounded up or
     * down
     */
    IOutputMessage add(int tag, double value, int precision, boolean roundUp);

    IOutputMessage add(int tag, byte value);

    IOutputMessage add(int tag, boolean value);

    IOutputMessage add(int tag, ByteEnum value);

    IOutputMessage add(int tag, IntEnum value);

    IOutputMessage add(int tag, StringEnum value);

    /**
     * Adds UTCTimestamp field (in "yyyyMMdd-HH:mm:ss.SSS" format)
     */
    IOutputMessage addUTCTimestamp(int tag, long timestamp);

    /**
     * Adds UTCTimeOnly field (in "HH:mm:ss.SSS" format)
     */
    IOutputMessage addUTCTimeOnly(int tag, long timestamp);

    /**
     * Adds UTCDateOnly field (in "yyyyMMdd" format)
     */
    IOutputMessage addUTCDateOnly(int tag, long timestamp);

    /**
     * Adds LocalMktDate (in "yyyyMMdd" format)
     */
    IOutputMessage addLocalMktDate(int tag, long timestamp);

    /**
     * Adds LocalMktDate (in "yyyyMMdd" format)
     */
    IOutputMessage addLocalMktDate2(int tag, int yyyymmdd);

    /**
     * Copies value of given tag from the provided byte buffer
     */
    IOutputMessage addRaw(int tag, byte[] buffer, int offset, int length);

    /**
     * Copies value of given tag from the provided byte array reference
     */
    IOutputMessage addRaw(int tag, ByteArrayReference bytes);

    /**
     * Adds tag that with complex value. Since FIX doesn't allow empty values
     * caller is required to call one of {@link AppendableValue} method to
     * provide tag value. Also caller is required to call
     * {@link AppendableValue#end()} at the end. Example:
     * <pre>
     * mb.add (FixTags.ClOrdId).append("FXI").append(orderId++).end();
     * </pre>
     */
    IAppendableValue add(int tag);

    /**
     * Copy current content into given buffer
     */
    int output(byte[] buffer, int offset);

    /**
     * @return current length of message body (in bytes)
     */
    int getLength();

    /**
     * Clear current content before building content for a new message
     */
    IOutputMessage clear();
}
