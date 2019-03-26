package org.f1x.api;

import java.io.IOException;

/**
 *
 * @author niels.kowalski
 */
public class FixException extends IOException {

    public FixException(final String message) {
        super(message);
    }

    public FixException(final Throwable cause) {
        super(cause);
    }

    public FixException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
