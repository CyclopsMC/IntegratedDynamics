package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerPortable;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.item.ItemPortableLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammerPortable extends ContainerScreenLogicProgrammerBase<ContainerLogicProgrammerPortable> {

    public ContainerScreenLogicProgrammerPortable(ContainerLogicProgrammerPortable container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

}
