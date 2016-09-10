package org.cyclops.integrateddynamics.capability.dynamiclight;

import org.cyclops.integrateddynamics.api.block.IDynamicLight;

/**
 * Default implementation of {@link IDynamicLight}.
 * @author rubensworks
 */
public class DynamicLightDefault implements IDynamicLight {

    @Override
    public void setLightLevel(int level) {

    }

    @Override
    public int getLightLevel() {
        return 0;
    }
}
