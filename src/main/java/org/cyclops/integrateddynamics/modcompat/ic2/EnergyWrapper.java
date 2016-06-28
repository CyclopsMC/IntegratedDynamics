package org.cyclops.integrateddynamics.modcompat.ic2;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;

/**
 * @author rubensworks
 */
public class EnergyWrapper implements IEnergyWrapper {

    private final Energy energy;

    public EnergyWrapper(TileEntityBlock tile) {
        this.energy = tile.getComponent(Energy.class);
    }

    @Override
    public int getStored() {
        return (int) energy.getEnergy();
    }

    @Override
    public int getCapacity() {
        return (int) energy.getCapacity();
    }
}
