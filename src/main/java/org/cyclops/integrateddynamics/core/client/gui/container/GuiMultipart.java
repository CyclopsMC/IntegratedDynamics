package org.cyclops.integrateddynamics.core.client.gui.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class GuiMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends GuiContainerExtended {

    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param container The container to make the GUI for.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     * @param partState The targeted part state.
     */
    public GuiMultipart(InventoryPlayer inventory, ContainerMultipart<P, S> container, IPartContainer partContainer,
                        P partType, S partState) {
        super(container);
        this.partContainer = container.getPartContainer();
        this.partType = container.getPartType();
        this.partState = container.getPartState();
    }

    @Override
    public String getGuiTexture() {
        return "";// TODO
    }

}
