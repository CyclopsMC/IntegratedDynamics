package org.cyclops.integrateddynamics.core.client.gui.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class GuiMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ScrollingGuiContainer {

    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;

    /**
     * Make a new instance.
     * @param container The container to make the GUI for.
     */
    public GuiMultipart(ContainerMultipart<P, S> container) {
        super(container);
        this.partContainer = container.getPartContainer();
        this.partType = container.getPartType();
        this.partState = container.getPartState();
    }

    protected abstract String getNameId();

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
               + getNameId() + ".png";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Draw aspects
        ScrollingInventoryContainer<IAspect> container = getScrollingInventoryContainer();
        FontRenderer fontRenderer = fontRendererObj;
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                GlStateManager.disableAlpha();
                GlStateManager.color(1, 1, 1, 1);
                mc.renderEngine.bindTexture(texture);
                drawTexturedModalRect(guiLeft + offsetX + 9,
                        guiTop + offsetY + 18 + (ContainerMultipart.ASPECT_BOX_HEIGHT) * i, 0, 213, 160,
                        ContainerMultipart.ASPECT_BOX_HEIGHT - 1);
                IAspect aspect = container.getVisibleElement(i);
                String aspectName = L10NHelpers.localize(aspect.getUnlocalizedName());
                fontRenderer.drawString(aspectName, this.guiLeft + offsetX + 10,
                        this.guiTop + offsetY + 20 + ContainerMultipart.ASPECT_BOX_HEIGHT * i,
                        RenderHelpers.RGBToInt(40, 40, 40));
            }
        }
    }

}
