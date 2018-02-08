package org.cyclops.integrateddynamics.api.evaluate;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final boolean transientError;

    public EvaluationException(String msg, boolean transientError) {
        super(msg);
        this.transientError = transientError;
    }

    public EvaluationException(String msg) {
        this(msg, false);
    }

    /**
     * @return If this error should not be persistent, i.e., if it should not stop re-evaluation in the next tick.
     */
    public boolean isTransientError() {
        return transientError;
    }
}
