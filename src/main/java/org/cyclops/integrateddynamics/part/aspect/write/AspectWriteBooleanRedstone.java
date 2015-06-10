package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;

/**
 * Base class for boolean redstone aspects.
 * @author rubensworks
 */
public class AspectWriteBooleanRedstone extends AspectWriteBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "redstone";
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(P partType, PartTarget target, S state) {
        // TODO
    }
}
