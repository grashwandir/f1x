package org.f1x.api.message;

import org.f1x.api.FixException;
import org.f1x.util.ByteArrayReference;

/**
 *
 * @author niels.kowalski
 */
public interface IWalker {

    void onWalkBegin(final IWalkable msg) throws FixException;

    boolean onField(final IWalkable msg, final IEntry value) throws FixException;

    void onWalkFinish(final IWalkable msg) throws FixException;
}
