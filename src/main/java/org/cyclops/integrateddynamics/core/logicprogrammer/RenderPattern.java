package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;

/**
 * Sub gui for rendering logic programmer elements.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class RenderPattern<E extends IGuiInputElement, G extends Screen, C extends AbstractContainerMenu> extends SubGuiBox implements ISubGuiBox {

    @Getter
    protected final E element;
    private final int x, y;
    protected final G gui;
    protected final C container;
    protected ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/logic_programmer.png");

    public RenderPattern(E element, int baseX, int baseY, int maxWidth, int maxHeight,
                         G gui, C container) {
        super(SubGuiBox.Box.LIGHT);
        this.element = element;
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        this.x = calculateX(baseX, maxWidth, configRenderPattern);
        this.y = calculateY(baseY, maxHeight, configRenderPattern);
        this.gui = gui;
        this.container = container;
    }

    public static int calculateX(int baseX, int maxWidth, IConfigRenderPattern configRenderPattern) {
        return baseX + (maxWidth  - configRenderPattern.getWidth()) / 2;
    }

    public static int calculateY(int baseY, int maxHeight, IConfigRenderPattern configRenderPattern) {
        return baseY + (maxHeight - configRenderPattern.getHeight()) / 2;
    }

    protected void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(SubGuiBox.TEXTURE, x, y, 19, 0, 18, 18);
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
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        if (drawRenderPattern()) {
            IConfigRenderPattern configRenderPattern = element.getRenderPattern();

            int baseX = getX() + guiLeft;
            int baseY = getY() + guiTop;

            for (Pair<Integer, Integer> slot : configRenderPattern.getSlotPositions()) {
                drawSlot(guiGraphics, baseX + slot.getLeft(), baseY + slot.getRight());
            }

            if (configRenderPattern.getSymbolPosition() != null) {
                RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, element.getSymbol(),
                        baseX + configRenderPattern.getSymbolPosition().getLeft(),
                        baseY + configRenderPattern.getSymbolPosition().getRight() + 8,
                        0, 1, 0, false, Font.DisplayMode.NORMAL);
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
