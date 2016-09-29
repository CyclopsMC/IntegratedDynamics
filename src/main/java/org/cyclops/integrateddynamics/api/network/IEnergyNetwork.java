package org.cyclops.integrateddynamics.api.network;

import net.minecraftforge.energy.IEnergyStorage;

/**
 * A network capability that holds energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IEnergyStorage, IPositionedAddonsNetwork {

    /**
     * @return The current network consumption rate.
     */
    public int getConsumptionRate();

}
