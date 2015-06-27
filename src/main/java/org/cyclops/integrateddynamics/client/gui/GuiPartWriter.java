package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
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
import java.util.List;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartWriter<P extends IPartTypeWriter<P, S> & IGuiContainerProvider, S extends IPartStateWriter<P>>
        extends GuiMultipart<P, S, IAspectWrite> {

    private static final int ERROR_X = 152;
    private static final int ERROR_Y = 20;
    private static final int ERROR_WIDTH = 13;
    private static final int ERROR_HEIGHT = 13;

    private static final int OK_X = 152;
    private static final int OK_Y = 20;
    private static final int OK_WIDTH = 14;
    private static final int OK_HEIGHT = 14;

    /**
     * Make a new instance.
     * @param partTarget The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     */
    public GuiPartWriter(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType) {
        super(new ContainerPartWriter<P, S>(player, partTarget, partContainer, partType));
    }

    @Override
    protected String getNameId() {
        return "partWriter";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(ContainerMultipart<P, S, IAspectWrite> container, int index, IAspectWrite aspect, int mouseX, int mouseY) {
        // Render error tooltip
        L10NHelpers.UnlocalizedString error = getPartState().getError(aspect);
        if(error != null) {
            if(isPointInRegion(ERROR_X, ERROR_Y + index * container.getAspectBoxHeight(), ERROR_WIDTH, ERROR_HEIGHT, mouseX, mouseY)) {
                List<String> lines = StringHelpers.splitLines(error.localize(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                        EnumChatFormatting.RED.toString());
                drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
            }
        }
    }

    @Override
    protected void drawAdditionalElementInfo(ContainerMultipart container, int index, IAspectWrite aspect) {
        int aspectBoxHeight = container.getAspectBoxHeight();

        // Render dummy target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(ItemVariable.getInstance()), aspect);
        Rectangle pos = getElementPosition(container, index, true);
        itemRender.renderItemAndEffectIntoGUI(itemStack, pos.x, pos.y);

        // Render error symbol
        mc.renderEngine.bindTexture(texture);
        if(getPartState().getError(aspect) != null) {
            drawTexturedModalRect(guiLeft + offsetX + ERROR_X,
                    guiTop + offsetY + ERROR_Y + aspectBoxHeight * index, 195, 0, ERROR_WIDTH, ERROR_HEIGHT);
        } else if(getPartState().getActiveAspect() == aspect) {
            drawTexturedModalRect(guiLeft + offsetX + OK_X,
                    guiTop + offsetY + OK_Y + aspectBoxHeight * index, 195, 13, OK_WIDTH, OK_HEIGHT);
        }
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
