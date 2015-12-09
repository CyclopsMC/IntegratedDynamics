package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for block object read aspects.
 * @author rubensworks
 */
public abstract class AspectReadObjectBlockBase extends AspectReadObjectBase<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock> {

    @Override
    public String getUnlocalizedObjectType() {
        return "block." + getUnlocalizedBlockType();
    }

    protected abstract String getUnlocalizedBlockType();

    @Override
    public ValueObjectTypeBlock getValueType() {
        return ValueTypes.OBJECT_BLOCK;
    }

}
