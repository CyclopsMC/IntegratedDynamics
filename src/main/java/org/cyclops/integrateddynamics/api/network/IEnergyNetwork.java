package org.cyclops.integrateddynamics.api.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;

import java.util.Set;

/**
 * A network that accepts energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IEnergyBattery, IPartNetwork {

    /**
     * Add the position of a energy storage battery that must be accessible to the network.
     * @param pos The energy battery position.
     * @return If the battery was added to the network.
     */
    public boolean addEnergyBattery(DimPos pos);

    /**
     * Remove the position of a energy storage battery that was accessible to the network.
     * @param pos The energy battery position.
     */
    public void removeEnergyBattery(DimPos pos);

    /**
     * @return The energy batteries in this network.
     */
    public Set<DimPos> getEnergyBatteries();

    /**
     * @return The current network consumption rate.
     */
    public int getConsumptionRate();

}
