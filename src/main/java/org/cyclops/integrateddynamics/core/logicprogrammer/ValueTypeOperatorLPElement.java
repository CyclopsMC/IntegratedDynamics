package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Setter;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeDropdownList;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeOperatorValueChangedPacket;

import java.util.List;
import java.util.Set;

/**
 * Element for the operator value type.
 * @author rubensworks
 */
public class ValueTypeOperatorLPElement extends ValueTypeLPElementBase implements IDropdownEntryListener {

    @Setter
    private IOperator selectedOperator = null;
    private GuiElementValueTypeDropdownList<IOperator, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> innerGuiElement;

    public ValueTypeOperatorLPElement() {
        super(ValueTypes.OPERATOR);
        Set<IDropdownEntry<IOperator>> operatorEntries = Sets.newLinkedHashSet();
        for (IOperator operator : Operators.REGISTRY.getOperators()) {
            operatorEntries.add(new OperatorDropdownEntry(operator));
        }
        this.innerGuiElement = new GuiElementValueTypeDropdownList<>(getValueType(), getRenderPattern());
        getInnerGuiElement().setDropdownPossibilities(operatorEntries);
        getInnerGuiElement().setDropdownEntryListener(this);
    }

    @Override
    public GuiElementValueTypeDropdownList<IOperator, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> getInnerGuiElement() {
        return innerGuiElement;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public ITextComponent validate() {
        if (selectedOperator == null) {
            try {
                ValueHelpers.parseString(getValueType(), getInnerGuiElement().getInputString());
            } catch (EvaluationException e) {
                return e.getErrorMessage();
            }
        }
        return null;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE_CANVAS;
    }

    @Override
    public boolean canWriteElementPre() {
        return selectedOperator != null;
    }

    @Override
    public IValue getValue() {
        return ValueTypeOperator.ValueOperator.of(selectedOperator);
    }

    @Override
    public void activate() {

    }

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry dropdownEntry) {
        OperatorDropdownEntry operatorDropdownEntry = (OperatorDropdownEntry) dropdownEntry;
        selectedOperator = operatorDropdownEntry == null ? null : operatorDropdownEntry.getValue();
        if (MinecraftHelpers.isClientSideThread()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeOperatorValueChangedPacket(
                            ValueTypeOperator.ValueOperator.of(selectedOperator)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight, ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new RenderPatternOperator(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    public static class RenderPatternOperator<S extends ISubGuiBox, G extends AbstractGui, C extends Container> extends ValueTypeOperatorLPElementRenderPattern {

        private final ValueTypeOperatorLPElement element;

        public RenderPatternOperator(ValueTypeOperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight, ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            this.element = element;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            IOperator operator = element.selectedOperator;
            if (operator != null) {
                int offsetY = 0;
                for (ITextComponent line : ValueTypeOperator.getSignatureLines(operator, true)) {
                    // MCP: drawString
                    fontRenderer.func_243246_a(matrixStack, line, getX() + guiLeft + 10, getY() + guiTop + 25 + offsetY, Helpers.RGBToInt(10, 10, 10));
                    offsetY += fontRenderer.FONT_HEIGHT;
                }
            }
        }
    }

    public static class OperatorDropdownEntry implements IDropdownEntry<IOperator> {

        private final IOperator operator;

        public OperatorDropdownEntry(IOperator operator) {
            this.operator = operator;
        }

        @Override
        public String getMatchString() {
            return operator.getLocalizedNameFull().getString();
        }

        @Override
        public String getDisplayString() {
            return getMatchString();
        }

        @Override
        public List<IFormattableTextComponent> getTooltip() {
            return ValueTypeOperator.getSignatureLines(operator, true);
        }

        @Override
        public IOperator getValue() {
            return operator;
        }
    }

}
