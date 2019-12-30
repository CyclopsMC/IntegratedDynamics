package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Exception to indicate a failed evaluation.
 * @author rubensworks
 */
public class EvaluationException extends Exception {

    private final ITextComponent errorMessage;

    public EvaluationException(String errorMessage) {
        this(new StringTextComponent(errorMessage));
    }

    public EvaluationException(ITextComponent errorMessage) {
        super(errorMessage.getString());
        this.errorMessage = errorMessage;
    }

    public ITextComponent getErrorMessage() {
        return errorMessage;
    }
}
