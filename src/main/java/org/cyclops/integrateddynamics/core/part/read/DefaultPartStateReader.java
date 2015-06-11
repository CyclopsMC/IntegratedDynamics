package org.cyclops.integrateddynamics.core.part.read;

import com.google.common.collect.Maps;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;

import java.util.Map;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.read.IPartStateReader} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class DefaultPartStateReader<P extends IPartTypeReader>
        extends DefaultPartState<P> implements IPartStateReader<P> {

    private final Map<IAspect, IAspectVariable> aspectVariables = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(IAspectRead<V, T> aspect) {
        return aspectVariables.get(aspect);
    }

    @Override
    public void setVariable(IAspect aspect, IAspectVariable variable) {
        aspectVariables.put(aspect, variable);
    }

    @Override
    public Class<? extends IPartState> getPartStateClass() {
        return IPartStateReader.class;
    }
}
