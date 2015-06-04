package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneReader;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneWriter;

/**
 * Collection of parts types.
 * @author rubensworks
 */
public final class PartTypes {

    public static final IPartTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IPartTypeRegistry.class);

    public static final PartTypeRedstoneReader REDSTONE_READER = REGISTRY.register(new PartTypeRedstoneReader("redstoneReader"));
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = REGISTRY.register(new PartTypeRedstoneWriter("redstoneWriter"));

}
