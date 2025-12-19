package org.cyclops.integrateddynamics.sound;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class SoundEventEffectPageFlipSingleConfig extends org.cyclops.cyclopscore.config.extendedconfig.SoundEventConfigCommon<IModBase> {
    public SoundEventEffectPageFlipSingleConfig() {
        super(
                IntegratedDynamics._instance,
                "effect_page_flipsingle",
                (eConfig) -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(eConfig.getMod().getModId(), eConfig.getNamedId()))
        );
    }
}
