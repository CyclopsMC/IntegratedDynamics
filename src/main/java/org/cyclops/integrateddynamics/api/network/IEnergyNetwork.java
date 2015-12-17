package org.cyclops.integrateddynamics.api.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IEnergyBatteryFacade;

import java.util.Map;

/**
 * A network that accepts energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IPartNetwork {

    /**
     * @return The currently stored energy.
     */
    public int getStoredEnergy();

    /**
     * @return The maximum amount of energy that can be stored.
     */
    public int getMaxStoredEnergy();

    /**
     * Add the given energy amount from the network.
     * @param energy The energy amount to add.
     * @param simulate If the addition should be stimulated.
     * @return The amount of energy that was added.
     */
    public int addEnergy(int energy, boolean simulate);

    /**
     * Remove the given energy amount from the network.
     * @param energy The energy amount to remove.
     * @param simulate If the consumption should be stimulated.
     * @return The amount of energy that was consumed.
     */
    public int consume(int energy, boolean simulate);

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
    public Map<DimPos, IEnergyBatteryFacade> getEnergyBatteries();

}
