package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Dummy block variable.
 * @author rubensworks
 */
public class DummyVariableBlock extends DummyVariable<ValueObjectTypeBlock.ValueBlock> {

    public DummyVariableBlock(ValueObjectTypeBlock.ValueBlock value) {
        super(ValueTypes.OBJECT_BLOCK, value);
    }

    public DummyVariableBlock() {
        super(ValueTypes.OBJECT_BLOCK);
    }

}
