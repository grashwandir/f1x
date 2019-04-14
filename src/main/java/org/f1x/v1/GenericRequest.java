package org.f1x.v1;

import java.io.IOException;
import org.f1x.api.FixException;
import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.fields.MsgType;
import org.f1x.api.session.FixSession;

public class GenericRequest {

    public final static int NULL_REQ_ID = Integer.MIN_VALUE;

    private final FixSession<?, MessageBuilder, ?> owner;
    private final int idTag;
//    private final MsgType msgType;
//    private int id = NULL_REQ_ID;
    private final MessageBuilder message;
//    public volatile Object userObject;

    public GenericRequest(final FixSession<?, MessageBuilder, ?> owner, final int idTag, final MsgType msgType) {
        this.owner = owner;
        this.idTag = idTag;
//        this.msgType = msgType;
        this.message = owner.createMessageBuilder();
        this.message.setMessageType(msgType);
    }

//    public int getId() {
//        return this.id;
//    }

    public void setId(final int id) {
        this.message.add(idTag, id);
    }

    public void clear() {
        message.clear();
//        this.id = NULL_REQ_ID;
    }

    public void add(final int fieldId, final CharSequence value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final int value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final byte value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final char value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final long value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final boolean value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final float value) {
        message.add(fieldId, value);
    }

    public void add(final int fieldId, final double value) {
        message.add(fieldId, value);
    }

//    public boolean isStopped() {
//        return owner.isStopped();
//    }
//
//    public void send() throws IOException {
//        if (id == NULL_REQ_ID) {
//            throw new FixException("Request ID not set for message type: " + message.getMessageType() + " (" + idTag + ")");
//        }
//        this.message.add(idTag, id);
//        owner.send(message);
//    }

    public CharSequence getMsgType() {
        return message.getMessageType();
    }

    public MessageBuilder getMessage() {
        return message;
    }

}
