package org.cyclops.integrateddynamics.core.part.read;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.part.PartStateBase;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A default implementation of the {@link IPartStateReader}.
 * @author rubensworks
 */
public class PartStateReaderBase<P extends IPartTypeReader>
        extends PartStateBase<P> implements IPartStateReader<P> {

    private final Map<IAspect, IAspectVariable> aspectVariables = new IdentityHashMap<>();

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
    public void resetVariables() {
        this.aspectVariables.clear();
    }

    @Override
    public void setAspectProperties(IAspect aspect, IAspectProperties properties) {
        super.setAspectProperties(aspect, properties);
        this.aspectVariables.remove(aspect);
    }

}
