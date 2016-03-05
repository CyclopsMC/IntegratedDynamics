package org.cyclops.integrateddynamics.modcompat.charset.aspect.write;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.modcompat.charset.CharsetPipesModCompat;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteItemStackBase;

import java.util.Collections;

/**
 * Set the target shifter filter.
 * @author rubensworks
 */
public class AspectWriteItemStackCharsetPipesShifter extends AspectWriteItemStackBase {

    @Override
    protected String getUnlocalizedItemStackType() {
        return "charsetpipe.shifter";
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
                                                                                       S state, IVariable<ValueObjectTypeItemStack.ValueItemStack> variable)
            throws EvaluationException {
        ShifterPart shifter = (ShifterPart) state.getCapability(CharsetPipesModCompat.SHIFTER);
        shifter.setFilter(Collections.singleton(variable.getValue()));
        shifter.setShifting(true);
    }
}
