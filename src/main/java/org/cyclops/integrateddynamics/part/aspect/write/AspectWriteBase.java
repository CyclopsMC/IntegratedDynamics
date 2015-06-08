package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;

/**
 * Base class for write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectWrite<V, T> {

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>>  void update(P partType, PartTarget target, S state) {
        // TODO
    }

    protected String getUnlocalizedType() {
        return "write";
    }

}
