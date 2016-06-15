package org.cyclops.integrateddynamics.core.logicprogrammer;

/**
 * Logic programmer element types for the operator value type.
 * @author rubensworks
 */
public class ValueTypeOperatorElementType extends SingleElementType<ValueTypeOperatorElement> {
    public ValueTypeOperatorElementType() {
        super(new ILogicProgrammerElementConstructor<ValueTypeOperatorElement>() {
            @Override
            public ValueTypeOperatorElement construct() {
                return new ValueTypeOperatorElement();
            }
        }, "operator");
    }
}
