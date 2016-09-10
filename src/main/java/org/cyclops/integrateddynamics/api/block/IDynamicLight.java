package org.cyclops.integrateddynamics.api.block;

/**
 * Capability that can have its light level updated and stored.
 * @author rubensworks
 */
public interface IDynamicLight {

    /**
     * Set the light level.
     * @param level The light level.
     */
    public void setLightLevel(int level);

    /**
     * Get the light level.
     * @return The light level.
     */
    public int getLightLevel();

}
