package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipartAspects;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.awt.*;

/**
 * Gui for a reader part.
 * @author rubensworks
 */
public class GuiPartReader<P extends IPartTypeReader<P, S> & IGuiContainerProvider, S extends IPartStateReader<P>>
        extends GuiMultipartAspects<P, S, IAspectRead> {

    /**
     * Make a new instance.
     * @param partTarget The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     */
    public GuiPartReader(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType) {
        super(new ContainerPartReader<P, S>(player, partTarget, partContainer, partType));
    }

    @Override
    protected String getNameId() {
        return "part_reader";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(ContainerMultipartAspects<P, S, IAspectRead> container, int index, IAspectRead aspect, int mouseX, int mouseY) {

    }

    @Override
    protected void drawAdditionalElementInfo(ContainerMultipartAspects container, int index, IAspectRead aspect) {
        FontRenderer fontRenderer = fontRendererObj;

        // Get current aspect value
        ContainerPartReader reader = (ContainerPartReader) container;

        Pair<String, Integer> readValues = reader.getReadValue(aspect);
        if(readValues != null) {
            RenderHelpers.drawScaledCenteredString(fontRendererObj, readValues.getLeft(), this.guiLeft + offsetX + 16,
                    this.guiTop + offsetY + 39 + container.getAspectBoxHeight() * index,
                    70, readValues.getRight());
        }

        // Render target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(ItemVariable.getInstance()), aspect);
        Rectangle pos = getElementPosition(container, index, true);

        RenderHelper.enableGUIStandardItemLighting();
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
