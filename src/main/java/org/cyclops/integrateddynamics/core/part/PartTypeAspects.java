package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Set;

/**
 * An abstract {@link IPartType} that can hold aspects.
 * @author rubensworks
 */
public abstract class PartTypeAspects<P extends IPartType<P, S>, S extends IPartState<P>> extends PartTypeConfigurable<P, S> {

    public PartTypeAspects(String name, RenderPosition renderPosition) {
        super(name, renderPosition);
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
    public void update(IPartNetwork network, PartTarget target, S state) {
        super.update(network, target, state);
        for(IAspect aspect : getAspects()) {
            aspect.update(network, this, target, state);
        }
    }

    @Override
    public int getConsumptionRate(S state) {
        return 1;
    }

}
