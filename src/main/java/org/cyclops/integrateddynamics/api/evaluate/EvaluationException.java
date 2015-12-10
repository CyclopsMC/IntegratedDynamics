package org.cyclops.integrateddynamics.api.evaluate;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    public EvaluationException(String msg) {
        super(msg);
    }

}
