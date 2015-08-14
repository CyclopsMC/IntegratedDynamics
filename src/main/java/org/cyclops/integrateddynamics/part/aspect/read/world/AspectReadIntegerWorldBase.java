package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Base class for integer world aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerWorldBase extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "world." + getUnlocalizedIntegerWorldType();
    }

    protected abstract String getUnlocalizedIntegerWorldType();

}
