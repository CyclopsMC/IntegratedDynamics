package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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

    public boolean isEmpty() {
        return subGuis.isEmpty();
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
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        }
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        for(ISubGui subGui : getSubGuis()) {
            if(subGui.charTyped(evt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        for(ISubGui subGui : getSubGuis()) {
            if(subGui.keyPressed(evt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        for(ISubGui subGui : getSubGuis()) {
            if (subGui.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        return false;
    }
}
