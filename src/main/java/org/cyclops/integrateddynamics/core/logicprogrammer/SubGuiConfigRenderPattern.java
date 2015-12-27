package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;

import java.util.List;

/**
 * Sub gui for logic programmer elements.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class SubGuiConfigRenderPattern<E extends IGuiInputElement, G extends Gui, C extends Container> extends SubGuiBox implements ISubGuiBox {

    protected final E element;
    private final int x, y;
    protected final G gui;
    protected final C container;

    public SubGuiConfigRenderPattern(E element, int baseX, int baseY, int maxWidth, int maxHeight,
                                     G gui, C container) {
        super(SubGuiBox.Box.LIGHT);
        this.element = element;
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        this.x = baseX + (maxWidth  - configRenderPattern.getWidth()) / 2;
        this.y = baseY + (maxHeight - configRenderPattern.getHeight()) / 2;
        this.gui = gui;
        this.container = container;
    }

    protected void drawSlot(int x, int y) {
        this.drawTexturedModalRect(x, y, 3, 0, 18, 18);
    }

    @Override
    public void initGui(int guiLeft, int guiTop) {
        super.initGui(guiLeft, guiTop);
    }

    @Override
    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();

        int baseX = getX() + guiLeft;
        int baseY = getY() + guiTop;

        for(Pair<Integer, Integer> slot : configRenderPattern.getSlotPositions()) {
            drawSlot(baseX + slot.getLeft(), baseY + slot.getRight());
        }

        if(configRenderPattern.getSymbolPosition() != null) {
            int width = fontRenderer.getStringWidth(element.getSymbol());
            RenderHelpers.drawScaledCenteredString(fontRenderer, element.getSymbol(),
                    baseX + configRenderPattern.getSymbolPosition().getLeft(),
                    baseY + configRenderPattern.getSymbolPosition().getRight() + 8,
                    width, 1, 0);
        }
        GlStateManager.color(1, 1, 1);
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

    protected List<String> getValueTypeTooltip(IValueType<?> valueType) {
        List<String> lines = Lists.newLinkedList();
        lines.add(valueType.getDisplayColorFormat() + L10NHelpers.localize(valueType.getUnlocalizedName()));
        return lines;
    }

}
