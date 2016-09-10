package org.cyclops.integrateddynamics.capability.energybattery;

import org.cyclops.integrateddynamics.api.block.IEnergyBattery;

/**
 * Default implementation of the energy battery.
 * @author rubensworks
 */
public class EnergyBatteryDefault implements IEnergyBattery {
    @Override
    public int getStoredEnergy() {
        return 0;
    }

    @Override
    public int getMaxStoredEnergy() {
        return 0;
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        return 0;
    }

    @Override
    public int consume(int energy, boolean simulate) {
        return 0;
    }
}
