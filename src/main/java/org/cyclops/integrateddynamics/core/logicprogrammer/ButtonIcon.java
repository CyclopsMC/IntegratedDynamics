package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonExtended;

import java.util.function.Supplier;

/**
 * A small square button with an icon, drawn pixel by pixel, as the font has no symbols such as undo arrows.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ButtonIcon extends ButtonExtended {

    public static final int SIZE = 12;

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
    protected void drawButtonInner(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Same colors and shadow as the text of text buttons
        int color = !active ? 0xFFA0A0A0 : isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFFE0E0E0;
        int x = getX() + (width - Icon.SIZE) / 2;
        int y = getY() + (height - Icon.SIZE) / 2;
        Icon icon = this.icon.get();
        icon.draw(guiGraphics, x + 1, y + 1, (color & 0xFCFCFC) >> 2 | color & 0xFF000000);
        icon.draw(guiGraphics, x, y, color);
    }

    public static enum Icon {
        UNDO("..#....", ".##....", "######.", ".##...#", "..#...#", "......#", "..####."),
        REDO("....#..", "....##.", ".######", "#...##.", "#...#..", "#......", ".####.."),
        COPY("#####..", "#...#..", "#.#####", "#.#...#", "###...#", "..#...#", "..#####"),
        CHECK(".......", "......#", ".....##", "#...##.", "##.##..", ".###...", "..#...."),
        CROSS("#.....#", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#...#.", "#.....#");

        public static final int SIZE = 7;

        private final String[] rows;

        Icon(String... rows) {
            this.rows = rows;
        }

        public void draw(GuiGraphics guiGraphics, int x, int y, int color) {
            for (int row = 0; row < rows.length; row++) {
                String pixels = rows[row];
                int start = -1;
                for (int column = 0; column <= pixels.length(); column++) {
                    boolean filled = column < pixels.length() && pixels.charAt(column) == '#';
                    if (filled && start < 0) {
                        start = column;
                    } else if (!filled && start >= 0) {
                        guiGraphics.fill(x + start, y + row, x + column, y + row + 1, color);
                        start = -1;
                    }
                }
            }
        }
    }

}
