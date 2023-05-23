package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.client.gui.WidgetTextFieldDropdown;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeStringValueChangedPacket;

import java.util.Set;

/**
 * A render pattern for value types that can be read from and written to strings.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class GuiElementValueTypeDropdownListRenderPattern<T, S extends ISubGuiBox, G extends GuiComponent, C extends AbstractContainerMenu>
        extends RenderPattern<GuiElementValueTypeDropdownList<T, G, C>, G, C> implements IDropdownEntryListener<T> {

    @Getter
    protected final GuiElementValueTypeDropdownList<T, G, C> element;
    @Getter
    private WidgetTextFieldDropdown<T> searchField = null;

    public GuiElementValueTypeDropdownListRenderPattern(GuiElementValueTypeDropdownList<T, G, C> element,
                                                        int baseX, int baseY, int maxWidth, int maxHeight,
                                                        G gui, C container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.element = element;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        Font fontRenderer = Minecraft.getInstance().font;
        int searchWidth = getElement().getRenderPattern().getWidth() - 28;
        int searchX = getX() + 14;
        int searchY = getY() + 6;
        this.searchField = new WidgetTextFieldDropdown<>(fontRenderer, guiLeft + searchX, guiTop + searchY, searchWidth,
                fontRenderer.lineHeight + 3, Component.translatable("gui.cyclopscore.search"), true, getDropdownPossibilities());
        this.searchField.setDropdownEntryListener(this);
        this.searchField.setMaxLength(64);
        this.searchField.setBordered(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(true);
        String value = element.getInputString();
        if (value == null) {
            value = "";
        }
        this.searchField.setValue(value);
        element.setInputString(searchField.getValue());
        this.searchField.setWidth(searchWidth);
        this.searchField.setX(guiLeft + (searchX + searchWidth) - this.searchField.getWidth());
    }

    protected Set<IDropdownEntry<T>> getDropdownPossibilities() {
        return element.getDropdownPossibilities();
    }

    @Override
    public void renderBg(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        // Textbox
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (searchField.isFocused()) {
            if (searchField.charTyped(typedChar, keyCode)) {
                onTyped();
                return true;
            }
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (searchField.isFocused()) {
            searchField.keyPressed(typedChar, keyCode, modifiers);
            onTyped();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    private void onTyped() {
        element.setInputString(searchField.getValue());
        if (container instanceof IDirtyMarkListener) {
            ((IDirtyMarkListener) container).onDirty();
        }
        IntegratedDynamics._instance.getPacketHandler().sendToServer(
                new LogicProgrammerValueTypeStringValueChangedPacket(element.getInputString()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return searchField.mouseClicked(mouseX, mouseY, mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry dropdownEntry) {
        element.onSetDropdownPossiblity(dropdownEntry);
        if (container instanceof IDirtyMarkListener) {
            ((IDirtyMarkListener) container).onDirty();
        }
    }
}
