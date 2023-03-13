package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Gui for parts.
 * @author rubensworks
 */
public abstract class ContainerScreenMultipartAspects<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect, C extends ContainerMultipartAspects<P, S, A>>
        extends ContainerScreenScrolling<C> {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

    private Map<IAspect, ButtonText> aspectPropertyButtons = Maps.newHashMap();

    public ContainerScreenMultipartAspects(C container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected Rectangle getScrollRegion() {
        return new Rectangle(this.leftPos + 9, this.topPos + 18, 160, 105);
    }

    @Override
    public void init() {
        clearWidgets();
        super.init();
        if(getMenu().getPartType().getContainerProviderSettings(null).isPresent()) {
            addRenderableWidget(new ButtonImage(this.leftPos - 20, this.topPos + 0, 18, 18,
                    Component.translatable("gui.integrateddynamics.partsettings"),
                    createServerPressable(ContainerMultipartAspects.BUTTON_SETTINGS, (button) -> {}),
                    new IImage[]{
                            org.cyclops.integrateddynamics.client.gui.image.Images.BUTTON_BACKGROUND_INACTIVE,
                            org.cyclops.integrateddynamics.client.gui.image.Images.BUTTON_MIDDLE_SETTINGS
                    },
                    false, 0, 0));
            if (getMenu().getPartType().supportsOffsets()) {
                addRenderableWidget(new ButtonImage(this.leftPos - 20, this.topPos + 20, 18, 18,
                        Component.translatable("gui.integrateddynamics.part_offsets"),
                        createServerPressable(ContainerMultipartAspects.BUTTON_OFFSETS, (button) -> {
                        }),
                        new IImage[]{
                                org.cyclops.integrateddynamics.client.gui.image.Images.BUTTON_BACKGROUND_INACTIVE,
                                org.cyclops.integrateddynamics.client.gui.image.Images.BUTTON_MIDDLE_OFFSET
                        },
                        false, 0, 0));
            }
        }
        for(Map.Entry<IAspect, String> entry : getMenu().getAspectPropertyButtons().entrySet()) {
            ButtonText button = new ButtonText(-20, -20, 10, 10,
                    Component.translatable("gui.integrateddynamics.aspect_settings"), Component.literal("+"),
                    createServerPressable(entry.getValue(), b -> {}), true);
            aspectPropertyButtons.put(entry.getKey(), button);
            addRenderableWidget(button);
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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        // Reset button positions
        for(Map.Entry<IAspect, ButtonText> entry : this.aspectPropertyButtons.entrySet()) {
            entry.getValue().x = -20;
            entry.getValue().y = -20;
        }

        // Draw part name
        RenderHelpers.drawScaledCenteredString(matrixStack, font, title.getString(),
                this.leftPos + offsetX + 6, this.topPos + offsetY + 10, 70, 4210752);

        // Draw aspects
        C container = getMenu();
        int aspectBoxHeight = container.getAspectBoxHeight();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                A aspect = container.getVisibleElement(i);

                //GlStateManager._disableAlphaTest();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(aspect.getValueType().getDisplayColor());
                RenderSystem.setShaderColor(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                        colorSmoothener(rgb.getRight()), 1);

                // Background
                RenderHelpers.bindTexture(texture);
                blit(matrixStack, leftPos + offsetX + 9,
                        topPos + offsetY + 18 + aspectBoxHeight * i, 0, getBaseYSize(), 160, aspectBoxHeight - 1);

                // Aspect type info
                String aspectName = L10NHelpers.localize(aspect.getTranslationKey());
                RenderHelpers.drawScaledCenteredString(matrixStack, font, aspectName,
                        this.leftPos + offsetX + 26,
                        this.topPos + offsetY + 25 + aspectBoxHeight * i,
                        getMaxLabelWidth(), Helpers.RGBToInt(40, 40, 40));

                drawAdditionalElementInfo(matrixStack, container, i, aspect);

                if(aspectPropertyButtons.containsKey(aspect)) {
                    ButtonText button = aspectPropertyButtons.get(aspect);
                    button.x = this.leftPos + offsetX + 116;
                    button.y = this.topPos + offsetY + 20 + aspectBoxHeight * i;
                }
            }
        }
    }

    protected abstract void drawAdditionalElementInfo(PoseStack matrixStack, C container, int index, A aspect);

    protected Rectangle getElementPosition(C container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.leftPos : 0),
                             ITEM_POSITION.y + container.getAspectBoxHeight() * i + offsetY + (absolute ? this.topPos : 0),
                             ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        C container = getMenu();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                // Item icon tooltip
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<Component> lines = Lists.newLinkedList();
                    container.getVisibleElement(i).loadTooltip(lines, true);
                    drawTooltip(lines, matrixStack, mouseX - this.leftPos, mouseY - this.topPos);
                }
                drawAdditionalElementInfoForeground(matrixStack, container, i, container.getVisibleElement(i), mouseX, mouseY);

                // Optional aspect properties tooltip
                IAspect aspect = container.getVisibleElement(i);
                if(aspectPropertyButtons.containsKey(aspect)) {
                    ButtonText button = aspectPropertyButtons.get(aspect);
                    int x = button.x - leftPos;
                    int y = button.y - topPos;
                    if(isHovering(x, y, button.getWidth(), button.getHeight(), mouseX, mouseY)) {
                        List<Component> lines = Lists.newLinkedList();
                        lines.add(Component.translatable("gui.integrateddynamics.part.properties")
                                .withStyle(ChatFormatting.WHITE));
                        for(IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                            lines.add(Component.literal("-")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.translatable(property.getTranslationKey())));
                        }
                        drawTooltip(lines, matrixStack, mouseX - this.leftPos, mouseY - this.topPos);
                    }
                }
            }
        }

        if (isHovering(-20, 0, 18, 18, mouseX, mouseY)) {
            drawTooltip(Lists.newArrayList(Component.translatable("gui.integrateddynamics.part_settings")), matrixStack, mouseX - leftPos, mouseY - topPos);
        }
        if (isHovering(-20, 20, 18, 18, mouseX, mouseY)) {
            drawTooltip(Lists.newArrayList(Component.translatable("gui.integrateddynamics.part_offsets")), matrixStack, mouseX - leftPos, mouseY - topPos);
        }
    }

    protected abstract void drawAdditionalElementInfoForeground(PoseStack matrixStack, C container, int index,
                                                                A aspect, int mouseX, int mouseY);

    public int getMaxLabelWidth() {
        return 63;
    }
}
