package org.cyclops.integrateddynamics.modcompat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;

/**
 * Energy Storage wrapper for Tesla.
 * @author rubensworks
 */
public class EnergyStorageTesla implements IEnergyStorage {

    private final IBlockAccess world;
    private final BlockPos pos;
    private final EnumFacing facing;

    private ITeslaHolder energyStorage = null;
    private ITeslaConsumer energyReceiver = null;
    private ITeslaProducer energyProvider = null;

    public EnergyStorageTesla(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        this.world = world;
        this.pos = pos;
        this.facing = facing;
    }

    protected ITeslaHolder getEnergyStorage() {
        if (energyStorage != null) return energyStorage;
        return energyStorage = TileHelpers.getCapability(world, pos, facing, TeslaCapabilities.CAPABILITY_HOLDER);
    }

    protected ITeslaConsumer getEnergyReceiver() {
        if (energyReceiver != null) return energyReceiver;
        return energyReceiver = TileHelpers.getCapability(world, pos, facing, TeslaCapabilities.CAPABILITY_CONSUMER);
    }

    protected ITeslaProducer getEnergyProvider() {
        if (energyProvider != null) return energyProvider;
        return energyProvider = TileHelpers.getCapability(world, pos, facing, TeslaCapabilities.CAPABILITY_PRODUCER);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        ITeslaConsumer energyReceiver = getEnergyReceiver();
        return energyReceiver != null ? (int) energyReceiver.givePower(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        ITeslaProducer energyProvider = getEnergyProvider();
        return energyProvider != null ? (int) energyProvider.takePower(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        ITeslaHolder energyStorage = getEnergyStorage();
        return energyStorage != null ? (int) Math.min(Integer.MAX_VALUE, energyStorage.getStoredPower()) : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        ITeslaHolder energyStorage = getEnergyStorage();
        return energyStorage != null ? (int) Math.min(Integer.MAX_VALUE, energyStorage.getCapacity()) : 0;
    }

    @Override
    public boolean canExtract() {
        return getEnergyProvider() != null;
    }

    @Override
    public boolean canReceive() {
        return getEnergyReceiver() != null;
    }
}
