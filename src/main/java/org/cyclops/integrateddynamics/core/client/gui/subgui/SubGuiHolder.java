package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
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
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        for(ISubGui subGui : getSubGuis()) {
            subGui.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
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
