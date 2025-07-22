package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ValueTypeListLPElementClient;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeListValueChangedPacket;

import java.util.List;
import java.util.Map;

/**
 * Element for the list value type.
 * @author rubensworks
 */
public class ValueTypeListLPElement extends ValueTypeLPElementBase<ValueTypeListLPElementClient> {

    private IValueType listValueType;
    private Map<Integer, IValueTypeLogicProgrammerElement> subElements;
    private int length = 0;
    private int activeElement = -1;

    private ValueTypeList.ValueList serverValue = null;

    public ValueTypeListLPElement() {
        super(ValueTypes.LIST);
    }

    public void setServerValue(ValueTypeList.ValueList serverValue) {
        this.serverValue = serverValue;
    }

    @Override
    public ValueTypeListLPElementClient constructClient() {
        return new ValueTypeListLPElementClient(this);
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE_CANVAS_WIDE;
    }

    @Override
    public boolean canWriteElementPre() {
        return IModHelpers.get().getMinecraftHelpers().isClientSideThread() ? listValueType != null : serverValue != null;
    }

    protected List<IValue> constructValues() {
        List<IValue> valueList = Lists.newArrayListWithExpectedSize(this.length);
        for (Map.Entry<Integer, IValueTypeLogicProgrammerElement> value : this.subElements.entrySet()) {
            if(value.getValue().validate() == null) {
                valueList.add(value.getKey(), value.getValue().getValue());
            } else {
                valueList.add(value.getKey(), listValueType.getDefault());
            }
        }
        return valueList;
    }

    @Override
    public IValue getValue() {
        return IModHelpers.get().getMinecraftHelpers().isClientSideThread()
                ? ValueTypeList.ValueList.ofList(listValueType, constructValues()) : serverValue;
    }

    @Override
    public void setValue(IValue value) {
        if (!IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            serverValue = (ValueTypeList.ValueList) value;
        }

        IValueTypeListProxy list = ((ValueTypeList.ValueList) value).getRawValue();
        if (!list.isInfinite()) {
            setListValueType(list.getValueType());
            try {
                int length = list.getLength();
                this.length = length;
                for (int i = 0; i < length; i++) {
                    initializeElement(i).setValue(list.get(i));
                }
            } catch (EvaluationException e) {
                // Ignore exceptions
            }
        }
    }

    @Override
    public void setValueInContainer(ContainerLogicProgrammerBase container) {
        if (length > 0) {
            IValueTypeLogicProgrammerElement subElement = setActiveElement(0);
            int x = RenderPatternCommon.calculateX(ContainerLogicProgrammerBase.BASE_X, ContainerLogicProgrammerBase.MAX_WIDTH, subElement.getRenderPattern());
            int y = RenderPatternCommon.calculateY(ContainerLogicProgrammerBase.BASE_Y, ContainerLogicProgrammerBase.MAX_HEIGHT, subElement.getRenderPattern());
            container.setElementInventory(subElement, x, y);
            container.getTemporaryInputSlots().removeDirtyMarkListener(container);
            subElement.setValueInContainer(container);
            container.getTemporaryInputSlots().addDirtyMarkListener(container);
        }
    }

    public IValueType getListValueType() {
        return listValueType;
    }

    public void setListValueType(IValueType listValueType) {
        this.listValueType = listValueType;
        reset();
    }

    public void reset() {
        this.subElements = Maps.newHashMap();
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            getClient().reset();
        }
        setLength(0);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        setActiveElement(length - 1);
    }

    public IValueTypeLogicProgrammerElement initializeElement(int index) {
        IValueTypeLogicProgrammerElement subElement = listValueType.createLogicProgrammerElement();
        subElement.activate();
        subElements.put(index, subElement);
        return subElement;
    }

    public int getActiveElement() {
        return activeElement;
    }

    public IValueTypeLogicProgrammerElement setActiveElement(int index) {
        activeElement = index;
        IValueTypeLogicProgrammerElement subElement;
        if(index >= 0 && !subElements.containsKey(index)) {
            subElement = initializeElement(index);
            subElement.activate();
        } else {
            subElement = subElements.get(index);
        }
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            getClient().setActiveElement(activeElement);
        }
        return subElement;
    }

    public void removeElement(int index) {
        Map<Integer, IValueTypeLogicProgrammerElement> oldSubElements = subElements;
        subElements = Maps.newHashMap();
        for(Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : oldSubElements.entrySet()) {
            int i = entry.getKey();
            if(i < index) {
                subElements.put(i, entry.getValue());
            } else if(i > index) {
                subElements.put(i - 1, entry.getValue());
            }
        }
        if (IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            getClient().removeElement(index);
        }
        setLength(length - 1);
    }

    public Map<Integer, IValueTypeLogicProgrammerElement> getSubElements() {
        return subElements;
    }

    @Override
    public void activate() {
        reset();
    }

    @Override
    public void deactivate() {
        this.activeElement = -1;
    }

    @Override
    public Component validate() {
        if(!IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            return serverValue == null ? Component.literal("") : null;
        }
        if(IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerValueTypeListValueChangedPacket(ValueDeseralizationContext.ofClient(),
                    listValueType == null ? ValueTypes.LIST.getDefault() : ValueTypeList.ValueList.ofList(listValueType, constructValues())));
        }
        if(this.listValueType == null) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUTITEM);
        }
        for(Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : subElements.entrySet()) {
            Component error = entry.getValue().validate();
            if(error != null) {
                return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDLISTELEMENT, entry.getKey(), error);
            }
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return (slotId == 0 && super.isItemValidForSlot(slotId, itemStack)) ||
                (activeElement >= 0 && subElements.containsKey(activeElement)
                        && subElements.get(activeElement).isItemValidForSlot(slotId, itemStack));
    }

}
