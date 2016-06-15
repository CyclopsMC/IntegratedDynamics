package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.client.gui.GuiTextFieldDropdown;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.logicprogrammer.SubGuiConfigRenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeValueChangedPacket;

import java.io.IOException;
import java.util.Set;

/**
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class ValueTypeSubGuiRenderPattern<S extends ISubGuiBox, G extends Gui, C extends Container> extends SubGuiConfigRenderPattern<ValueTypeGuiElement<G, C>, G, C> implements IDropdownEntryListener {

    @Getter
    protected final ValueTypeGuiElement<G, C> element;
    @Getter
    private GuiTextFieldDropdown searchField = null;

    public ValueTypeSubGuiRenderPattern(ValueTypeGuiElement<G, C> element, int baseX, int baseY, int maxWidth, int maxHeight,
                                        G gui, C container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.element = element;
    }

    @Override
    public void initGui(int guiLeft, int guiTop) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int searchWidth = getElement().getRenderPattern().getWidth() - 28;
        int searchX = getX() + 14;
        int searchY = getY() + 6;
        this.searchField = new GuiTextFieldDropdown(0, fontRenderer, guiLeft + searchX, guiTop + searchY, searchWidth,
                fontRenderer.FONT_HEIGHT + 3, true, getDropdownPossibilities());
        this.searchField.setDropdownEntryListener(this);
        this.searchField.setMaxStringLength(64);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(true);
        String value = element.getInputString();
        if (value == null) {
            value = element.getDefaultInputString();
        }
        this.searchField.setText(value);
        element.setInputString(searchField.getText());
        this.searchField.width = searchWidth;
        this.searchField.xPosition = guiLeft + (searchX + searchWidth) - this.searchField.width;
    }

    protected Set<IDropdownEntry<?>> getDropdownPossibilities() {
        return element.getDropdownPossibilities();
    }

    @Override
    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        // Textbox
        searchField.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    @Override
    public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
        if (!checkHotbarKeys) {
            if (searchField.textboxKeyTyped(typedChar, keyCode)) {
                element.setInputString(searchField.getText());
                if (container instanceof IDirtyMarkListener) {
                    ((IDirtyMarkListener) container).onDirty();
                }
                IntegratedDynamics._instance.getPacketHandler().sendToServer(
                        new LogicProgrammerValueTypeValueChangedPacket(element.getInputString()));
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

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry<?> dropdownEntry) {
        element.onSetDropdownPossiblity(dropdownEntry);
    }
}
