package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.gui.image.Images;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.inventory.container.ContainerWrenchConfig;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 * Gui for looking at the configuration inside a Wrench,
 * and switching off the parts of it that should not be pasted.
 * @author rubensworks
 */
public class ContainerScreenWrenchConfig extends ContainerScreenScrolling<ContainerWrenchConfig> {

    private static final int BOX_WIDTH = 160;
    private static final int BOX_HEIGHT = 18;
    private static final int BOX_X = 9;
    private static final int BOX_Y = 18;
    private static final int BUTTON_X = 12;
    private static final int ICON_X = 26;
    private static final int LABEL_X = 27;
    private static final int LABEL_X_ICON = 45;
    private static final int VALUE_X = 103;
    private static final int VALUE_WIDTH = 63;
    private static final int VALUE_HEIGHT = 10;

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
        return new Rectangle(this.leftPos + BOX_X, this.topPos + BOX_Y, BOX_WIDTH, 105);
    }

    @Override
    public void init() {
        clearWidgets();
        this.entryButtons.clear();
        super.init();

        for (PartConfigEntry entry : getMenu().getEntries()) {
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
            int x = this.leftPos + offsetX;
            int y = this.topPos + offsetY + BOX_Y + BOX_HEIGHT * i;

            Triple<Float, Float, Float> rgb = Helpers.intToRGB(
                    SECTION_COLORS.getOrDefault(entry.section(), Helpers.RGBToInt(255, 255, 255)));
            RenderSystem.setShaderColor(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                    colorSmoothener(rgb.getRight()), 1);
            guiGraphics.blit(texture, x + BOX_X, y, 0, getBaseYSize(), BOX_WIDTH, BOX_HEIGHT - 1);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            if (!entry.icon().isEmpty()) {
                Lighting.setupForFlatItems();
                guiGraphics.renderItem(entry.icon(), x + ICON_X, y + 1);
            }

            ButtonCheckbox button = this.entryButtons.get(entry.id());
            if (button != null) {
                button.setChecked(container.isEntryEnabled(entry.id()));
                button.setX(x + BUTTON_X);
                button.setY(y + 3);
            }
        }
    }

    // Everything that has to end up on top of the buttons is drawn here, as the buttons are drawn after the background
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The gui has no room for the usual labels, the name of the Wrench goes in the bar at the top instead
        guiGraphics.drawString(font, this.title, offsetX + 8, offsetY + 6,
                Helpers.RGBToInt(64, 64, 64), false);

        PartConfigEntry hovered = null;
        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + BOX_Y + BOX_HEIGHT * i;
            int labelX = entry.icon().isEmpty() ? LABEL_X : LABEL_X_ICON;
            String value = entry.value().getString();
            // Without a value there is no box to leave room for, so the name may take the whole row
            int labelWidth = (value.isEmpty() ? BOX_X + BOX_WIDTH - 3 : VALUE_X - 3) - labelX;

            String group = entry.group().getString();
            if (group.isEmpty()) {
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        entry.label().getString(), offsetX + labelX, y + 9,
                        labelWidth, Helpers.RGBToInt(40, 40, 40), false, Font.DisplayMode.NORMAL);
            } else {
                // The group goes above the entry itself, so that the aspect a property belongs to is always visible
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        group, offsetX + labelX, y + 4, labelWidth, 0.5F, labelWidth,
                        getColor(entry.group(), Helpers.RGBToInt(120, 120, 120)), false, Font.DisplayMode.NORMAL);
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        entry.label().getString(), offsetX + labelX, y + 11,
                        labelWidth, Helpers.RGBToInt(40, 40, 40), false, Font.DisplayMode.NORMAL);
            }

            if (!value.isEmpty()) {
                drawValueBox(guiGraphics, offsetX + VALUE_X, y + 4);
                // In the colour of its value type, the same way that the part gui shows a property value
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        value, offsetX + VALUE_X + 2, y + 9, VALUE_WIDTH - 4, 1F, VALUE_WIDTH - 4,
                        getColor(entry.value(), Helpers.RGBToInt(255, 255, 255)), false, Font.DisplayMode.NORMAL);
            }

            if (isHovering(offsetX + BOX_X, y, BOX_WIDTH, BOX_HEIGHT - 1, mouseX, mouseY)) {
                hovered = entry;
            }
        }

        // After the rows, as the rows that come below the hovered one would otherwise draw over the tooltip
        if (hovered != null) {
            drawTooltip(getEntryTooltip(hovered), guiGraphics.pose(),
                    mouseX - this.leftPos, mouseY - this.topPos);
        }
    }

    /**
     * @param entry An entry that is shown.
     * @return What that entry holds, in full, as the row itself has no room for all of it.
     */
    protected List<Component> getEntryTooltip(PartConfigEntry entry) {
        List<Component> lines = Lists.newArrayList();
        lines.add(entry.label().copy().withStyle(ChatFormatting.WHITE));
        if (!entry.group().getString().isEmpty()) {
            lines.add(entry.group().copy());
        }
        if (!entry.value().getString().isEmpty()) {
            lines.add(Component.translatable("gui.integrateddynamics.wrench_config.entry.value",
                    entry.value().copy()).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable(entry.section().getTranslationKey())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return lines;
    }

    /**
     * An inset box in the style of the rest of this gui, to set a value apart from the name next to it.
     */
    protected void drawValueBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + VALUE_WIDTH, y + VALUE_HEIGHT, 0xFF000000 | Helpers.RGBToInt(55, 55, 55));
        guiGraphics.fill(x + 1, y + 1, x + VALUE_WIDTH + 1, y + VALUE_HEIGHT + 1,
                0xFF000000 | Helpers.RGBToInt(255, 255, 255));
        guiGraphics.fill(x + 1, y + 1, x + VALUE_WIDTH, y + VALUE_HEIGHT,
                0xFF000000 | Helpers.RGBToInt(139, 139, 139));
    }

    protected int getColor(Component component, int fallback) {
        TextColor color = component.getStyle().getColor();
        return color == null ? fallback : color.getValue();
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
