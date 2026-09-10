package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integrateddynamics.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.inventory.container.ContainerWrenchConfig;

import java.awt.Rectangle;
import java.util.Map;

/**
 * Gui for looking at the configuration inside a Wrench,
 * and switching off the parts of it that should not be pasted.
 * @author rubensworks
 */
public class ContainerScreenWrenchConfig extends ContainerScreenScrolling<ContainerWrenchConfig> {

    private static final int BOX_HEIGHT = 18;
    private static final int BUTTON_X = 12;
    private static final int LABEL_X = 29;
    private static final int LABEL_WIDTH = 74;
    private static final int VALUE_X = 105;
    private static final int VALUE_WIDTH = 60;

    /**
     * The colour that the row of an entry is tinted in, so that the sections stay apart at a glance.
     */
    private static final Map<PartConfigSection, Integer> SECTION_COLORS = Map.of(
            PartConfigSection.PART_SETTINGS, Helpers.RGBToInt(120, 160, 215),
            PartConfigSection.ASPECT, Helpers.RGBToInt(215, 170, 100));

    private final Map<String, ButtonCheckbox> entryButtons = Maps.newHashMap();

    public ContainerScreenWrenchConfig(ContainerWrenchConfig container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected Rectangle getScrollRegion() {
        return new Rectangle(this.leftPos + 9, this.topPos + 18, 160, 105);
    }

    @Override
    public void init() {
        clearWidgets();
        this.entryButtons.clear();
        super.init();

        for (PartConfigEntry entry : getMenu().getEntries()) {
            // Parked outside of the gui until it turns out to be on the page that is shown
            // Parked outside of the gui until it turns out to be on the page that is shown
            ButtonCheckbox button = new ButtonCheckbox(-20, -20, 12, 12,
                    Component.translatable("gui.integrateddynamics.wrench_config.entry"),
                    createServerPressable(entry.id(), b -> {}));
            this.entryButtons.put(entry.id(), button);
            addRenderableWidget(button);
        }

        // Next to the gui, where this mod puts the buttons of a part as well
        addRenderableWidget(new ButtonImage(this.leftPos - 20, this.topPos, 18, 18,
                Component.translatable("gui.integrateddynamics.wrench_config.toggle_all"),
                createServerPressable(ContainerWrenchConfig.BUTTON_TOGGLE_ALL, b -> {}),
                new IImage[]{Images.BUTTON_BACKGROUND_INACTIVE, Images.BUTTON_MIDDLE_CHECK_ALL},
                false, 0, 0));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/wrench_config.png");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        for (ButtonCheckbox button : this.entryButtons.values()) {
            button.setX(-20);
            button.setY(-20);
        }

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = this.topPos + offsetY + 18 + BOX_HEIGHT * i;

            Triple<Float, Float, Float> rgb = Helpers.intToRGB(
                    SECTION_COLORS.getOrDefault(entry.section(), Helpers.RGBToInt(255, 255, 255)));
            RenderSystem.setShaderColor(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                    colorSmoothener(rgb.getRight()), 1);
            guiGraphics.blit(texture, this.leftPos + offsetX + 9, y, 0, getBaseYSize(), 160, BOX_HEIGHT - 1);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            ButtonCheckbox button = this.entryButtons.get(entry.id());
            if (button != null) {
                button.setChecked(container.isEntryEnabled(entry.id()));
                button.setX(this.leftPos + offsetX + BUTTON_X);
                button.setY(y + 3);
            }
        }
    }

    // Everything that has to end up on top of the buttons is drawn here, as the buttons are drawn after the background
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The gui has no room for the usual labels, the name of the Wrench goes in the bar at the top instead
        RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                this.title.getString(), offsetX + 6, offsetY + 10, 80,
                Helpers.RGBToInt(64, 64, 64), false, Font.DisplayMode.NORMAL);

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + 18 + BOX_HEIGHT * i;

            // The group above the entry itself, so that the aspect that a property belongs to is always visible
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                    entry.group().getString(), offsetX + LABEL_X, y + 4,
                    LABEL_WIDTH, 0.5F, LABEL_WIDTH, Helpers.RGBToInt(120, 120, 120), false, Font.DisplayMode.NORMAL);
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                    entry.label().getString(), offsetX + LABEL_X, y + 10,
                    LABEL_WIDTH, Helpers.RGBToInt(40, 40, 40), false, Font.DisplayMode.NORMAL);
            String value = entry.value().getString();
            if (!value.isEmpty()) {
                // In the colour of its value type, the same way that the part gui shows a property value
                TextColor color = entry.value().getStyle().getColor();
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        value, offsetX + VALUE_X, y + 10, VALUE_WIDTH,
                        color == null ? Helpers.RGBToInt(90, 90, 90) : color.getValue(),
                        false, Font.DisplayMode.NORMAL);
            }
        }
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @Override
    protected int getBaseXSize() {
        return 195;
    }

    @Override
    protected int getBaseYSize() {
        return 222;
    }

}
