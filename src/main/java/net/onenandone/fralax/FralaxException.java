package net.onenandone.fralax;

/**
 * Exception thrown by Fralax-Related classes when errors occur during parsing.
 *
 * @author Daniel Draper Johann Bähler
 * @version 1.0
 */
public class FralaxException extends RuntimeException {

    /**
     * @param message message of the Exception.
     * @see RuntimeException#RuntimeException(String)
     */
    public FralaxException(String message) {
        super(message);
    }

    /**
     * @param message message of the Exception.
     * @param cause   cause of the Exception.
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public FralaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
