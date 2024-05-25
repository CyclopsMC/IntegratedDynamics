package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.entity.player.Inventory;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Container for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammer extends ContainerLogicProgrammerBase {

    public ContainerLogicProgrammer(int id, Inventory playerInventory) {
        super(RegistryEntries.CONTAINER_LOGIC_PROGRAMMER.get(), id, playerInventory);
    }
}
