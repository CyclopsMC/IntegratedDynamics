package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.network.chat.MutableComponent;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final MutableComponent errorMessage;

    public EvaluationException(MutableComponent errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
    }

    public MutableComponent getErrorMessage() {
        return errorMessage;
    }
}
