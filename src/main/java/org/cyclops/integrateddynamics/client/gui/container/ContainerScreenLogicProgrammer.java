package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammer extends ContainerScreenLogicProgrammerBase<ContainerLogicProgrammer> {

    public ContainerScreenLogicProgrammer(ContainerLogicProgrammer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

}
