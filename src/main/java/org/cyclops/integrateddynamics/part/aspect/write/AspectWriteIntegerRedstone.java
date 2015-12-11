package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Write the redstone level.
 * @author rubensworks
 */
public class AspectWriteIntegerRedstone extends AspectWriteIntegerBase {

    private static final IWriteRedstoneComponent WRITE_REDSTONE_COMPONENT = new WriteRedstoneComponent();

    @Override
    protected String getUnlocalizedIntegerType() {
        return "redstone";
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeInteger.ValueInteger> variable) {
        try {
            ValueTypeInteger.ValueInteger value = variable.getValue();
            WRITE_REDSTONE_COMPONENT.setRedstoneLevel(target, value.getRawValue());
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
