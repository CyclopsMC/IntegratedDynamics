package org.cyclops.integrateddynamics.api.evaluate;

/**
 * An exception to signal mismatching variable types.
 * @author rubensworks
 */
public class InvalidValueTypeException extends EvaluationException {

    public InvalidValueTypeException(String msg, boolean transientError) {
        super(msg, transientError);
    }

    public InvalidValueTypeException(String msg) {
        super(msg);
    }

}
