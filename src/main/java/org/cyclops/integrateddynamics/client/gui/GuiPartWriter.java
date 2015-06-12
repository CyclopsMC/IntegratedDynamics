package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.awt.*;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartWriter<P extends IPartTypeWriter<P, S> & IGuiContainerProvider, S extends IPartStateWriter<P>>
        extends GuiMultipart<P, S, IAspectWrite> {

    /**
     * Make a new instance.
     * @param partTarget The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     * @param partState The targeted part state.
     */
    public GuiPartWriter(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType, S partState) {
        super(new ContainerPartWriter<P, S>(player, partTarget, partContainer, partType, partState));
    }

    @Override
    protected String getNameId() {
        return "partWriter";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(ContainerMultipart<P, S, IAspectWrite> container, int index, IAspectWrite aspect, int mouseX, int mouseY) {

    }

    @Override
    protected void drawAdditionalElementInfo(ContainerMultipart container, int index, IAspectWrite aspect) {
        int aspectBoxHeight = container.getAspectBoxHeight();

        // Render dummy target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(new ItemStack(ItemVariable.getInstance()), aspect);
        Rectangle pos = getElementPosition(container, index, true);
        itemRender.renderItemAndEffectIntoGUI(itemStack, pos.x, pos.y);
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
