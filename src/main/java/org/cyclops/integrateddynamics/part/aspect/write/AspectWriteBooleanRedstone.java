package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;

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
                                                                                       S state, IVariable<ValueTypeBoolean.ValueBoolean> variable) {
        try {
            ValueTypeBoolean.ValueBoolean value = variable.getValue();
            WRITE_REDSTONE_COMPONENT.setRedstoneLevel(target, value.getRawValue() ? 15 : 0);
        } catch (EvaluationException e) {
            state.addError(this, new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
            state.setDeactivated(true);
        }

    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        WRITE_REDSTONE_COMPONENT.deactivate(target);
    }
}
