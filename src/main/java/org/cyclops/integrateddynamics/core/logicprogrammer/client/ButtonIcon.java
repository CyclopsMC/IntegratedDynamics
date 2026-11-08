package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonExtended;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.integrateddynamics.client.gui.image.Images;

import java.util.function.Supplier;

/**
 * A small square button with an icon, as the font has no symbols such as undo arrows.
 * @author rubensworks
 */
public class ButtonIcon extends ButtonExtended {

    public static final int SIZE = 12;
    // The colors of the text of text buttons
    private static final int COLOR_INACTIVE = ARGB.color(160, 160, 160);
    private static final int COLOR_HOVERED = ARGB.color(255, 255, 160);
    private static final int COLOR = ARGB.color(224, 224, 224);

    private final Supplier<Icon> icon;

    /**
     * @param x The x position.
     * @param y The y position.
     * @param narrationMessage What it does, for narration.
     * @param icon The icon, which can change depending on the state.
     * @param pressable What it does.
     */
    public ButtonIcon(int x, int y, Component narrationMessage, Supplier<Icon> icon, Button.OnPress pressable) {
        super(x, y, SIZE, SIZE, narrationMessage, pressable, true);
        this.icon = icon;
    }

    @Override
    protected void drawButtonInner(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int color = !active ? COLOR_INACTIVE : isHoveredOrFocused() ? COLOR_HOVERED : COLOR;
        Image image = this.icon.get().getImage();
        int x = getX() + (width - image.getWidth()) / 2;
        int y = getY() + (height - image.getHeight()) / 2;
        // With a shadow, as text has
        drawWithColor(guiGraphics, image, x + 1, y + 1, 0.25F, color);
        drawWithColor(guiGraphics, image, x, y, 1F, color);
    }

    private static void drawWithColor(GuiGraphicsExtractor guiGraphics, Image image, int x, int y, float brightness, int color) {
        image.drawWithColor(guiGraphics, x, y, ARGB.red(color) / 255F * brightness,
                ARGB.green(color) / 255F * brightness, ARGB.blue(color) / 255F * brightness, 1F);
    }

    public static enum Icon {
        UNDO(Images.ICON_UNDO),
        REDO(Images.ICON_REDO),
        COPY(Images.ICON_COPY),
        CHECK(Images.ICON_CHECK),
        FORMAT(Images.ICON_FORMAT);

        private final Image image;

        Icon(Image image) {
            this.image = image;
        }

        public Image getImage() {
            return image;
        }
    }

}
