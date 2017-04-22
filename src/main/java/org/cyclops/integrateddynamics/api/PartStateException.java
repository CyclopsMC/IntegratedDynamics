package org.cyclops.integrateddynamics.api;

/**
 * A runtime exception that can be thrown when a part is in an invalid state.
 * @author rubensworks
 */
public class PartStateException extends IllegalArgumentException {

    public PartStateException(String message) {
        super(message);
    }

}
