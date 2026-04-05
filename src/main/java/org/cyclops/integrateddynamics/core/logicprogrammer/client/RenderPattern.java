package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPatternCommon;

/**
 * Sub gui for rendering logic programmer elements.
 * @author rubensworks
 */
public class RenderPattern<E extends IGuiInputElement, G extends Screen, C extends AbstractContainerMenu> extends SubGuiBox implements ISubGuiBox {

    protected final E element;
    private final int x, y;
    protected final G gui;
    protected final C container;
    protected Identifier texture = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/logic_programmer.png");

    public E getElement() {
        return element;
    }

    public RenderPattern(E element, int baseX, int baseY, int maxWidth, int maxHeight,
                         G gui, C container) {
        super(SubGuiBox.Box.LIGHT);
        this.element = element;
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        this.x = RenderPatternCommon.calculateX(baseX, maxWidth, configRenderPattern);
        this.y = RenderPatternCommon.calculateY(baseY, maxHeight, configRenderPattern);
        this.gui = gui;
        this.container = container;
    }

    public C getContainer() {
        return container;
    }

    protected void drawSlot(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SubGuiBox.TEXTURE, x, y, 19, 0, 18, 18, 256, 256);
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
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        if (drawRenderPattern()) {
            IConfigRenderPattern configRenderPattern = element.getRenderPattern();

            int baseX = getX() + guiLeft;
            int baseY = getY() + guiTop;

            for (Pair<Integer, Integer> slot : configRenderPattern.getSlotPositions()) {
                drawSlot(guiGraphics, baseX + slot.getLeft(), baseY + slot.getRight());
            }

            if (configRenderPattern.getSymbolPosition() != null) {
                IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, fontRenderer, element.getSymbol(),
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

    public void sendValueToServer() {

    }

}
