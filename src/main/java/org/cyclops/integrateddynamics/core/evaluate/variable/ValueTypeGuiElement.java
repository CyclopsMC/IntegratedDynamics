package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.evaluate.operator.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.logicprogrammer.SubGuiConfigRenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeValueChangedPacket;

import java.io.IOException;
import java.util.List;

/**
 * Element for value type.
 * @author rubensworks
 */
@Data
public class ValueTypeGuiElement<G extends Gui, C extends Container> implements IGuiInputElement<G, C> {

    private final IValueType valueType;
    private final String defaultInputString;
    private String inputString;

    public ValueTypeGuiElement(IValueType valueType) {
        this.valueType = valueType;
        defaultInputString = getValueType().toCompactString(getValueType().getDefault());
    }

    public void setInputString(String inputString, SubGuiRenderPattern subGui) {
        this.inputString = inputString;
        if(subGui != null) {
            subGui.searchField.setText(inputString);
        }
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
    }

    @Override
    public void loadTooltip(List<String> lines) {
        getValueType().loadTooltip(lines, true);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public void activate() {
        this.inputString = new String(defaultInputString);
    }

    @Override
    public void deactivate() {
        this.inputString = null;
    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        return getValueType().canDeserialize(inputString);
    }

    @Override
    public int getColor() {
        return getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public SubGuiRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  G gui, C container) {
        return new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @SideOnly(Side.CLIENT)
    public class SubGuiRenderPattern extends SubGuiConfigRenderPattern<ValueTypeGuiElement, G, C> {

        private GuiTextField searchField = null;

        public SubGuiRenderPattern(ValueTypeGuiElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   G gui, C container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            int searchWidth = 71;
            int searchX = getX() + 14;
            int searchY = getY() + 6;
            this.searchField = new GuiTextField(0, fontRenderer, guiLeft + searchX, guiTop + searchY, searchWidth, fontRenderer.FONT_HEIGHT);
            this.searchField.setMaxStringLength(64);
            this.searchField.setMaxStringLength(15);
            this.searchField.setEnableBackgroundDrawing(false);
            this.searchField.setVisible(true);
            this.searchField.setTextColor(16777215);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setText(getValueType().toCompactString(getValueType().getDefault()));
            inputString = searchField.getText();
            this.searchField.width = searchWidth;
            this.searchField.xPosition = guiLeft + (searchX + searchWidth) - this.searchField.width;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            textureManager.bindTexture(TEXTURE);
            this.drawTexturedModalRect(searchField.xPosition - 1, searchField.yPosition - 1, 21, 0, searchField.width + 1, 12);
            // Textbox
            searchField.drawTextBox();
        }

        @Override
        public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
            if (!checkHotbarKeys) {
                if (searchField.textboxKeyTyped(typedChar, keyCode)) {
                    inputString = searchField.getText();
                    if(container instanceof IDirtyMarkListener) {
                        ((IDirtyMarkListener) container).onDirty();
                    }
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(
                            new LogicProgrammerValueTypeValueChangedPacket(inputString));
                    return true;
                }
            }
            return super.keyTyped(checkHotbarKeys, typedChar, keyCode);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

    }

    public abstract static class SubGuiValueTypeInfo<G extends GuiContainerExtended, C extends Container> extends SubGuiBox.Base {

        private final IGuiInputElement element;
        protected final G gui;
        protected final C container;

        public SubGuiValueTypeInfo(G gui, C container, IGuiInputElement<G, C> element, int x, int y, int width, int height) {
            super(Box.DARK, x, y, width, height);
            this.gui = gui;
            this.container = container;
            this.element = element;
        }

        protected abstract boolean showError();
        protected abstract L10NHelpers.UnlocalizedString getLastError();
        protected abstract ResourceLocation getTexture();

        protected int getSignalX() {
            return getWidth() - 19;
        }

        protected int getSignalY() {
            return (getHeight() - 12) / 2;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            int x = guiLeft + getX();
            int y = guiTop + getY();

            fontRenderer.drawString(element.getLocalizedNameFull(), x + 2, y + 6, Helpers.RGBToInt(240, 240, 240));

            if(showError()) {
                L10NHelpers.UnlocalizedString lastError = getLastError();
                if (lastError != null) {
                    Images.ERROR.draw(this, x + getSignalX(), y + getSignalY() - 1);
                } else {
                    Images.OK.draw(this, x + getSignalX(), y + getSignalY() + 1);
                }
            }
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

            int x = getX();
            int y = getY();

            if(showError()) {
                L10NHelpers.UnlocalizedString lastError = getLastError();
                if (lastError != null && gui.isPointInRegion(x + getSignalX(), y + getSignalY() - 1, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                    List<String> lines = Lists.newLinkedList();
                    lines.addAll(StringHelpers.splitLines(lastError.localize(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            EnumChatFormatting.RED.toString()));
                    gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

    }

}
