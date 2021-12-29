package org.cyclops.integrateddynamics.api.evaluate;

import net.minecraft.network.chat.MutableComponent;

/**
 * An exception to signal mismatching variable types.
 * @author rubensworks
 */
public class InvalidValueTypeException extends EvaluationException {

    public InvalidValueTypeException(MutableComponent errorMessage) {
        super(errorMessage);
    }

}
