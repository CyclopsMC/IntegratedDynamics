package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;

import java.awt.*;
import java.util.List;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class GuiMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>, A extends IAspect>
        extends ScrollingGuiContainer {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;

    /**
     * Make a new instance.
     * @param container The container to make the GUI for.
     */
    public GuiMultipart(ContainerMultipart<P, S, A> container) {
        super(container);
        this.target = container.getTarget();
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

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        FontRenderer fontRenderer = fontRendererObj;

        // Draw part name
        RenderHelpers.drawScaledCenteredString(fontRenderer, L10NHelpers.localize(getPartType().getUnlocalizedName()),
                this.guiLeft + offsetX + 6, this.guiTop + offsetY + 10, 70, Helpers.RGBToInt(0, 0, 0));

        // Draw aspects
        ContainerMultipart<P, S, A> container = (ContainerMultipart) getScrollingInventoryContainer();
        int aspectBoxHeight = container.getAspectBoxHeight();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                A aspect = container.getVisibleElement(i);

                GlStateManager.disableAlpha();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(aspect.getValueType().getDisplayColor());
                GlStateManager.color(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                        colorSmoothener(rgb.getRight()), 1);

                // Background
                mc.renderEngine.bindTexture(texture);
                drawTexturedModalRect(guiLeft + offsetX + 9,
                        guiTop + offsetY + 18 + aspectBoxHeight * i, 0, 213, 160, aspectBoxHeight - 1);

                // Aspect type info
                String aspectName = L10NHelpers.localize(aspect.getUnlocalizedName());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + 26,
                        this.guiTop + offsetY + 25 + aspectBoxHeight * i,
                        60, Helpers.RGBToInt(40, 40, 40));

                drawAdditionalElementInfo(container, i, aspect);
            }
        }
    }

    protected abstract void drawAdditionalElementInfo(ContainerMultipart<P, S, A> container, int index, A aspect);

    protected Rectangle getElementPosition(ContainerMultipart<P, S, A> container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.guiLeft : 0),
                             ITEM_POSITION.y + container.getAspectBoxHeight() * i + offsetY + (absolute ? this.guiTop : 0),
                             ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        ContainerMultipart<P, S, A> container = (ContainerMultipart) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<String> lines = Lists.newLinkedList();
                    container.getVisibleElement(i).loadTooltip(lines, true);
                    drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                drawAdditionalElementInfoForeground(container, i, container.getVisibleElement(i), mouseX, mouseY);
            }
        }
    }

    protected abstract void drawAdditionalElementInfoForeground(ContainerMultipart<P, S, A> container, int index,
                                                                A aspect, int mouseX, int mouseY);

}
