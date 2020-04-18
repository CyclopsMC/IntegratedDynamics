package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenMultipartAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;

import java.awt.*;

/**
 * Gui for a reader part.
 * @author rubensworks
 */
public class ContainerScreenPartReader<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>>
        extends ContainerScreenMultipartAspects<P, S, IAspectRead, ContainerPartReader<P, S>> {

    public ContainerScreenPartReader(ContainerPartReader<P, S> container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected String getNameId() {
        return "part_reader";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(ContainerPartReader<P, S> container, int index, IAspectRead aspect, int mouseX, int mouseY) {

    }

    @Override
    protected void drawAdditionalElementInfo(ContainerPartReader<P, S> container, int index, IAspectRead aspect) {
        // Get current aspect value
        Pair<ITextComponent, Integer> readValues = container.getReadValue(aspect);
        if(readValues != null && readValues.getLeft() != null) {
            RenderHelpers.drawScaledCenteredString(font, readValues.getLeft().getFormattedText(), this.guiLeft + offsetX + 16,
                    this.guiTop + offsetY + 39 + container.getAspectBoxHeight() * index,
                    70, readValues.getRight());
        }

        // Render target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(RegistryEntries.ITEM_VARIABLE), aspect);
        Rectangle pos = getElementPosition(container, index, true);

        RenderHelper.enableStandardItemLighting();
        itemRenderer.renderItemAndEffectIntoGUI(itemStack, pos.x, pos.y);
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
