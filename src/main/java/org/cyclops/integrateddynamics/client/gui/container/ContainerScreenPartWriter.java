package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenMultipartAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;

import java.awt.*;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class ContainerScreenPartWriter<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends ContainerScreenMultipartAspects<P, S, IAspectWrite, ContainerPartWriter<P, S>> {

    private static final int ERROR_X = 152;
    private static final int ERROR_Y = 20;
    private static final int OK_X = 152;
    private static final int OK_Y = 20;

    public ContainerScreenPartWriter(ContainerPartWriter<P, S> container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected String getNameId() {
        return "part_writer";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(MatrixStack matrixStack, ContainerPartWriter<P, S> container, int index, IAspectWrite aspect, int mouseX, int mouseY) {
        // Render error tooltip
        if(getContainer().isPartStateEnabled()) {
            displayErrors.drawForeground(matrixStack, getContainer().getAspectErrors(aspect), ERROR_X, ERROR_Y + container.getAspectBoxHeight() * index, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        }
    }

    @Override
    protected void drawAdditionalElementInfo(MatrixStack matrixStack, ContainerPartWriter<P, S> container, int index, IAspectWrite aspect) {
        int aspectBoxHeight = container.getAspectBoxHeight();

        // Render dummy target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(RegistryEntries.ITEM_VARIABLE), aspect);
        Rectangle pos = getElementPosition(container, index, true);
        RenderHelper.enableStandardItemLighting();
        itemRenderer.renderItemAndEffectIntoGUI(itemStack, pos.x, pos.y);

        // Render error symbol
        if(getContainer().isPartStateEnabled()) {
            displayErrors.drawBackground(matrixStack, getContainer().getAspectErrors(aspect), ERROR_X, ERROR_Y + aspectBoxHeight * index, OK_X, OK_Y + aspectBoxHeight * index, this,
                    this.guiLeft, this.guiTop, getContainer().getPartStateActiveAspect() == aspect);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        ContainerPartWriter<?, ?> container = (ContainerPartWriter<?, ?>) getContainer();
        RenderHelpers.drawScaledCenteredString(matrixStack, font, container.getWriteValue().getString(), this.guiLeft + offsetX + 53,
                this.guiTop + offsetY + 132, 70, container.getWriteValueColor());
    }

    @Override
    protected int getBaseXSize() {
        return 195;
    }

    @Override
    protected int getBaseYSize() {
        return 222;
    }

    @Override
    public int getMaxLabelWidth() {
        return 85;
    }
}
