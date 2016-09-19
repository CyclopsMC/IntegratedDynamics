package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class GuiLogicProgrammer extends GuiLogicProgrammerBase {

    /**
     * Make a new instance.
     * @param inventoryPlayer The player inventory.
     * @param world The world.
     * @param blockPos The position.
     */
    public GuiLogicProgrammer(InventoryPlayer inventoryPlayer, World world, BlockPos blockPos) {
        super(inventoryPlayer, new ContainerLogicProgrammer(inventoryPlayer, world, blockPos));
    }

}
