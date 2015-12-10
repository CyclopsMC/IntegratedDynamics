package org.cyclops.integrateddynamics.modcompat.charset.aspect.write;

import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanBase;

/**
 * Set the target shifter action.
 * @author rubensworks
 */
public class AspectWriteBooleanCharsetPipesShifter extends AspectWriteBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "charsetpipes.shifter";
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeBoolean.ValueBoolean> variable) {
        // TODO
    }
}
