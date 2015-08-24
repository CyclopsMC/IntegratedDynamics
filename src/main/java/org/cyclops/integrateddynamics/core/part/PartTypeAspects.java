package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Set;

/**
 * An abstract {@link IPartType} that can hold aspects.
 * @author rubensworks
 */
public abstract class PartTypeAspects<P extends IPartType<P, S>, S extends IPartState<P>> extends PartTypeConfigurable<P, S> {

    public PartTypeAspects(String name) {
        super(name);
    }

    /**
     * @return All possible aspects that can be used in this part type.
     */
    public Set<IAspect> getAspects() {
        return Aspects.REGISTRY.getAspects(this);
    }

    @Override
    public boolean isUpdate(S state) {
        return !getAspects().isEmpty();
    }

    @Override
    public void update(Network network, PartTarget target, S state) {
        super.update(network, target, state);
        for(IAspect aspect : getAspects()) {
            aspect.update(network, this, target, state);
        }
    }

}
