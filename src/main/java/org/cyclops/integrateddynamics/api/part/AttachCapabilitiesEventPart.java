package org.cyclops.integrateddynamics.api.part;

import net.minecraftforge.event.AttachCapabilitiesEvent;

/**
 * Event for when an {@link IPartState} is being constructed.
 * @author rubensworks
 */
public class AttachCapabilitiesEventPart extends AttachCapabilitiesEvent<IPartState> {

    private final IPartType partType;

    public AttachCapabilitiesEventPart(IPartType partType, IPartState partState) {
        super(IPartState.class, partState);
        this.partType = partType;
    }

    public IPartType getPartType() {
        return partType;
    }

    public IPartState getPartState() {
        return getObject();
    }
}
