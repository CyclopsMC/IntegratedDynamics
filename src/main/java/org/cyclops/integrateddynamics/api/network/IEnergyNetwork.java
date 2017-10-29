package org.cyclops.integrateddynamics.api.network;

import net.minecraftforge.energy.IEnergyStorage;

/**
 * A network capability that holds energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IPositionedAddonsNetwork {
    public static final int DEFAULT_CHANNEL = 0;

    /**
     * @return The current network consumption rate.
     */
    public int getConsumptionRate();

    /**
     * @return An IEnergyStorage that only interacts with the given channel.
     */
    public IEnergyStorage getChannel(int channel);

    public int getEnergyStored();
    public int getMaxEnergyStored();

}
