package org.f1x.api.message;

import org.f1x.api.FixException;
import org.f1x.util.parse.Value;

/**
 *
 * @author niels.kowalski
 */
public interface Walker {

    void onWalkBegin(final MessageParser msg) throws FixException;

    boolean onField(final MessageParser msg, final int fieldId, final Value value) throws FixException;

    void onWalkFinish(final MessageParser msg) throws FixException;
}
