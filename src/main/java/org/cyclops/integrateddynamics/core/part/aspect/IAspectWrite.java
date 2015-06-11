package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;

/**
 * An element that can be used inside parts to access a specific aspect of something to write.
 * @author rubensworks
 */
public interface IAspectWrite<V extends IValue, T extends IValueType<V>> extends IAspect<V, T> {

    /**
     * Write the given variable value for the given part.
     * @param partType The part type.
     * @param target The position that is targeted by the given part.
     * @param state The current state of the given part.
     * @param variable The variable to write.
     * @param <P> The part type type.
     * @param <S> The part state.
     */
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<V> variable);

    /**
     * Whent this aspect has become inactive.
     * @param partType The part type.
     * @param target The position that is targeted by the given part.
     * @param state The current state of the given part.
     * @param <P> The part type type.
     * @param <S> The part state.
     */
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state);

}
