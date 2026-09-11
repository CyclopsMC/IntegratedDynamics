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
    private static final int TITLE_X = 8;
    /**
     * How much room there is left of the search field.
     */
    private static final int TITLE_WIDTH = 72;
    private static final int LABEL_X = 27;
    private static final int VALUE_X = 103;
    /**
     * The same for every row, so that the names stay under each other
     * whatever a row happens to show on its right.
     */
    private static final int LABEL_WIDTH = VALUE_X - 3 - LABEL_X;
    private static final int VALUE_WIDTH = 63;
    private static final int VALUE_HEIGHT = 10;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_X = VALUE_X + VALUE_WIDTH - SLOT_SIZE;
    /**
     * A slot is a pixel taller than the row that holds it, so it hangs over the line above rather than the one below.
     */
    private static final int SLOT_Y = -1;
    /**
     * Where an empty slot sits inside the gui texture, which is where the player inventory starts.
     */
    private static final int SLOT_TEXTURE_X = 8;
    private static final int SLOT_TEXTURE_Y = 139;

    /**
     * What the row of an entry that belongs to no value type is tinted in, which is nothing at all.
     */
    private static final int COLOR_NEUTRAL = Helpers.RGBToInt(255, 255, 255);

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

            // The same colour that a part gui gives the value type of an aspect
            Triple<Float, Float, Float> rgb = Helpers.intToRGB(entry.color().orElse(COLOR_NEUTRAL));
            RenderSystem.setShaderColor(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                    colorSmoothener(rgb.getRight()), 1);
            guiGraphics.blit(texture, x + BOX_X, y, 0, getBaseYSize(), BOX_WIDTH, BOX_HEIGHT - 1);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            // In a slot of its own, so that a card looks the same here as it does inside a part
            if (!entry.icon().isEmpty()) {
                guiGraphics.blit(texture, x + SLOT_X, y + SLOT_Y, SLOT_TEXTURE_X, SLOT_TEXTURE_Y,
                        SLOT_SIZE, SLOT_SIZE);
                Lighting.setupForFlatItems();
                guiGraphics.renderItem(entry.icon(), x + SLOT_X + 1, y + SLOT_Y + 1);
                guiGraphics.renderItemDecorations(font, entry.icon(), x + SLOT_X + 1, y + SLOT_Y + 1);
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
        // The gui has no room for the usual labels, what it holds goes in the bar at the top instead,
        // shrunk when needed so that it never runs into the search field next to it
        RenderHelpers.drawScaledString(guiGraphics, font, this.title.getString(), offsetX + TITLE_X, offsetY + 6,
                Math.min(1F, (float) TITLE_WIDTH / font.width(this.title)),
                Helpers.RGBToInt(64, 64, 64), false, Font.DisplayMode.NORMAL);

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + BOX_Y + BOX_HEIGHT * i;
            String value = entry.value().getString();

            String group = entry.group().getString();
            if (group.isEmpty()) {
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        entry.label().getString(), offsetX + LABEL_X, y + 9, LABEL_WIDTH,
                        getColor(entry.label(), Helpers.RGBToInt(40, 40, 40)), false, Font.DisplayMode.NORMAL);
            } else {
                // The group goes above the entry itself, so that the aspect a property belongs to is always visible
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        group, offsetX + LABEL_X, y + 4, LABEL_WIDTH, 0.5F, LABEL_WIDTH,
                        getColor(entry.group(), Helpers.RGBToInt(120, 120, 120)), false, Font.DisplayMode.NORMAL);
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        entry.label().getString(), offsetX + LABEL_X, y + 11, LABEL_WIDTH,
                        getColor(entry.label(), Helpers.RGBToInt(40, 40, 40)), false, Font.DisplayMode.NORMAL);
            }

            if (!value.isEmpty()) {
                // A row that also holds a slot leaves room for it
                int valueWidth = entry.icon().isEmpty() ? VALUE_WIDTH : SLOT_X - 2 - VALUE_X;
                drawValueBox(guiGraphics, offsetX + VALUE_X, y + 4, valueWidth);
                // In the colour of its value type, the same way that the part gui shows a property value
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        value, offsetX + VALUE_X + 2, y + 9, valueWidth - 4, 1F, valueWidth - 4,
                        getColor(entry.value(), Helpers.RGBToInt(255, 255, 255)), false, Font.DisplayMode.NORMAL);
            }

        }
    }

    // Tooltips are drawn here rather than with the rows,
    // as inside the gui they would be kept away from the wrong edge of the screen
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + BOX_Y + BOX_HEIGHT * i;
            if (!entry.icon().isEmpty()
                    && isHovering(offsetX + SLOT_X, y + SLOT_Y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                // The card itself is shown, so it tells the player what it holds just like it does anywhere else
                guiGraphics.renderTooltip(font, getTooltipFromItem(this.minecraft, entry.icon()),
                        entry.icon().getTooltipImage(), mouseX, mouseY);
                return;
            }
            if (isHovering(offsetX + BOX_X, y, BOX_WIDTH, BOX_HEIGHT - 1, mouseX, mouseY)) {
                guiGraphics.renderComponentTooltip(font, getEntryTooltip(entry), mouseX, mouseY);
                return;
            }
        }
    }

    /**
     * @param entry An entry that is shown.
     * @return What that entry holds, in full, as the row itself has no room for all of it.
     */
    protected List<Component> getEntryTooltip(PartConfigEntry entry) {
        List<Component> lines = Lists.newArrayList();
        // A name that carries the colour of a value type keeps it, as that is what tells two aspects apart
        lines.add(entry.label().getStyle().getColor() == null
                ? entry.label().copy().withStyle(ChatFormatting.WHITE) : entry.label().copy());
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
    protected void drawValueBox(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + VALUE_HEIGHT, 0xFF000000 | Helpers.RGBToInt(55, 55, 55));
        guiGraphics.fill(x + 1, y + 1, x + width + 1, y + VALUE_HEIGHT + 1,
                0xFF000000 | Helpers.RGBToInt(255, 255, 255));
        guiGraphics.fill(x + 1, y + 1, x + width, y + VALUE_HEIGHT,
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
