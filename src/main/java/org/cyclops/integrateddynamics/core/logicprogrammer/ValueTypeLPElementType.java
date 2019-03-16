package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.ImmutableList;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Value type element type.
 * @author rubensworks
 */
public class ValueTypeLPElementType implements ILogicProgrammerElementType<IValueTypeLogicProgrammerElement> {

    @Override
    public IValueTypeLogicProgrammerElement getByName(String name) {
        return ValueTypes.REGISTRY.getValueType(name).createLogicProgrammerElement();
    }

    @Override
    public String getName(IValueTypeLogicProgrammerElement element) {
        return element.getValueType().getTranslationKey();
    }

    @Override
    public String getName() {
        return "valuetype";
    }

    @Override
    public List<IValueTypeLogicProgrammerElement> createElements() {
        ImmutableList.Builder<IValueTypeLogicProgrammerElement> builder = ImmutableList.builder();
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            IValueTypeLogicProgrammerElement valueTypeLPElement = valueType.createLogicProgrammerElement();
            if(valueTypeLPElement != null) {
                builder.add(valueTypeLPElement);
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
            if(!valueType.isCategory() && valueType.createLogicProgrammerElement() != null) {
                builder.add(valueType);
            }
        }
        return builder.build();
    }

}
