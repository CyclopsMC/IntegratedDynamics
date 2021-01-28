package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.util.text.IFormattableTextComponent;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final IFormattableTextComponent errorMessage;

    public EvaluationException(IFormattableTextComponent errorMessage) {
        super(errorMessage.toString());
        this.errorMessage = errorMessage;
    }

    public IFormattableTextComponent getErrorMessage() {
        return errorMessage;
    }
}
