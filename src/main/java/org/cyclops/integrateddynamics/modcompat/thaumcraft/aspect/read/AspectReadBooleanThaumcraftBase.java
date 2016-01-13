package org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read;

import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;

/**
 * Base class for boolean thaumcraft aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanThaumcraftBase extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "thaumcraft." + getUnlocalizedBooleanThaumcraftType();
    }

    protected abstract String getUnlocalizedBooleanThaumcraftType();

}
