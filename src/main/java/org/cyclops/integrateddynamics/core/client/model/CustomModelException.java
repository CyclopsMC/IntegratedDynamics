package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.resources.Identifier;

/**
 * Exception that can occur while loading custom models.
 * @author rubensworks
 */
public class CustomModelException extends RuntimeException {

    public CustomModelException(Identifier modelLocation) {
        super(String.format("The facadeModel %s could not be loaded.", modelLocation));
    }

}
