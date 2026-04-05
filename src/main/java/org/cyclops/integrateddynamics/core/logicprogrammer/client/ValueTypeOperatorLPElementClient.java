package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeDropdownListRenderPattern;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeOperatorLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class ValueTypeOperatorLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeOperatorLPElement> {
    public ValueTypeOperatorLPElementClient(ValueTypeOperatorLPElement element) {
        super(element);
    }

    @Override
    public void setValueInGui(ISubGuiBox subGui) {
        if (this.getElement().getSelectedOperator() != null) {
            ((GuiElementValueTypeDropdownListRenderPattern) subGui).getSearchField().setValue(this.getElement().getSelectedOperator().getLocalizedNameFull().getString());
            ((GuiElementValueTypeDropdownListRenderPattern) subGui).onTyped();
            ((GuiElementValueTypeDropdownListRenderPattern) subGui).getSearchField().refreshDropdownList();
        }
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight, ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeOperatorLPElementClient.RenderPatternOperator(this.getElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    public static class RenderPatternOperator<S extends ISubGuiBox, G extends AbstractContainerScreen, C extends AbstractContainerMenu> extends ValueTypeOperatorLPElementRenderPattern {

        private final ValueTypeOperatorLPElement element;

        public RenderPatternOperator(ValueTypeOperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight, ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            this.element = element;
        }

        @Override
        public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            IOperator operator = element.getSelectedOperator();
            if (operator != null) {
                int offsetY = 0;
                for (Component line : ValueTypeOperator.getSignatureLines(operator, true)) {
                    guiGraphics.text(fontRenderer, line, getX() + guiLeft + 10, getY() + guiTop + 25 + offsetY, IModHelpers.get().getBaseHelpers().RGBAToInt(10, 10, 10, 255), false);
                    offsetY += fontRenderer.lineHeight;
                }
            }
        }
    }
}
