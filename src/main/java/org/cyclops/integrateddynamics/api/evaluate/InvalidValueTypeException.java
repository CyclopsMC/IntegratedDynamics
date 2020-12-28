package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.util.text.IFormattableTextComponent;

/**
 * An exception to signal mismatching variable types.
 * @author rubensworks
 */
public class InvalidValueTypeException extends EvaluationException {

    public InvalidValueTypeException(IFormattableTextComponent errorMessage) {
        super(errorMessage);
    }

}
