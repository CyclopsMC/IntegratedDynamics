package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public abstract class SubGuiValueTypeInfoBase<S extends ISubGuiBox, G extends ContainerScreenExtended<?>, C extends AbstractContainerMenu> extends SubGuiBox.Base {

    private final IGuiInputElement element;
    protected final G gui;
    protected final C container;

    public SubGuiValueTypeInfoBase(G gui, C container, IGuiInputElement<S, G, C, ?> element, int x, int y, int width, int height) {
        super(Box.DARK, x, y, width, height);
        this.gui = gui;
        this.container = container;
        this.element = element;
    }

    protected abstract boolean showError();

    protected abstract Component getLastError();

    protected abstract Identifier getTexture();

    protected int getSignalX() {
        return getWidth() - 22;
    }

    protected int getSignalY() {
        return (getHeight() - 12) / 2;
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        int x = guiLeft + getX();
        int y = guiTop + getY();

        if (this.shouldRenderElementName()) {
            guiGraphics.text(fontRenderer, element.getName(), x + 2, y + 6, IModHelpers.get().getBaseHelpers().RGBAToInt(240, 240, 240, 255), true);
        }

        if (showError()) {
            Component lastError = getLastError();
            if (lastError != null) {
                Images.ERROR.draw(guiGraphics, x + getSignalX(), y + getSignalY() - 1);
            } else {
                Images.OK.draw(guiGraphics, x + getSignalX(), y + getSignalY() + 1);
            }
        }
    }

    public boolean shouldRenderElementName() {
        return true;
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

        int x = getX();
        int y = getY();

        if (showError()) {
            Component lastError = getLastError();
            if (lastError != null && gui.isHovering(x + getSignalX(), y + getSignalY() - 1, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                List<Component> lines = StringHelpers.splitLines(lastError.getString(), IModHelpers.get().getL10NHelpers().getMaxTooltipLineLength(),
                                ChatFormatting.RED.toString())
                        .stream()
                        .map(Component::literal)
                        .collect(Collectors.toList());
                gui.drawTooltip(lines, guiGraphics, mouseX, mouseY);
            }
        }
    }

}
