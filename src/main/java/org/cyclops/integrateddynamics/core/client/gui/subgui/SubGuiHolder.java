package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

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

    @Override
    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        for(ISubGui subGui : subGuis) {
            subGui.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        for(ISubGui subGui : subGuis) {
            subGui.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        }
    }
}
