package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.util.text.IFormattableTextComponent;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final IFormattableTextComponent errorMessage;
    private boolean retryEvaluation;

    public EvaluationException(IFormattableTextComponent errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
        this.retryEvaluation = false;
    }

    public IFormattableTextComponent getErrorMessage() {
        return errorMessage;
    }

    public void setRetryEvaluation(boolean retryEvaluation) {
        this.retryEvaluation = retryEvaluation;
    }

    public boolean isRetryEvaluation() {
        return retryEvaluation;
    }
}
