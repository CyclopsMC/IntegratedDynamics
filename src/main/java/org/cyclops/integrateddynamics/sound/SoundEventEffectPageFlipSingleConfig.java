package org.cyclops.integrateddynamics.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class SoundEventEffectPageFlipSingleConfig extends org.cyclops.cyclopscore.config.extendedconfig.SoundEventConfig {
    public SoundEventEffectPageFlipSingleConfig() {
        super(
                IntegratedDynamics._instance,
                "effect_page_flipsingle",
                (eConfig) -> SoundEvent.createVariableRangeEvent(new ResourceLocation(eConfig.getMod().getModId(), eConfig.getNamedId()))
        );
    }
}
