package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerPortable;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.item.ItemPortableLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammerPortable extends ContainerScreenLogicProgrammerBase<ContainerLogicProgrammerPortable> {

    public ContainerScreenLogicProgrammerPortable(ContainerLogicProgrammerPortable container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

}
