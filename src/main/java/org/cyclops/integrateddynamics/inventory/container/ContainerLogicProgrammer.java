package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Container for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammer extends ContainerLogicProgrammerBase {

    public ContainerLogicProgrammer(int id, PlayerInventory playerInventory) {
        super(RegistryEntries.CONTAINER_LOGIC_PROGRAMMER, id, playerInventory);
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false; // TODO: rm
    }

}
