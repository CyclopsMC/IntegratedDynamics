package org.cyclops.integrateddynamics.gametest.fuzzing;

/**
 * Exception thrown by NetworkFuzzer when an error occurs during network generation.
 * This exception is intended to be caught and converted to a GameTestAssertException.
 *
 * @author rubensworks
 */
public class NetworkFuzzerException extends Exception {

    public NetworkFuzzerException(String message) {
        super(message);
    }

    public NetworkFuzzerException(String message, Throwable cause) {
        super(message, cause);
    }
}
