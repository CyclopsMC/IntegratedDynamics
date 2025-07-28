package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeDropdownList;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ValueTypeOperatorLPElementClient;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeOperatorValueChangedPacket;

import java.util.List;
import java.util.Set;

/**
 * Element for the operator value type.
 * @author rubensworks
 */
public class ValueTypeOperatorLPElement extends ValueTypeLPElementBase<ValueTypeOperatorLPElementClient> implements IDropdownEntryListener {

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
    public ValueTypeOperatorLPElementClient constructClient() {
        return new ValueTypeOperatorLPElementClient(this);
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
    public Component validate() {
        if (selectedOperator == null) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, getInnerGuiElement().getInputString());
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
    public void setValue(IValue value) {
        this.selectedOperator = ((ValueTypeOperator.ValueOperator) value).getRawValue();
    }

    @Override
    public void activate() {

    }

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry dropdownEntry) {
        OperatorDropdownEntry operatorDropdownEntry = (OperatorDropdownEntry) dropdownEntry;
        selectedOperator = operatorDropdownEntry == null ? null : operatorDropdownEntry.getValue();
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread() && selectedOperator != null) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeOperatorValueChangedPacket(ValueDeseralizationContext.ofClient(),
                            ValueTypeOperator.ValueOperator.of(selectedOperator)));
        }
    }

    public IOperator getSelectedOperator() {
        return selectedOperator;
    }

    public void setSelectedOperator(IOperator selectedOperator) {
        this.selectedOperator = selectedOperator;
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
        public MutableComponent getDisplayString() {
            return Component.literal(getMatchString());
        }

        @Override
        public List<MutableComponent> getTooltip() {
            return ValueTypeOperator.getSignatureLines(operator, true);
        }

        @Override
        public IOperator getValue() {
            return operator;
        }
    }

}
