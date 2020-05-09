package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * GUI element for value type that can be read from and written to strings.
 * @author rubensworks
 */
@Data
public class GuiElementValueTypeString<G extends Gui, C extends Container> implements IGuiInputElement<RenderPattern, G, C>, IDropdownEntryListener {

    private final IValueType valueType;
    private Predicate<IValue> validator;
    private final IConfigRenderPattern renderPattern;
    private String defaultInputString;
    private String inputString;
    private Set<IDropdownEntry<?>> dropdownPossibilities = Collections.emptySet();
    private IDropdownEntryListener dropdownEntryListener = null;

    public GuiElementValueTypeString(IValueType valueType, IConfigRenderPattern renderPattern) {
        this.valueType = valueType;
        this.validator = Predicates.alwaysTrue();
        this.renderPattern = renderPattern;
        defaultInputString = getValueType().toCompactString(getValueType().getDefault());
    }

    public void setInputString(String inputString, GuiElementValueTypeStringRenderPattern subGui) {
        this.inputString = inputString;
        if(subGui != null) {
            subGui.getSearchField().setText(inputString);
        }
    }

    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(getValueType().getTranslationKey());
    }

    @Override
    public void loadTooltip(List<String> lines) {
        getValueType().loadTooltip(lines, true, null);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
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
        L10NHelpers.UnlocalizedString error = getValueType().canDeserialize(inputString);
        if (error == null && !this.validator.test(ValueHelpers.deserializeRaw(getValueType(), inputString))) {
            error = new L10NHelpers.UnlocalizedString(L10NValues.VALUE_ERROR);
        }
        return error;
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
    @SideOnly(Side.CLIENT)
    public GuiElementValueTypeStringRenderPattern<GuiElementValueTypeStringRenderPattern, G, C> createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  G gui, C container) {
        return new GuiElementValueTypeStringRenderPattern<>(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @SideOnly(Side.CLIENT)
    public abstract static class SubGuiValueTypeInfo<S extends ISubGuiBox, G extends GuiContainerExtended, C extends Container> extends SubGuiBox.Base {

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
        protected abstract L10NHelpers.UnlocalizedString getLastError();
        protected abstract ResourceLocation getTexture();

        protected int getSignalX() {
            return getWidth() - 22;
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
                            TextFormatting.RED.toString()));
                    gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

    }

}
