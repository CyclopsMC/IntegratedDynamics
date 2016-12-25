package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;
import org.cyclops.integrateddynamics.part.*;

/**
 * Collection of parts types.
 * @author rubensworks
 */
public final class PartTypes {

    public static final IPartTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IPartTypeRegistry.class);

    public static void load() {}

    // Readers
    public static final PartTypeAudioReader AUDIO_READER = REGISTRY.register(new PartTypeAudioReader("audio_reader"));
    public static final PartTypeBlockReader BLOCK_READER = REGISTRY.register(new PartTypeBlockReader("block_reader"));
    public static final PartTypeEntityReader ENTITY_READER = REGISTRY.register(new PartTypeEntityReader("entity_reader"));
    public static final PartTypeExtraDimensionalReader EXTRADIMENSIONAL_READER = REGISTRY.register(new PartTypeExtraDimensionalReader("extradimensional_reader"));
    public static final PartTypeFluidReader FLUID_READER = REGISTRY.register(new PartTypeFluidReader("fluid_reader"));
    public static final PartTypeInventoryReader INVENTORY_READER = REGISTRY.register(new PartTypeInventoryReader("inventory_reader"));
    public static final PartTypeMachineReader MACHINE_READER = REGISTRY.register(new PartTypeMachineReader("machine_reader"));
    public static final PartTypeNetworkReader NETWORK_READER = REGISTRY.register(new PartTypeNetworkReader("network_reader"));
    public static final PartTypeRedstoneReader REDSTONE_READER = REGISTRY.register(new PartTypeRedstoneReader("redstone_reader"));
    public static final PartTypeWorldReader WORLD_READER = REGISTRY.register(new PartTypeWorldReader("world_reader"));

    // Writers
    public static final PartTypeAudioWriter AUDIO_WRITER = REGISTRY.register(new PartTypeAudioWriter("audio_writer"));
    public static final PartTypeEffectWriter EFFECT_WRITER = REGISTRY.register(new PartTypeEffectWriter("effect_writer"));
    public static final PartTypeInventoryWriter INVENTORY_WRITER = REGISTRY.register(new PartTypeInventoryWriter("inventory_writer"));
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = REGISTRY.register(new PartTypeRedstoneWriter("redstone_writer"));

    // Panels
    public static final PartTypePanelLightStatic STATIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightStatic("static_light_panel"));
    public static final PartTypePanelLightDynamic DYNAMIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightDynamic("dynamic_light_panel"));
    public static final PartTypePanelDisplay DISPLAY_PANEL = REGISTRY.register(new PartTypePanelDisplay("display_panel"));

}
