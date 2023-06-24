package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGui;

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
    public void init(int guiLeft, int guiTop) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.init(guiLeft, guiTop);
        }
    }

    @Override
    public void tick() {
        for(ISubGui subGui : getSubGuis()) {
            subGui.tick();
        }
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        for(ISubGui subGui : getSubGuis()) {
            if(subGui.charTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        for(ISubGui subGui : getSubGuis()) {
            if(subGui.keyPressed(typedChar, keyCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for(ISubGui subGui : getSubGuis()) {
            if (subGui.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }
}
