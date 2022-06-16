package org.cyclops.integrateddynamics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

/**
 * All mod SoundEvent references.
 * @author rubensworks
 */
public class IntegratedDynamicsSoundEvents {

    public static SoundEvent effect_page_flipsingle;
    public static SoundEvent effect_page_flipmultiple;

    private static SoundEvent getRegisteredSoundEvent(IForgeRegistry<SoundEvent> registry,  String id) {
        ResourceLocation resourceLocation = new ResourceLocation(Reference.MOD_ID, id);
        SoundEvent soundEvent = new SoundEvent(resourceLocation);
        registry.register(resourceLocation, soundEvent);
        return soundEvent;
    }

    @SubscribeEvent
    public static void registerSoundEvents(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.SOUND_EVENTS)) {
            effect_page_flipsingle = getRegisteredSoundEvent(event.getForgeRegistry(), "effect.page.flipsingle");
            effect_page_flipmultiple = getRegisteredSoundEvent(event.getForgeRegistry(), "effect.page.flipmultiple");
        }
    }

}
