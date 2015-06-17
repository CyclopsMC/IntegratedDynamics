package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;

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
        ValueTypeInteger.ValueInteger value = variable.getValue();
        WRITE_REDSTONE_COMPONENT.setRedstoneLevel(target, value.getRawValue());
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        WRITE_REDSTONE_COMPONENT.deactivate(target);
    }

}
