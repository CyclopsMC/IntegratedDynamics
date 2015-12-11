package org.cyclops.integrateddynamics.modcompat.charset.aspect.read;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;
import pl.asie.charset.api.pipes.IPipe;

/**
 * If the target is a pipe.
 * @author rubensworks
 */
public class AspectReadBooleanCharsetPipesPipeApplicable extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "charsetpipe.applicable";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos pos = target.getTarget().getPos();
        IPipe pipe = TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IPipe.class);
        return ValueTypeBoolean.ValueBoolean.of(pipe != null);
    }
}
