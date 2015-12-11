package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class GuiMultipartAspects<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>, A extends IAspect>
        extends ScrollingGuiContainer {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;

    private Map<IAspect, GuiButtonText> aspectPropertyButtons = Maps.newHashMap();

    /**
     * Make a new instance.
     * @param container The container to make the GUI for.
     */
    public GuiMultipartAspects(ContainerMultipartAspects<P, S, A> container) {
        super(container);
        this.target = container.getTarget();
        this.partContainer = container.getPartContainer();
        this.partType = container.getPartType();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        super.initGui();
        if(getPartType() instanceof PartTypeConfigurable && ((PartTypeConfigurable) getPartType()).hasSettings()) {
            buttonList.add(new GuiButtonImage(ContainerMultipartAspects.BUTTON_SETTINGS, this.guiLeft + 174, this.guiTop + 4, 15, 15, Images.CONFIG_BOARD, -2, -3, true));
        }
        for(Map.Entry<IAspect, Integer> entry : (Set<Map.Entry<IAspect, Integer>>) ((ContainerMultipartAspects) getContainer()).getAspectPropertyButtons().entrySet()) {
            GuiButtonText button = new GuiButtonText(entry.getValue(), -20, -20, 10, 10, "+", true);
            aspectPropertyButtons.put(entry.getKey(), button);
            buttonList.add(button);
        }
    }

    @SuppressWarnings("unchecked")
    public S getPartState() {
        return ((ContainerMultipartAspects<P, S, A>) container).getPartState();
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

        // Reset button positions
        for(Map.Entry<IAspect, GuiButtonText> entry : this.aspectPropertyButtons.entrySet()) {
            entry.getValue().xPosition = -20;
            entry.getValue().yPosition = -20;
        }

        // Draw part name
        RenderHelpers.drawScaledCenteredString(fontRenderer, L10NHelpers.localize(getPartType().getUnlocalizedName()),
                this.guiLeft + offsetX + 6, this.guiTop + offsetY + 10, 70, Helpers.RGBToInt(0, 0, 0));

        // Draw aspects
        ContainerMultipartAspects<P, S, A> container = (ContainerMultipartAspects) getScrollingInventoryContainer();
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
                        guiTop + offsetY + 18 + aspectBoxHeight * i, 0, getBaseYSize(), 160, aspectBoxHeight - 1);

                // Aspect type info
                String aspectName = L10NHelpers.localize(aspect.getUnlocalizedName());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + 26,
                        this.guiTop + offsetY + 25 + aspectBoxHeight * i,
                        60, Helpers.RGBToInt(40, 40, 40));

                drawAdditionalElementInfo(container, i, aspect);

                if(aspectPropertyButtons.containsKey(aspect)) {
                    GuiButtonText button = aspectPropertyButtons.get(aspect);
                    button.xPosition = this.guiLeft + offsetX + 116;
                    button.yPosition = this.guiTop + offsetY + 20 + aspectBoxHeight * i;
                }
            }
        }
    }

    protected abstract void drawAdditionalElementInfo(ContainerMultipartAspects<P, S, A> container, int index, A aspect);

    protected Rectangle getElementPosition(ContainerMultipartAspects<P, S, A> container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.guiLeft : 0),
                             ITEM_POSITION.y + container.getAspectBoxHeight() * i + offsetY + (absolute ? this.guiTop : 0),
                             ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        ContainerMultipartAspects<P, S, A> container = (ContainerMultipartAspects) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                // Item icon tooltip
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<String> lines = Lists.newLinkedList();
                    container.getVisibleElement(i).loadTooltip(lines, true);
                    drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                drawAdditionalElementInfoForeground(container, i, container.getVisibleElement(i), mouseX, mouseY);

                // Optional aspect properties tooltip
                IAspect aspect = container.getVisibleElement(i);
                if(aspectPropertyButtons.containsKey(aspect)) {
                    GuiButtonText button = aspectPropertyButtons.get(aspect);
                    int x = button.xPosition - guiLeft;
                    int y = button.yPosition - guiTop;
                    if(isPointInRegion(x, y, button.width, button.height, mouseX, mouseY)) {
                        List<String> lines = Lists.newLinkedList();
                        lines.add(EnumChatFormatting.WHITE + L10NHelpers.localize("gui.integrateddynamics.part.properties"));
                        for(IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                            lines.add("-" + EnumChatFormatting.YELLOW + L10NHelpers.localize(property.getUnlocalizedName()));
                        }
                        drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                    }
                }
            }
        }
    }

    protected abstract void drawAdditionalElementInfoForeground(ContainerMultipartAspects<P, S, A> container, int index,
                                                                A aspect, int mouseX, int mouseY);

}
