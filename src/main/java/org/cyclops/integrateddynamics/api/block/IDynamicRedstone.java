package org.cyclops.integrateddynamics.api.block;

/**
 * Capability that can have its redstone level updated and stored.
 * @author rubensworks
 */
public interface IDynamicRedstone {

    /**
     * Set the redstone level.
     * @param level The redstone level.
     * @param strongPower If the redstone power should be strong.
     */
    public void setRedstoneLevel(int level, boolean strongPower);

    /**
     * Get the redstone level.
     * @return The redstone level.
     */
    public int getRedstoneLevel();

    /**
     * @return If the redstone power is strong.
     */
    public boolean isStrong();

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

    /**
     * Store the last value that was used to trigger a redstone pulse.
     * @param value A pulse value.
     */
    public void setLastPulseValue(int value);

    /**
     * @return The last pulse value.
     */
    public int getLastPulseValue();

}
