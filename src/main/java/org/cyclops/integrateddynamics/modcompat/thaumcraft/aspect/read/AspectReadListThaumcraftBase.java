package org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadListBase;

/**
 * Base class for thaumcraft list aspect.
 * @author rubensworks
 */
public abstract class AspectReadListThaumcraftBase extends AspectReadListBase {

    @Override
    protected String getUnlocalizedListType() {
        return "thaumcraft." + getUnlocalizedListThaumcraftType();
    }

    protected abstract String getUnlocalizedListThaumcraftType();
}
