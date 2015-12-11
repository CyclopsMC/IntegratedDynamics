package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.List;

/**
 * Operator element type.
 * @author rubensworks
 */
public class OperatorElementType implements ILogicProgrammerElementType<OperatorElement> {

    @Override
    public OperatorElement getByName(String name) {
        return new OperatorElement(Operators.REGISTRY.getOperator(name));
    }

    @Override
    public String getName(OperatorElement element) {
        return element.getOperator().getUniqueName();
    }

    @Override
    public String getName() {
        return "operator";
    }

    @Override
    public List<OperatorElement> createElements() {
        List<OperatorElement> elements = Lists.newLinkedList();
        for(IOperator operator : Operators.REGISTRY.getOperators()) {
            elements.add(new OperatorElement(operator));
        }
        return elements;
    }
}
