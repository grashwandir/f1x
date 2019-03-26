package org.f1x.api.message;

/**
 *
 * @author niels.kowalski
 */
public interface IMessageParser extends MessageParser {

    void set(byte[] buffer, int offset, int length);

    int getOffset();

    String describe();
}
