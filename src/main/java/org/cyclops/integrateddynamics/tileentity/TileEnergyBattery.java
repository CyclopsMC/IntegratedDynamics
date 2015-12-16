package org.cyclops.integrateddynamics.tileentity;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectable;

/**
 * A tile entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileEnergyBattery extends TileCableConnectable implements IEnergyBattery {

    @NBTPersist
    private int energy;

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public int getStoredEnergy() {
        return this.energy;
    }

    @Override
    public int getMaxStoredEnergy() {
        return BlockEnergyBatteryConfig.capacity;
    }

    public void updateBlockState() {
        int fill = (int) Math.floor(((float) energy * (BlockEnergyBattery.FILL.getAllowedValues().size() - 1)) / (float) getMaxStoredEnergy());
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).withProperty(BlockEnergyBattery.FILL, fill));
    }

    protected void setEnergy(int energy) {
        this.energy = energy;
        updateBlockState();
        sendUpdate();
    }

    @Override
    public void addEnergy(int energy) {
        int newEnergy = getStoredEnergy() + energy;
        setEnergy(Math.min(newEnergy, getMaxStoredEnergy()));
    }

    @Override
    public int consume(int energy, boolean simulate) {
        int stored = getStoredEnergy();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

}
