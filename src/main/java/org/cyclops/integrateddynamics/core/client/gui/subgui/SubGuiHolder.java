package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGui;

import java.io.IOException;
import java.util.Set;

/**
 * A subgui that itself can contain multiple {@link ISubGui} and delegates to them.
 * @author rubensworks
 */
public class SubGuiHolder implements ISubGui {

    private final Set<ISubGui> subGuis = Sets.newTreeSet(new ISubGui.SubGuiComparator());

    public void addSubGui(ISubGui subGui) {
        subGuis.add(subGui);
    }

    public boolean removeSubGui(ISubGui subGui) {
        return subGuis.remove(subGui);
    }

    public void clear() {
        subGuis.clear();
    }

    protected Set<ISubGui> getSubGuis() {
        return Sets.newHashSet(subGuis);
    }

    @Override
    public void initGui(int guiLeft, int guiTop) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.initGui(guiLeft, guiTop);
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
        for(ISubGui subGui : getSubGuis()) {
            if(subGui.keyTyped(checkHotbarKeys, typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for(ISubGui subGui : getSubGuis()) {
            subGui.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
