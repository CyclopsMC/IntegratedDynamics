package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadLongBase;

/**
 * Base class for integer world aspects.
 * @author rubensworks
 */
public abstract class AspectReadLongWorldBase extends AspectReadLongBase {

    @Override
    protected String getUnlocalizedLongType() {
        return "world." + getUnlocalizedLongWorldType();
    }

    protected abstract String getUnlocalizedLongWorldType();

}
