package org.f1x.v1;

import org.f1x.api.FixException;
import org.f1x.api.message.IInputMessage;
import org.f1x.api.message.IWalker;
import org.f1x.api.message.fields.FixTags;
import org.f1x.util.AsciiUtils;


/**
 *
 * @author niels.kowalski
 */
public class MessageWalkable implements IInputMessage {

    private static final byte FIELD_ID_READING_STATE = 1;
    private static final byte FIELD_VALUE_READING_STATE = 2;
    private static final byte NORMAL_STATE = 0;

    private final FixEntry entry = new FixEntry();
    private int state = NORMAL_STATE;
    private int[] fields;
    private int lastFieldIdx;
    private int bytesToRead;

    private byte[] message;
    private int start;
    private int length;

    public MessageWalkable() {
        this.fields = new int[384];
        reset();
    }

    public int parse(final byte[] buffer, final int startPosition, final int currentPosition, final int lengthFromCurrentPosition) throws FixException {
        message = buffer;
        start = startPosition;
        final byte[] currentMessage = buffer;
        int[] currentFields = fields;
        int currentLastFieldIdx = lastFieldIdx;
        int currentBytesToRead = bytesToRead;
        int currentState = state;
        int currentFieldId = -1;
        int currentFieldValueStart = -1;
        int currentFieldValueLength = -1;
        if (currentState != 0 && currentLastFieldIdx >= 0) {
            final int fldSetIdx = currentLastFieldIdx * 3;
            currentFieldId = currentFields[fldSetIdx];
            currentFieldValueStart = currentFields[fldSetIdx + 1];
            currentFieldValueLength = currentFields[fldSetIdx + 2];
        }
        int bytesRead = 0;
        for (int i = 0; i < lengthFromCurrentPosition; ++i) {
            ++bytesRead;
            --currentBytesToRead;
            final byte b = buffer[currentPosition + i];
            //                SWITCH_STATE:{
            switch (currentState) {
                case NORMAL_STATE: {
                    final int n = b - 48;
                    switch (n) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                            ++currentLastFieldIdx;
                            currentFieldId = n;
                            currentState = FIELD_ID_READING_STATE;
                            break;
                        }
                        default: {
                            throw new FixException("Unexpected char " + (char) b + " when reading field ID");
                        }
                    }
                    break;
                }
                case FIELD_ID_READING_STATE: {
                    switch (b) {
                        case AsciiUtils.SOH: {
                            throw new FixException("Unexpected separator SOH when reading field ID");
                        }
                        case '=': {
                            currentFieldValueLength = 0;
                            currentState = FIELD_VALUE_READING_STATE;
                            break;
                        }
                        default: {
                            currentFieldId = currentFieldId * 10 + (b - 48); // 48 Ascii = 0
                            break;
                        }
                    }
                    break;
                }
                case FIELD_VALUE_READING_STATE: {
                    switch (b) {
                        case '=': {
                            throw new FixException("Unexpected char " + (char) b + " when reading field value");
                        }
                        case AsciiUtils.SOH: {
                            switch (currentFieldId) {
                                case -1:
                                    break;
                                case FixTags.BodyLength:
                                    currentBytesToRead += 8; //TODO: understand this, 8 ASCII = BS backspace
                                default:
                                    int fldSetIdx = currentLastFieldIdx * 3;
                                    currentFields = ensureFieldsCapacity(fldSetIdx);
                                    currentFields[fldSetIdx] = currentFieldId;
                                    currentFields[fldSetIdx + 1] = currentFieldValueStart;
                                    currentFields[fldSetIdx + 2] = currentFieldValueLength;
                                    currentFieldId = -1;
                                    currentFieldValueStart = -1;
                                    currentFieldValueLength = -1;
                            }
                            currentState = NORMAL_STATE;
                            break;
                        }
                        default: {
                            switch (currentFieldId) {
                                case FixTags.BodyLength:
                                    final int v = b - 48;
                                    if (currentBytesToRead > Integer.MAX_VALUE - currentMessage.length) {
                                        if (v != 0) {
                                            currentBytesToRead = v;
                                        }
                                    } else {
                                        currentBytesToRead++;
                                        currentBytesToRead = currentBytesToRead * 10 + v;
                                    }
                                    break;
                            }
                            if (currentFieldValueStart < 0) {
                                currentFieldValueStart = currentPosition - startPosition + i;
                            }
                            currentFieldValueLength++;
                        } // end of default case
                    }
//                    break;
                } // end of FIELD_VALUE_CASE
            } // end of switch statement
            if (currentBytesToRead == 0) {
                break;
            }
        } // end for loop
        if (bytesRead > 0) {
            if (currentFieldId >= 0) {
                final int fldSetIdx3 = currentLastFieldIdx * 3;
                currentFields = ensureFieldsCapacity(fldSetIdx3);
                currentFields[fldSetIdx3] = currentFieldId;
                currentFields[fldSetIdx3 + 1] = currentFieldValueStart;
                currentFields[fldSetIdx3 + 2] = currentFieldValueLength;
            }
            length += bytesRead;
            lastFieldIdx = currentLastFieldIdx;
            bytesToRead = ((currentBytesToRead >= 0) ? currentBytesToRead : -1);
            state = currentState;
            if (currentBytesToRead == 0) {
                return lengthFromCurrentPosition - bytesRead;
            }
        }
        return -1;
    }

    @Override
    public final void reset() {
        this.message = null;
        this.length = 0;
        this.start = 0;
        this.state = NORMAL_STATE;
        this.lastFieldIdx = -1;
        this.bytesToRead = Integer.MAX_VALUE;
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public char charAt(final int index) {
        return (char) message[start + index];
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new String(message, this.start + start, this.start + end);
    }

//    @Override
//    public String toString() {
//        return new StringBuilder(MessageWalkable.class.getSimpleName())
//                .append("{msg=").append(message)
//                .append(", start=").append(start)
//                .append(", length=").append(length)
//                .append("}").toString();
//    }
    @Override
    public String toString() {
        return new String(message, start, length);
    }

    private int[] ensureFieldsCapacity(final int fldSetIdx) {
        final int expectedLength = fldSetIdx + 3;
        if (expectedLength > fields.length) {
            final int inc = fields.length >> 1;
            final int[] newFields = new int[fields.length + inc];
            System.arraycopy(fields, 0, newFields, 0, fields.length);
            fields = newFields;
        }
        return fields;
    }

    @Override
    public void walk(final IWalker walker) throws FixException {
        walker.onWalkBegin(this);
        entry.set(message, start, length);
        for (int i = 0; i <= lastFieldIdx; ++i) {
            final int fldSetIdx = i * 3;
            final int fieldId = fields[fldSetIdx];
            final int fieldValueStart = fields[fldSetIdx + 1];
            final int fieldValueLength = fields[fldSetIdx + 2];
            entry.setTagNum(fieldId);
            entry.setValOffset(fieldValueStart);
            entry.setValLength(fieldValueLength);
            if (!walker.onField(this, entry)) {
                break;
            }
        }
        walker.onWalkFinish(this);
    }

//    public void walk2(final IWalker walker) throws FixException {
//        walker.onWalkBegin(this);
//        final int[] flds = this.fields;
//        final int lstFldIdx = this.lastFieldIdx;
//        final FIXEntry val = this.entry;
//        val.bytes = this.message;
//        for (int i = 0; i <= lstFldIdx; ++i) {
//            final int fldSetIdx = i * 3;
//            final int fieldId = flds[fldSetIdx];
//            final int fieldValueStart = flds[fldSetIdx + 1];
//            final int fieldValueLength = flds[fldSetIdx + 2];
//            val.start = this.start + fieldValueStart;
//            val.length = fieldValueLength;
//            if (!walker.onField(this, fieldId, val)) {
//                break;
//            }
//        }
//        walker.onWalkFinish(this);
//    }
    //===========================================================================

}
