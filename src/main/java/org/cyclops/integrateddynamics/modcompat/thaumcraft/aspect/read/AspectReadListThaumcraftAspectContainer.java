package org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueTypeListProxyPositionedAspectContainer;

/**
 * Read a block from the world.
 * @author rubensworks
 */
public class AspectReadListThaumcraftAspectContainer extends AspectReadListThaumcraftBase {

    @Override
    protected String getUnlocalizedListThaumcraftType() {
        return "aspectcontainer";
    }

    @Override
    protected ValueTypeList.ValueList getValue(PartTarget target, IAspectProperties properties) {
        return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedAspectContainer(target.getTarget().getPos()));
    }

}
