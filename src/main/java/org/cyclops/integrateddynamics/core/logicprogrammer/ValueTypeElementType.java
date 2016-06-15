package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.ImmutableList;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Value type element type.
 * @author rubensworks
 */
public class ValueTypeElementType implements ILogicProgrammerElementType<ValueTypeElement> {

    @Override
    public ValueTypeElement getByName(String name) {
        return getByValueType(ValueTypes.REGISTRY.getValueType(name));
    }

    /**
     * Get the element by value type.
     * @param valueType The value type.
     * @return The corresponding element.
     */
    public ValueTypeElement getByValueType(IValueType valueType) {
        return new ValueTypeElement(valueType);
    }

    @Override
    public String getName(ValueTypeElement element) {
        return element.getInnerGuiElement().getValueType().getUnlocalizedName();
    }

    @Override
    public String getName() {
        return "valuetype";
    }

    @Override
    public List<ValueTypeElement> createElements() {
        ImmutableList.Builder<ValueTypeElement> builder = ImmutableList.builder();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            if(valueType.hasDefaultLogicProgrammerElement()) {
                builder.add(new ValueTypeElement(valueType));
            }
        }
        return builder.build();
    }

    /**
     * @return All possible value types in this element type.
     */
    public List<IValueType> getValueTypes() {
        ImmutableList.Builder<IValueType> builder = ImmutableList.builder();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            if(!valueType.isCategory() && !valueType.isObject()) {
                builder.add(valueType);
            }
        }
        return builder.build();
    }

}
