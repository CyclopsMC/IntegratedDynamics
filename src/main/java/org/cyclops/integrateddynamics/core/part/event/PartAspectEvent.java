package org.cyclops.integrateddynamics.core.part.event;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;

import javax.annotation.Nullable;

/**
 * An aspect part event that is posted in the Forge event bus.
 * @author rubensworks
 */
public class PartAspectEvent<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect> extends PartEvent<P, S> {

    @Nullable
    private final EntityPlayer entityPlayer;
    private final A aspect;

    public PartAspectEvent(INetwork network, IPartNetwork partNetwork, PartTarget target, P partType, S partState, @Nullable EntityPlayer entityPlayer, A aspect) {
        super(network, partNetwork, target, partType, partState);
        this.entityPlayer = entityPlayer;
        this.aspect = aspect;
    }

    @Nullable
    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public A getAspect() {
        return aspect;
    }
}
