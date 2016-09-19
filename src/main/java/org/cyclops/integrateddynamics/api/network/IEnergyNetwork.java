package org.cyclops.integrateddynamics.api.network;

import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.datastructure.DimPos;

import java.util.Set;

/**
 * A network capability that holds energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IEnergyStorage {

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
