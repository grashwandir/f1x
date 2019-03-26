package org.f1x.api;

/**
 *
 * @author niels.kowalski
 */
public class UncheckedFixException extends RuntimeException {

    public UncheckedFixException(String message) {
        super(message);
    }

    public UncheckedFixException(String message, Throwable cause) {
        super(message, cause);
    }
}
