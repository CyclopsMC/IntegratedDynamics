package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammer extends ContainerScreenLogicProgrammerBase<ContainerLogicProgrammer> {

    public ContainerScreenLogicProgrammer(ContainerLogicProgrammer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

}
