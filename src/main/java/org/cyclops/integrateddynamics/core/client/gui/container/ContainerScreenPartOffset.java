package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartOffset;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Gui for part offsets.
 * @author rubensworks
 */
public class ContainerScreenPartOffset<T extends ContainerPartOffset> extends ContainerScreenExtended<T> {

    private WidgetNumberField numberFieldX = null;
    private WidgetNumberField numberFieldY = null;
    private WidgetNumberField numberFieldZ = null;
    private ButtonText buttonSave = null;

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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/part_offsets.png");
    }

    @Override
    public void init() {
        super.init();

        numberFieldX = new WidgetNumberField(font, leftPos + 107 - 54 - 7 - 18, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldX.setMaxLength(4);
        numberFieldX.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldX.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldX.setVisible(true);
        numberFieldX.setTextColor(ARGB.opaque(16777215));
        numberFieldX.setCanLoseFocus(true);

        numberFieldY = new WidgetNumberField(font, leftPos + 107 - 54 + 36 - 7, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldY.setMaxLength(4);
        numberFieldY.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldY.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldY.setVisible(true);
        numberFieldY.setTextColor(ARGB.opaque(16777215));
        numberFieldY.setCanLoseFocus(true);

        numberFieldZ = new WidgetNumberField(font, leftPos + 107 - 54 + 72 - 7 + 18, topPos + 33, 46, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.partoffset.x"), true);
        numberFieldZ.setMaxLength(4);
        numberFieldZ.setMaxValue(GeneralConfig.maxPartOffset);
        numberFieldZ.setMinValue(-GeneralConfig.maxPartOffset);
        numberFieldZ.setVisible(true);
        numberFieldZ.setTextColor(ARGB.opaque(16777215));
        numberFieldZ.setCanLoseFocus(true);

        MutableComponent save = Component.translatable("gui.integrateddynamics.button.save");
        addRenderableWidget(buttonSave = new ButtonText(this.leftPos + 178, this.topPos + 6, font.width(save.getVisualOrderText()) + 6, 16, save, save,
                createServerPressable(ContainerPartOffset.BUTTON_SAVE, b -> onSave()), true));

        this.refreshValues();
    }

    @Override
    public void onClose() {
        // Auto-save the offsets when the gui is closed,
        // so that players don't have to explicitly confirm their changes.
        onSave();
        super.onClose();
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (!this.numberFieldX.charTyped(evt)
                && !this.numberFieldY.charTyped(evt)
                && !this.numberFieldZ.charTyped(evt)) {
            onSave();
            return super.charTyped(evt);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (evt.key() != GLFW.GLFW_KEY_ESCAPE) {
            if (this.numberFieldX.keyPressed(evt)
                    || this.numberFieldY.keyPressed(evt)
                    || this.numberFieldZ.keyPressed(evt)) {
                onSave();
                return true;
            }
            return true;
        } else {
            // Don't close all GUIs, but go back to the part's GUI.
            exitToPartGui(evt);
            return true;
        }
    }

    /**
     * Save the current offsets and go back to the GUI of the part.
     */
    protected void exitToPartGui(InputWithModifiers input) {
        buttonSave.onPress(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        if (this.numberFieldX.mouseClicked(evt, isDoubleClick)
                || this.numberFieldY.mouseClicked(evt, isDoubleClick)
                || this.numberFieldZ.mouseClicked(evt, isDoubleClick)) {
            onSave();
            return true;
        }
        return super.mouseClicked(evt, isDoubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.text(font, Component.translatable("gui.integrateddynamics.part_offsets"), this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);

        guiGraphics.text(font, "X", leftPos + 45 + 5, topPos + 19, ARGB.opaque(0), false);
        guiGraphics.text(font, "Y", leftPos + 99 + 5, topPos + 19, ARGB.opaque(0), false);
        guiGraphics.text(font, "Z", leftPos + 153 + 5, topPos + 19, ARGB.opaque(0), false);

        numberFieldX.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        numberFieldY.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        numberFieldZ.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        for (int i = 0; i < 3; i++) {
            int x = leftPos + 64 + i * 54;
            if (getMenu().isOffsetVariableFilled(i)) {
                IImage image = container.getOffsetVariableError(i) == null ? Images.OK : Images.ERROR;
                image.draw(guiGraphics, x, topPos + 52);
            }
        }

        if (getMenu().getMaxOffset() == 0) {
            Images.ERROR.draw(guiGraphics, leftPos + 74, topPos + 3);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (isHovering(0, 0, 90, 18, mouseX, mouseY)) {
            List<Component> lines = Lists.newArrayList(
                    Component.translatable("gui.integrateddynamics.partoffset.offsets"),
                    Component.translatable("gui.integrateddynamics.partoffset.offsets.max", getMenu().getMaxOffset())
                            .withStyle(ChatFormatting.GRAY)
            );
            if (getMenu().getMaxOffset() == 0) {
                lines.add(Component.translatable("gui.integrateddynamics.partoffset.offsets.max.howtoincrease", getMenu().getMaxOffset())
                        .withStyle(ChatFormatting.RED));
            }
            drawTooltip(lines, guiGraphics, mouseX, mouseY);
        }

        for (int i = 0; i < 3; i++) {
            int x = 64 + i * 54;
            int slot = i;
            IModHelpers.get().getGuiHelpers().renderTooltipOptional(this, guiGraphics, x, 52, 14, 13, mouseX, mouseY,
                    () -> {
                        Component unlocalizedMessage = container.getOffsetVariableError(slot);
                        if (unlocalizedMessage != null) {
                            return Optional.of(Collections.singletonList(unlocalizedMessage));
                        }
                        return Optional.empty();
                    });
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

        numberFieldX.setEditable(!getMenu().isOffsetVariableFilled(0));
        numberFieldY.setEditable(!getMenu().isOffsetVariableFilled(1));
        numberFieldZ.setEditable(!getMenu().isOffsetVariableFilled(2));

        if (valueId == getMenu().getMaxOffsetId()) {
            int max = getMenu().getMaxOffset();
            numberFieldX.setMaxValue(max);
            numberFieldX.setMinValue(-max);
            numberFieldY.setMaxValue(max);
            numberFieldY.setMinValue(-max);
            numberFieldZ.setMaxValue(max);
            numberFieldZ.setMinValue(-max);
        }
    }

}
