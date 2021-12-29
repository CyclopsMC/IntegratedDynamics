package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;

/**
 * Sub gui for rendering logic programmer elements.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class RenderPattern<E extends IGuiInputElement, G extends GuiComponent, C extends AbstractContainerMenu> extends SubGuiBox implements ISubGuiBox {

    @Getter
    protected final E element;
    private final int x, y;
    protected final G gui;
    protected final C container;

    public RenderPattern(E element, int baseX, int baseY, int maxWidth, int maxHeight,
                         G gui, C container) {
        super(SubGuiBox.Box.LIGHT);
        this.element = element;
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        this.x = baseX + (maxWidth  - configRenderPattern.getWidth()) / 2;
        this.y = baseY + (maxHeight - configRenderPattern.getHeight()) / 2;
        this.gui = gui;
        this.container = container;
    }

    protected void drawSlot(PoseStack matrixStack, int x, int y) {
        this.blit(matrixStack, x, y, 19, 0, 18, 18);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
    }

    @Override
    public void tick() {

    }

    protected boolean drawRenderPattern() {
        return true;
    }

    @Override
    public void renderBg(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        if (drawRenderPattern()) {
            IConfigRenderPattern configRenderPattern = element.getRenderPattern();

            int baseX = getX() + guiLeft;
            int baseY = getY() + guiTop;

            for (Pair<Integer, Integer> slot : configRenderPattern.getSlotPositions()) {
                drawSlot(matrixStack, baseX + slot.getLeft(), baseY + slot.getRight());
            }

            if (configRenderPattern.getSymbolPosition() != null) {
                RenderHelpers.drawScaledCenteredString(matrixStack, fontRenderer, element.getSymbol(),
                        baseX + configRenderPattern.getSymbolPosition().getLeft(),
                        baseY + configRenderPattern.getSymbolPosition().getRight() + 8,
                        0, 1, 0);
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return element.getRenderPattern().getWidth();
    }

    @Override
    public int getHeight() {
        return element.getRenderPattern().getHeight();
    }

}
