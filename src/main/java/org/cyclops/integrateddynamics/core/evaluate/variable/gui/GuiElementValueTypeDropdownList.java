package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * GUI element for value type that are displayed using a dropdown list.
 * @author rubensworks
 */
@Data
public class GuiElementValueTypeDropdownList<T, G extends AbstractGui, C extends Container> implements IGuiInputElementValueType<RenderPattern, G, C>, IDropdownEntryListener<T> {

    private final IValueType valueType;
    private Predicate<IValue> validator;
    private final IConfigRenderPattern renderPattern;
    private String inputString;
    private Set<IDropdownEntry<T>> dropdownPossibilities = Collections.emptySet();
    private IDropdownEntryListener<T> dropdownEntryListener = null;

    public GuiElementValueTypeDropdownList(IValueType valueType, IConfigRenderPattern renderPattern) {
        this.valueType = valueType;
        this.validator = Predicates.alwaysTrue();
        this.renderPattern = renderPattern;
    }

    @Override
    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
    }

    @Override
    public void setValue(IValue value, RenderPattern propertyConfigPattern) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    @Override
    public IValue getValue() {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    @Override
    public ITextComponent getName() {
        return new TranslationTextComponent(getValueType().getTranslationKey());
    }

    @Override
    public void loadTooltip(List<ITextComponent> lines) {
        getValueType().loadTooltip(lines, true, null);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
    }

    @Override
    public void activate() {
        this.inputString = "";
    }

    @Override
    public void deactivate() {
        this.inputString = null;
    }

    @Override
    public ITextComponent validate() {
        try {
            IValue value = getValueType().parseString(inputString);
            if (!this.validator.test(value)) {
                return new TranslationTextComponent(L10NValues.VALUE_ERROR);
            }
        } catch (EvaluationException e) {
            return e.getErrorMessage();
        }
        return null;
    }

    @Override
    public int getColor() {
        return getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(getValueType().getTranslationKey());
    }

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry dropdownEntry) {
        if (dropdownEntryListener != null) {
            dropdownEntryListener.onSetDropdownPossiblity(dropdownEntry);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiElementValueTypeDropdownListRenderPattern<T, ?, G, C> createSubGui(int baseX, int baseY,
                                                                                 int maxWidth, int maxHeight, G gui, C container) {
        return new GuiElementValueTypeDropdownListRenderPattern<>(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class SubGuiValueTypeInfo<S extends ISubGuiBox, G extends ContainerScreenExtended<?>, C extends Container> extends SubGuiBox.Base {

        private final IGuiInputElement element;
        protected final G gui;
        protected final C container;

        public SubGuiValueTypeInfo(G gui, C container, IGuiInputElement<S, G, C> element, int x, int y, int width, int height) {
            super(Box.DARK, x, y, width, height);
            this.gui = gui;
            this.container = container;
            this.element = element;
        }

        protected abstract boolean showError();
        protected abstract ITextComponent getLastError();
        protected abstract ResourceLocation getTexture();

        protected int getSignalX() {
            return getWidth() - 22;
        }

        protected int getSignalY() {
            return (getHeight() - 12) / 2;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            int x = guiLeft + getX();
            int y = guiTop + getY();

            // MCP: drawString
            fontRenderer.drawShadow(matrixStack, element.getName(), x + 2, y + 6, Helpers.RGBToInt(240, 240, 240));

            if(showError()) {
                ITextComponent lastError = getLastError();
                if (lastError != null) {
                    Images.ERROR.draw(this, matrixStack, x + getSignalX(), y + getSignalY() - 1);
                } else {
                    Images.OK.draw(this, matrixStack, x + getSignalX(), y + getSignalY() + 1);
                }
            }
        }

        @Override
        public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

            int x = getX();
            int y = getY();

            if(showError()) {
                ITextComponent lastError = getLastError();
                if (lastError != null && gui.isPointInRegion(x + getSignalX(), y + getSignalY() - 1, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                    List<ITextComponent> lines = StringHelpers.splitLines(lastError.getString(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            TextFormatting.RED.toString())
                            .stream()
                            .map(StringTextComponent::new)
                            .collect(Collectors.toList());
                    gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

    }

}
