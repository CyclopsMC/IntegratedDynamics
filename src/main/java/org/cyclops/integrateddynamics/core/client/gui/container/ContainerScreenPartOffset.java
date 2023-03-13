package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartOffset;
import org.lwjgl.glfw.GLFW;

/**
 * Gui for part offsets.
 * @author rubensworks
 */
public class ContainerScreenPartOffset<T extends ContainerPartOffset> extends ContainerScreenExtended<T> {

    private WidgetNumberField numberFieldX = null;
    private WidgetNumberField numberFieldY = null;
    private WidgetNumberField numberFieldZ = null;

    public ContainerScreenPartOffset(T container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    protected void onSave() {
        try {
            ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastXValueId(), numberFieldX.getInt());
            ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastYValueId(), numberFieldY.getInt());
            ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastZValueId(), numberFieldZ.getInt());
        } catch (NumberFormatException e) { }
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/part_offsets.png");
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        numberFieldX = new WidgetNumberField(font, leftPos + 107 - 54 - 7 - 18, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldX.setMaxLength(4);
        numberFieldX.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldX.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldX.setVisible(true);
        numberFieldX.setTextColor(16777215);
        numberFieldX.setCanLoseFocus(true);

        numberFieldY = new WidgetNumberField(font, leftPos + 107 - 54 + 36 - 7, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldY.setMaxLength(4);
        numberFieldY.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldY.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldY.setVisible(true);
        numberFieldY.setTextColor(16777215);
        numberFieldY.setCanLoseFocus(true);

        numberFieldZ = new WidgetNumberField(font, leftPos + 107 - 54 + 72 - 7 + 18, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldZ.setMaxLength(4);
        numberFieldZ.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldZ.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldZ.setVisible(true);
        numberFieldZ.setTextColor(16777215);
        numberFieldZ.setCanLoseFocus(true);

        MutableComponent save = Component.translatable("gui.integrateddynamics.button.save");
        addRenderableWidget(new ButtonText(this.leftPos + 178, this.topPos + 6, font.width(save.getVisualOrderText()) + 6, 16, save, save,
                createServerPressable(ContainerPartOffset.BUTTON_SAVE, b -> onSave()), true));

        this.refreshValues();
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!this.numberFieldX.charTyped(typedChar, keyCode)
                && !this.numberFieldY.charTyped(typedChar, keyCode)
                && !this.numberFieldZ.charTyped(typedChar, keyCode)) {
            return super.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar != GLFW.GLFW_KEY_ESCAPE) {
            if (this.numberFieldX.keyPressed(typedChar, keyCode, modifiers)
                    || this.numberFieldY.keyPressed(typedChar, keyCode, modifiers)
                    || this.numberFieldZ.keyPressed(typedChar, keyCode, modifiers)) {
                return true;
            }
            return true;
        } else {
            return super.keyPressed(typedChar, keyCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.numberFieldX.mouseClicked(mouseX, mouseY, mouseButton)
                || this.numberFieldY.mouseClicked(mouseX, mouseY, mouseButton)
                || this.numberFieldZ.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        font.draw(matrixStack, "X", leftPos + 45 + 5, topPos + 19, Helpers.RGBToInt(0, 0, 0));
        font.draw(matrixStack, "Y", leftPos + 99 + 5, topPos + 19, Helpers.RGBToInt(0, 0, 0));
        font.draw(matrixStack, "Z", leftPos + 153 + 5, topPos + 19, Helpers.RGBToInt(0, 0, 0));
        numberFieldX.render(matrixStack, mouseX, mouseY, partialTicks);
        numberFieldY.render(matrixStack, mouseX, mouseY, partialTicks);
        numberFieldZ.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, Component.translatable("gui.integrateddynamics.part_offsets"), (float)this.titleLabelX, (float)this.titleLabelY, 4210752);

        if (isHovering(0, 0, 80, 18, mouseX, mouseY)) {
            drawTooltip(Lists.newArrayList(Component.translatable("gui.integrateddynamics.partoffset.offsets")), matrixStack, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    protected int getBaseXSize() {
        return 214;
    }

    @Override
    protected int getBaseYSize() {
        return 155;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        if (valueId == getMenu().getLastXValueId()) {
            numberFieldX.setValue(Integer.toString(getMenu().getLastXValue()));
        }
        if (valueId == getMenu().getLastYValueId()) {
            numberFieldY.setValue(Integer.toString(getMenu().getLastYValue()));
        }
        if (valueId == getMenu().getLastZValueId()) {
            numberFieldZ.setValue(Integer.toString(getMenu().getLastZValue()));
        }
    }

}
