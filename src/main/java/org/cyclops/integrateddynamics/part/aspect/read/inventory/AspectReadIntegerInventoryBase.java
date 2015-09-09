package org.cyclops.integrateddynamics.part.aspect.read.inventory;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Base class for integer inventory aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerInventoryBase extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "inventory." + getUnlocalizedIntegerWorldType();
    }

    protected abstract String getUnlocalizedIntegerWorldType();

}
