package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartType;

import javax.annotation.Nullable;

/**
 * An event thrown when a network is loaded with invalid parts.
 * @author rubensworks
 */
public class UnknownPartEvent extends NetworkEvent {

    private final String partTypeName;
    private IPartType partType;

    public UnknownPartEvent(IPartNetwork network, String partTypeName) {
        super(network);
        this.partTypeName = partTypeName;
        this.partType = null;
    }

    /**
     * @return The part type.
     */
    public @Nullable IPartType getPartType() {
        return partType;
    }

    /**
     * Set the part type to load instead.
     * @param partType The part type to load.
     */
    public void setPartType(IPartType partType) {
        this.partType = partType;
    }

    /**
     * @return The part name that is being loaded but no corresponding part is found in the registry.
     */
    public String getPartTypeName() {
        return partTypeName;
    }

}
