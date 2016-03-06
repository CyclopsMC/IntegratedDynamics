package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteIntegerBase extends AspectWriteBase<ValueTypeInteger.ValueInteger, ValueTypeInteger> {

    public AspectWriteIntegerBase() {

    }

    public AspectWriteIntegerBase(String unlocalizedTypeSuffix, IAspectProperties defaultProperties) {
        super(unlocalizedTypeSuffix, defaultProperties);
    }

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".integer." + getUnlocalizedIntegerType();
    }

    protected abstract String getUnlocalizedIntegerType();

    @Override
    public ValueTypeInteger getValueType() {
        return ValueTypes.INTEGER;
    }

}
