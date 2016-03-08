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
    public static final PartTypeAudioReader AUDIO_READER = REGISTRY.register(new PartTypeAudioReader("audioReader"));
    public static final PartTypeBlockReader BLOCK_READER = REGISTRY.register(new PartTypeBlockReader("blockReader"));
    public static final PartTypeEntityReader ENTITY_READER = REGISTRY.register(new PartTypeEntityReader("entityReader"));
    public static final PartTypeExtraDimensionalReader EXTRADIMENSIONAL_READER = REGISTRY.register(new PartTypeExtraDimensionalReader("extradimensionalReader"));
    public static final PartTypeFluidReader FLUID_READER = REGISTRY.register(new PartTypeFluidReader("fluidReader"));
    public static final PartTypeInventoryReader INVENTORY_READER = REGISTRY.register(new PartTypeInventoryReader("inventoryReader"));
    public static final PartTypeMachineReader MACHINE_READER = REGISTRY.register(new PartTypeMachineReader("machineReader"));
    public static final PartTypeNetworkReader NETWORK_READER = REGISTRY.register(new PartTypeNetworkReader("networkReader"));
    public static final PartTypeRedstoneReader REDSTONE_READER = REGISTRY.register(new PartTypeRedstoneReader("redstoneReader"));
    public static final PartTypeWorldReader WORLD_READER = REGISTRY.register(new PartTypeWorldReader("worldReader"));

    // Writers
    public static final PartTypeAudioWriter AUDIO_WRITER = REGISTRY.register(new PartTypeAudioWriter("audioWriter"));
    public static final PartTypeEffectWriter EFFECT_WRITER = REGISTRY.register(new PartTypeEffectWriter("effectWriter"));
    public static final PartTypeInventoryWriter INVENTORY_WRITER = REGISTRY.register(new PartTypeInventoryWriter("inventoryWriter"));
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = REGISTRY.register(new PartTypeRedstoneWriter("redstoneWriter"));

    // Panels
    public static final PartTypePanelLightStatic STATIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightStatic("staticLightPanel"));
    public static final PartTypePanelLightDynamic DYNAMIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightDynamic("dynamicLightPanel"));
    public static final PartTypePanelDisplay DISPLAY_PANEL = REGISTRY.register(new PartTypePanelDisplay("displayPanel"));

}
