package net.onenandone.fralax;

/**
 * @author Daniel Draper Johann Bähler
 *         Created on 06.04.16.
 * @version 1.0
 *          Exception thrown by Fralax-Related classes when errors occur during parsing.
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
