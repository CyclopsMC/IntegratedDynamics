package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;

import java.util.Comparator;

/**
 * A gui part that can be rendered withing another gui.
 * @author rubensworks
 */
public interface ISubGui {

    public void init(int guiLeft, int guiTop);

    public void tick();

    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY);

    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY);

    /**
     * Char type event
     * @param evt The character event
     * @return True if all next actions should be skipped
     */
    public boolean charTyped(CharacterEvent evt);

    /**
     * Key press event
     * @param evt The key event
     * @return True if all next actions should be skipped
     */
    public boolean keyPressed(KeyEvent evt);

    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick);

    public static class SubGuiComparator implements Comparator<ISubGui> {

        @Override
        public int compare(ISubGui o1, ISubGui o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    }

}
