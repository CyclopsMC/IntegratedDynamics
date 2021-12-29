package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.resources.ResourceLocation;

/**
 * Exception that can occur while loading custom models.
 * @author rubensworks
 */
public class CustomModelException extends RuntimeException {

    public CustomModelException(ResourceLocation modelLocation) {
        super(String.format("The model %s could not be loaded.", modelLocation));
    }

}
