package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.core.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.awt.*;

/**
 * Gui for a reader part.
 * @author rubensworks
 */
public class GuiPartReader<P extends IPartTypeReader<P, S> & IGuiContainerProvider, S extends IPartStateReader<P>>
        extends GuiMultipart<P, S, IAspectRead> {

    private long lastUpdate = -1;

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
        return "partReader";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(ContainerMultipart<P, S, IAspectRead> container, int index, IAspectRead aspect, int mouseX, int mouseY) {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Client-side, so we need to do a manual part update, but not every frame refresh.
        if(Minecraft.getMinecraft().theWorld.getWorldTime() > lastUpdate) {
            lastUpdate = Minecraft.getMinecraft().theWorld.getWorldTime();
            getPartType().update(null, getTarget(), getPartState());
        }
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawAdditionalElementInfo(ContainerMultipart container, int index, IAspectRead aspect) {
        FontRenderer fontRenderer = fontRendererObj;

        // Current aspect value
        IAspectVariable variable = getPartType().getVariable(getTarget(), getPartState(), aspect);
        String value = variable.getType().toCompactString(variable.getValue());
        fontRenderer.drawString(value, this.guiLeft + offsetX + 16,
                this.guiTop + offsetY + 35 + container.getAspectBoxHeight() * index,
                variable.getType().getDisplayColor());

        // Render target item
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
