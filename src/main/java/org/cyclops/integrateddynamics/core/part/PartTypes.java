package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.part.PartTypeInventoryReader;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneReader;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneWriter;
import org.cyclops.integrateddynamics.part.PartTypeWorldReader;

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

    // Writers
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = REGISTRY.register(new PartTypeRedstoneWriter("redstoneWriter"));

}
