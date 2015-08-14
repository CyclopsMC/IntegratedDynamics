package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;

/**
 * Base class for boolean world aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanWorldBase extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "world." + getUnlocalizedBooleanWorldType();
    }

    protected abstract String getUnlocalizedBooleanWorldType();

}
