package org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import thaumcraft.api.aspects.IAspectContainer;

/**
 * Aspect that checks if the target has a block.
 * @author rubensworks
 */
public class AspectReadBooleanThaumcraftIsAspectContainer extends AspectReadBooleanThaumcraftBase {

    @Override
    protected String getUnlocalizedBooleanThaumcraftType() {
        return "isaspectcontainer";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        return ValueTypeBoolean.ValueBoolean.of(TileHelpers.getSafeTile(dimPos.getWorld(), dimPos.getBlockPos(), IAspectContainer.class) != null);
    }
}
