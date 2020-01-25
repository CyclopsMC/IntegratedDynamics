package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

import java.util.Comparator;

/**
 * A gui part that can be rendered withing another gui.
 * @author rubensworks
 */
public interface ISubGui {

    public void init(int guiLeft, int guiTop);

    public void tick();

    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY);

    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY);

    /**
     * Char type event
     * @param typedChar The character typed
     * @param keyCode The keycode of the character typed
     * @return True if all next actions should be skipped
     */
    public boolean charTyped(char typedChar, int keyCode);

    /**
     * Key press event
     * @param typedChar The character typed
     * @param keyCode The keycode of the character typed
     * @param modifiers Key modifiers
     * @return True if all next actions should be skipped
     */
    public boolean keyPressed(int typedChar, int keyCode, int modifiers);

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton);

    public static class SubGuiComparator implements Comparator<ISubGui> {

        @Override
        public int compare(ISubGui o1, ISubGui o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    }

}
