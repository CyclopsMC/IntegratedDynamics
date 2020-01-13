package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerScreenMultipartAspects<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect, C extends ContainerMultipartAspects<P, S, A>>
        extends ContainerScreenScrolling<C> {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

    private Map<IAspect, ButtonText> aspectPropertyButtons = Maps.newHashMap();

    public ContainerScreenMultipartAspects(C container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected boolean isScrollAnywhere() {
        return true;
    }

    @Override
    public void init() {
        buttons.clear();
        super.init();
        if(getContainer().getPartType().getContainerProviderSettings(null).isPresent()) {
            addButton(new ButtonImage(this.guiLeft + 174, this.guiTop + 4, 15, 15,
                    L10NHelpers.localize("gui.integrateddynamics.partsettings"),
                    createServerPressable(ContainerMultipartAspects.BUTTON_SETTINGS, b -> {}), true,
                    Images.CONFIG_BOARD, -2, -3));
        }
        for(Map.Entry<IAspect, String> entry : getContainer().getAspectPropertyButtons().entrySet()) {
            ButtonText button = new ButtonText(-20, -20, 10, 10,
                    L10NHelpers.localize("gui.integrateddynamics.aspect_settings"), "+",
                    createServerPressable(entry.getValue(), b -> {}), true);
            aspectPropertyButtons.put(entry.getKey(), button);
            addButton(button);
        }
    }

    protected abstract String getNameId();

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/" + getNameId() + ".png");
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Reset button positions
        for(Map.Entry<IAspect, ButtonText> entry : this.aspectPropertyButtons.entrySet()) {
            entry.getValue().x = -20;
            entry.getValue().y = -20;
        }

        // Draw part name
        RenderHelpers.drawScaledCenteredString(font, title.getFormattedText(),
                this.guiLeft + offsetX + 6, this.guiTop + offsetY + 10, 70, Helpers.RGBToInt(0, 0, 0));

        // Draw aspects
        C container = getContainer();
        int aspectBoxHeight = container.getAspectBoxHeight();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                A aspect = container.getVisibleElement(i);

                GlStateManager.disableAlphaTest();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(aspect.getValueType().getDisplayColor());
                GlStateManager.color4f(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                        colorSmoothener(rgb.getRight()), 1);

                // Background
                RenderHelpers.bindTexture(texture);
                blit(guiLeft + offsetX + 9,
                        guiTop + offsetY + 18 + aspectBoxHeight * i, 0, getBaseYSize(), 160, aspectBoxHeight - 1);

                // Aspect type info
                String aspectName = L10NHelpers.localize(aspect.getTranslationKey());
                RenderHelpers.drawScaledCenteredString(font, aspectName,
                        this.guiLeft + offsetX + 26,
                        this.guiTop + offsetY + 25 + aspectBoxHeight * i,
                        getMaxLabelWidth(), Helpers.RGBToInt(40, 40, 40));

                drawAdditionalElementInfo(container, i, aspect);

                if(aspectPropertyButtons.containsKey(aspect)) {
                    ButtonText button = aspectPropertyButtons.get(aspect);
                    button.x = this.guiLeft + offsetX + 116;
                    button.y = this.guiTop + offsetY + 20 + aspectBoxHeight * i;
                }
            }
        }
    }

    protected abstract void drawAdditionalElementInfo(C container, int index, A aspect);

    protected Rectangle getElementPosition(C container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.guiLeft : 0),
                             ITEM_POSITION.y + container.getAspectBoxHeight() * i + offsetY + (absolute ? this.guiTop : 0),
                             ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        C container = getContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                // Item icon tooltip
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<ITextComponent> lines = Lists.newLinkedList();
                    container.getVisibleElement(i).loadTooltip(lines, true);
                    drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                drawAdditionalElementInfoForeground(container, i, container.getVisibleElement(i), mouseX, mouseY);

                // Optional aspect properties tooltip
                IAspect aspect = container.getVisibleElement(i);
                if(aspectPropertyButtons.containsKey(aspect)) {
                    ButtonText button = aspectPropertyButtons.get(aspect);
                    int x = button.x - guiLeft;
                    int y = button.y - guiTop;
                    if(isPointInRegion(x, y, button.getWidth(), button.getHeight(), mouseX, mouseY)) {
                        List<ITextComponent> lines = Lists.newLinkedList();
                        lines.add(new TranslationTextComponent("gui.integrateddynamics.part.properties")
                                .applyTextStyle(TextFormatting.WHITE));
                        for(IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                            lines.add(new StringTextComponent("-")
                                    .applyTextStyle(TextFormatting.YELLOW)
                                    .appendSibling(new TranslationTextComponent(property.getTranslationKey())));
                        }
                        drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                    }
                }
            }
        }
    }

    protected abstract void drawAdditionalElementInfoForeground(C container, int index,
                                                                A aspect, int mouseX, int mouseY);

    public int getMaxLabelWidth() {
        return 63;
    }
}
