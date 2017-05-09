package org.cyclops.integrateddynamics;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * All mod SoundEvent references.
 * @author rubensworks
 */
public class IntegratedDynamicsSoundEvents {

    public static final SoundEvent effect_page_flipsingle;
    public static final SoundEvent effect_page_flipmultiple;

    private static SoundEvent getRegisteredSoundEvent(String id) {
        ResourceLocation resourceLocation = new ResourceLocation(Reference.MOD_ID, id);
        return GameRegistry.register(new SoundEvent(resourceLocation).setRegistryName(resourceLocation));
    }

    static {
        if (!Bootstrap.isRegistered()) {
            throw new RuntimeException("Accessed Sounds before Bootstrap!");
        } else {
            effect_page_flipsingle = getRegisteredSoundEvent("effect.page.flipsingle");
            effect_page_flipmultiple = getRegisteredSoundEvent("effect.page.flipmultiple");
        }
    }

}
