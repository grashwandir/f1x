package org.f1x.api.message;

import org.f1x.api.FixException;

/**
 *
 * @author niels.kowalski
 */
public interface IWalkable extends CharSequence {

    void walk(final IWalker walker) throws FixException;

    void reset();
}
