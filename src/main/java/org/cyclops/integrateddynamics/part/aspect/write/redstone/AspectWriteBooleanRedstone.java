package org.cyclops.integrateddynamics.part.aspect.write.redstone;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanBase;

/**
 * Write the boolean redstone level.
 * @author rubensworks
 */
public class AspectWriteBooleanRedstone extends AspectWriteBooleanBase {

    private static final IWriteRedstoneComponent WRITE_REDSTONE_COMPONENT = new WriteRedstoneComponent();

    @Override
    protected String getUnlocalizedBooleanType() {
        return "redstone";
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeBoolean.ValueBoolean> variable)
            throws EvaluationException {
        WRITE_REDSTONE_COMPONENT.setRedstoneLevel(target, variable.getValue().getRawValue() ? 15 : 0);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        WRITE_REDSTONE_COMPONENT.deactivate(target);
    }
}
