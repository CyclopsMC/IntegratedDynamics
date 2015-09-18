package org.cyclops.integrateddynamics.core.client.gui.subgui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Comparator;

/**
 * A gui part that can be rendered withing another gui.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public interface ISubGui {

    public void initGui(int guiLeft, int guiTop);

    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY);

    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY);

    /**
     * Key type event
     * @param checkHotbarKeys If the hotbar keys should be checked
     * @param typedChar The character typed
     * @param keyCode The keycode of the character typed
     * @return True if all next actions should be skipped
     * @throws IOException An exception with IO.
     */
    public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException;

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;

    public static class SubGuiComparator implements Comparator<ISubGui> {

        @Override
        public int compare(ISubGui o1, ISubGui o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    }

}
