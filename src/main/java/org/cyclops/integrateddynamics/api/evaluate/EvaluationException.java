package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.network.chat.MutableComponent;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final MutableComponent errorMessage;
    private boolean retryEvaluation;

    public EvaluationException(MutableComponent errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
        this.retryEvaluation = false;
    }

    public MutableComponent getErrorMessage() {
        return errorMessage;
    }

    public void setRetryEvaluation(boolean retryEvaluation) {
        this.retryEvaluation = retryEvaluation;
    }

    public boolean isRetryEvaluation() {
        return retryEvaluation;
    }
}
