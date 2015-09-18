package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Value type element type.
 * @author rubensworks
 */
public class ValueTypeElementType implements ILogicProgrammerElementType<ValueTypeElement> {

    @Override
    public ValueTypeElement getByName(String name) {
        return new ValueTypeElement(ValueTypes.REGISTRY.getValueType(name));
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
        List<ValueTypeElement> elements = Lists.newLinkedList();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            if(!valueType.isCategory()) {
                elements.add(new ValueTypeElement(valueType));
            }
        }
        return elements;
    }
}
