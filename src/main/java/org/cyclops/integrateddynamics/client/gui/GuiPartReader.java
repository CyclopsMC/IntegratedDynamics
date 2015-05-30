package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;

/**
 * Gui for a reader part.
 * @author rubensworks
 */
public class GuiPartReader<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends GuiMultipart<P, S> {

    /**
     * Make a new instance.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     * @param partState The targeted part state.
     */
    public GuiPartReader(EntityPlayer player, IPartContainer partContainer, P partType, S partState) {
        super(new ContainerPartReader<P, S>(player, partContainer, partType, partState));
    }

    @Override
    protected String getNameId() {
        return "partReader";
    }

    @Override
    protected int getBaseXSize() {
        return 195;
    }

    @Override
    protected int getBaseYSize() {
        return 213;
    }
}
