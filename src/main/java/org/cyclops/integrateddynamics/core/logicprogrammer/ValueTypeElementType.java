package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Value type element type.
 * @author rubensworks
 */
public class ValueTypeElementType implements ILogicProgrammerElementType<ValueTypeLogicProgrammerElement> {

    @Override
    public ValueTypeLogicProgrammerElement getByName(String name) {
        return new ValueTypeLogicProgrammerElement(ValueTypes.REGISTRY.getValueType(name));
    }

    @Override
    public String getName(ValueTypeLogicProgrammerElement element) {
        return element.getValueType().getUnlocalizedName();
    }

    @Override
    public String getName() {
        return "valuetype";
    }

    @Override
    public List<ValueTypeLogicProgrammerElement> createElements() {
        List<ValueTypeLogicProgrammerElement> elements = Lists.newLinkedList();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            if(!valueType.isCategory()) {
                elements.add(new ValueTypeLogicProgrammerElement(valueType));
            }
        }
        return elements;
    }
}
