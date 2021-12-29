package org.cyclops.integrateddynamics;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * All mod SoundEvent references.
 * @author rubensworks
 */
public class IntegratedDynamicsSoundEvents {

    public static SoundEvent effect_page_flipsingle;
    public static SoundEvent effect_page_flipmultiple;

    private static SoundEvent getRegisteredSoundEvent(IForgeRegistry<SoundEvent> registry,  String id) {
        ResourceLocation resourceLocation = new ResourceLocation(Reference.MOD_ID, id);
        SoundEvent soundEvent = new SoundEvent(resourceLocation).setRegistryName(resourceLocation);
        registry.register(soundEvent);
        return soundEvent;
    }

    @SubscribeEvent
    public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
        effect_page_flipsingle = getRegisteredSoundEvent(event.getRegistry(), "effect.page.flipsingle");
        effect_page_flipmultiple = getRegisteredSoundEvent(event.getRegistry(), "effect.page.flipmultiple");
    }

}
