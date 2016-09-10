package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import org.cyclops.integrateddynamics.api.block.IDynamicLight;

/**
 * Dynamic light for MCMP parts.
 * @author rubensworks
 */
public class DynamicLightPart implements IDynamicLight {

    private final PartCable partCable;

    public DynamicLightPart(PartCable partCable) {
        this.partCable = partCable;
    }

    @Override
    public void setLightLevel(int level) {
        partCable.setLightLevel(level);
    }

    @Override
    public int getLightLevel() {
        return partCable.getLightLevel();
    }
}
