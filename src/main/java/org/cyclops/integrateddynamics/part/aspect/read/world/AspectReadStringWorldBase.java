package org.cyclops.integrateddynamics.part.aspect.read.world;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadStringBase;

/**
 * Base class for string world aspects.
 * @author rubensworks
 */
public abstract class AspectReadStringWorldBase extends AspectReadStringBase {

    @Override
    protected String getUnlocalizedStringType() {
        return "world." + getUnlocalizedStringWorldType();
    }

    protected abstract String getUnlocalizedStringWorldType();

}
