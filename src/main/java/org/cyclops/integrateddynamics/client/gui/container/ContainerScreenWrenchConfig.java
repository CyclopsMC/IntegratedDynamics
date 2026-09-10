package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
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
    private static final int LABEL_X = 27;
    private static final int BUTTON_X = 13;
    private static final int SEARCH_WIDTH = 60;
    private static final int TOGGLE_ALL_X = 146;
    private static final int LABEL_WIDTH = 138;

    private final Map<String, ButtonText> entryButtons = Maps.newHashMap();

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
            // The text of a ButtonText can not change afterwards, so the mark is drawn on top of it instead
            ButtonText button = new ButtonText(-20, -20, 10, 10,
                    Component.translatable("gui.integrateddynamics.wrench_config.entry"), Component.empty(),
                    createServerPressable(entry.id(), b -> {}), true);
            this.entryButtons.put(entry.id(), button);
            addRenderableWidget(button);
        }

        addRenderableWidget(new ButtonText(this.leftPos + TOGGLE_ALL_X, this.topPos + 5, 28, 12,
                Component.translatable("gui.integrateddynamics.wrench_config.toggle_all"),
                Component.translatable("gui.integrateddynamics.wrench_config.toggle_all.label"),
                createServerPressable(ContainerWrenchConfig.BUTTON_TOGGLE_ALL, b -> {}), true));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/wrench_config.png");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        for (ButtonText button : this.entryButtons.values()) {
            button.setX(-20);
            button.setY(-20);
        }

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            int y = this.topPos + offsetY + 18 + BOX_HEIGHT * i;
            guiGraphics.blit(texture, this.leftPos + offsetX + 9, y, 0, getBaseYSize(), 160, BOX_HEIGHT - 1);

            ButtonText button = this.entryButtons.get(container.getVisibleElement(i).id());
            if (button != null) {
                button.setX(this.leftPos + offsetX + BUTTON_X);
                button.setY(y + 4);
            }
        }
    }

    @Override
    protected int getSearchWidth() {
        // Shortened, so that the button to switch everything on or off fits next to it
        return SEARCH_WIDTH;
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

            // An empty box for what is switched off, so that a full page of entries stays scannable
            if (container.isEntryEnabled(entry.id())) {
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font,
                        "x", offsetX + BUTTON_X, y + 9, 10,
                        Helpers.RGBToInt(40, 40, 40), false, Font.DisplayMode.NORMAL);
            }
        }
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
