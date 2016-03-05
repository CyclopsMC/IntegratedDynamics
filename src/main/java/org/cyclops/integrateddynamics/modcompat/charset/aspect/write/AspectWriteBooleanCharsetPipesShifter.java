package org.cyclops.integrateddynamics.modcompat.charset.aspect.write;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.modcompat.charset.CharsetPipesModCompat;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanBase;

/**
 * Set the target shifter action.
 * @author rubensworks
 */
public class AspectWriteBooleanCharsetPipesShifter extends AspectWriteBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "charsetpipe.shifter";
    }

    public static void notifyNeighbours(PartTarget target) {
        DimPos dimPos = target.getCenter().getPos();
        dimPos.getWorld().notifyNeighborsOfStateChange(dimPos.getBlockPos(), dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock());
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
        super.onActivate(partType, target, state);
        state.addVolatileCapability(CharsetPipesModCompat.SHIFTER, new ShifterPart(target.getCenter().getSide()));
        AspectWriteBooleanCharsetPipesShifter.notifyNeighbours(target);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType, PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        state.removeVolatileCapability(CharsetPipesModCompat.SHIFTER);
        AspectWriteBooleanCharsetPipesShifter.notifyNeighbours(target);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeBoolean.ValueBoolean> variable)
            throws EvaluationException {
        ShifterPart shifter = (ShifterPart) state.getCapability(CharsetPipesModCompat.SHIFTER);
        shifter.setShifting(variable.getValue().getRawValue());
    }
}
