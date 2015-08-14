package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;

/**
 * Base class for boolean inventory aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanInventoryBase extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "inventory." + getUnlocalizedBooleanWorldType();
    }

    protected abstract String getUnlocalizedBooleanWorldType();

}
