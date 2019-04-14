package org.f1x.api.message;

import org.f1x.api.FixException;

/**
 *
 * @author niels.kowalski
 * @param <T>
 */
//TODO: ungeneric this class to work only with interface
public interface IMessageParser_1<T extends IInputMessage> {

    int parse(final byte[] buffer, final int startPosition, final int currentPosition, final int lengthFromCurrentPosition) throws FixException;

    T getMessage();

    int parsedLength();

    void reset();
}
