package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;

/**
 * Container for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammer extends ContainerLogicProgrammerBase {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param world The world.
     * @param blockPos The position.
     */
    public ContainerLogicProgrammer(InventoryPlayer inventory, World world, BlockPos blockPos) {
        super(inventory, BlockLogicProgrammer.getInstance());
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
