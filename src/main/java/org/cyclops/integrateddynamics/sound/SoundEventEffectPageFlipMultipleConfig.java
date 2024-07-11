package org.cyclops.integrateddynamics.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class SoundEventEffectPageFlipMultipleConfig extends org.cyclops.cyclopscore.config.extendedconfig.SoundEventConfig {
    public SoundEventEffectPageFlipMultipleConfig() {
        super(
                IntegratedDynamics._instance,
                "effect_page_flipmultiple",
                (eConfig) -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(eConfig.getMod().getModId(), eConfig.getNamedId()))
        );
    }
}
