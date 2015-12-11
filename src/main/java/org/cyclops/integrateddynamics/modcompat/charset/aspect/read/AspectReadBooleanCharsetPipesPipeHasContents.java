package org.cyclops.integrateddynamics.modcompat.charset.aspect.read;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;
import pl.asie.charset.api.pipes.IPipe;

/**
 * If the pipe has an itemstack.
 * @author rubensworks
 */
public class AspectReadBooleanCharsetPipesPipeHasContents extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "charsetpipe.hascontents";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos pos = target.getTarget().getPos();
        IPipe pipe = TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IPipe.class);
        boolean hasContents = false;
        if(pipe != null) {
            hasContents = pipe.getTravellingStack(null) != null;
        }
        return ValueTypeBoolean.ValueBoolean.of(hasContents);
    }
}
