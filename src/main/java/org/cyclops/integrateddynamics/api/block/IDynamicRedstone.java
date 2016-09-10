package org.cyclops.integrateddynamics.api.block;

/**
 * Capability that can have its redstone level updated and stored.
 * @author rubensworks
 */
public interface IDynamicRedstone {

    /**
     * Set the redstone level.
     * @param level The redstone level.
     */
    public void setRedstoneLevel(int level);

    /**
     * Get the redstone level.
     * @return The redstone level.
     */
    public int getRedstoneLevel();

    /**
     * Set if this side allows redstone to be inputted.
     * @param allow If it allows input.
     */
    public void setAllowRedstoneInput(boolean allow);

    /**
     * If this side allows redstone to be inputted.
     * @return If it allows input.
     */
    public boolean isAllowRedstoneInput();

}
