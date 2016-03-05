package org.cyclops.integrateddynamics.api.part;

import net.minecraftforge.event.AttachCapabilitiesEvent;

/**
 * Event for when an {@link IPartState} is being constructed.
 * @author rubensworks
 */
public class AttachCapabilitiesEventPart extends AttachCapabilitiesEvent {

    private final IPartType partType;
    private final IPartState partState;

    public AttachCapabilitiesEventPart(IPartType partType, IPartState partState) {
        super(partState);
        this.partType = partType;
        this.partState = partState;
    }

    public IPartType getPartType() {
        return partType;
    }

    public IPartState getPartState() {
        return partState;
    }
}
