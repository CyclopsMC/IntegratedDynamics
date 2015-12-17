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
    public static final PartTypeRedstoneReader REDSTONE_READER = REGISTRY.register(new PartTypeRedstoneReader("redstoneReader"));
    public static final PartTypeInventoryReader INVENTORY_READER = REGISTRY.register(new PartTypeInventoryReader("inventoryReader"));
    public static final PartTypeWorldReader WORLD_READER = REGISTRY.register(new PartTypeWorldReader("worldReader"));
    public static final PartTypeFluidReader FLUID_READER = REGISTRY.register(new PartTypeFluidReader("fluidReader"));
    public static final PartTypeMinecraftReader MINECRAFT_READER = REGISTRY.register(new PartTypeMinecraftReader("minecraftReader"));
    public static final PartTypeNetworkReader NETWORK_READER = REGISTRY.register(new PartTypeNetworkReader("networkReader"));

    // Writers
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = REGISTRY.register(new PartTypeRedstoneWriter("redstoneWriter"));

    // Panels
    public static final PartTypePanelLightStatic STATIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightStatic("staticLightPanel"));
    public static final PartTypePanelLightDynamic DYNAMIC_LIGHT_PANEL = REGISTRY.register(new PartTypePanelLightDynamic("dynamicLightPanel"));
    public static final PartTypePanelDisplay DISPLAY_PANEL = REGISTRY.register(new PartTypePanelDisplay("displayPanel"));

}
