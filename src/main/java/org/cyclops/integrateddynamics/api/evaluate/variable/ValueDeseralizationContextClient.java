package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.client.Minecraft;

/**
 * @author rubensworks
 */
public class ValueDeseralizationContextClient {
    public static ValueDeseralizationContext ofClient() {
        return ValueDeseralizationContext.of(Minecraft.getInstance().level);
    }
}
