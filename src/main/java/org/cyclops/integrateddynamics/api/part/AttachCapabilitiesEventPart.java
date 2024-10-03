package org.cyclops.integrateddynamics.api.part;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Event for when an {@link IPartState} is being constructed.
 * @author rubensworks
 */
public class AttachCapabilitiesEventPart extends Event {

    private final IPartType partType;
    private final IPartState partState;

    public AttachCapabilitiesEventPart(IPartType partType, IPartState partState) {
        this.partType = partType;
        this.partState = partState;
    }

    public IPartType getPartType() {
        return partType;
    }

    public IPartState getPartState() {
        return this.partState;
    }

    public <T> void register(
            PartCapability<T> capability,
            IPartType<?, ?> partType,
            ICapabilityProvider<IPartType<?, ?>, PartTarget, T> provider
    ) {
        Objects.requireNonNull(provider);
        capability.providers.computeIfAbsent(Objects.requireNonNull(partType), i -> new ArrayList<>()).add(provider);
    }

    public boolean isRegistered(PartCapability<?> capability, IPartType<?, ?> partType) {
        Objects.requireNonNull(partType);
        return capability.providers.containsKey(partType);
    }
}
