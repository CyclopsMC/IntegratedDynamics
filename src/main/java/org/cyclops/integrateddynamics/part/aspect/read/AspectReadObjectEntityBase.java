package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for entity read aspects.
 * @author rubensworks
 */
public abstract class AspectReadObjectEntityBase extends AspectReadObjectBase<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity> {

    @Override
    public String getUnlocalizedObjectType() {
        return "entity." + getUnlocalizedItemStackType();
    }

    protected abstract String getUnlocalizedItemStackType();

    @Override
    public ValueObjectTypeEntity getValueType() {
        return ValueTypes.OBJECT_ENTITY;
    }

}
