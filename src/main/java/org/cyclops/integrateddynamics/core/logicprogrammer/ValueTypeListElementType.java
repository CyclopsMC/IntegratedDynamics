package org.cyclops.integrateddynamics.core.logicprogrammer;

/**
 * Logic programmer element types for the list value type.
 * @author rubensworks
 */
public class ValueTypeListElementType extends SingleElementType<ValueTypeListElement> {
    public ValueTypeListElementType() {
        super(new ILogicProgrammerElementConstructor<ValueTypeListElement>() {
            @Override
            public ValueTypeListElement construct() {
                return new ValueTypeListElement();
            }
        }, "list");
    }
}
