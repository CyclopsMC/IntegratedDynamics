package org.cyclops.integrateddynamics.modcompat.rf;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;

/**
 * Energy Storage wrapper for RF.
 * @author rubensworks
 */
public class EnergyStorageRf implements IEnergyStorage {

    private final IBlockAccess world;
    private final BlockPos pos;
    private final EnumFacing facing;

    private IEnergyHandler energyStorage = null;
    private IEnergyReceiver energyReceiver = null;
    private IEnergyProvider energyProvider = null;

    public EnergyStorageRf(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        this.world = world;
        this.pos = pos;
        this.facing = facing;
    }

    protected IEnergyHandler getEnergyStorage() {
        if (energyStorage != null) return energyStorage;
        return energyStorage = TileHelpers.getSafeTile(world, pos, IEnergyHandler.class);
    }

    protected IEnergyReceiver getEnergyReceiver() {
        if (energyReceiver != null) return energyReceiver;
        return energyReceiver = TileHelpers.getSafeTile(world, pos, IEnergyReceiver.class);
    }

    protected IEnergyProvider getEnergyProvider() {
        if (energyProvider != null) return energyProvider;
        return energyProvider = TileHelpers.getSafeTile(world, pos, IEnergyProvider.class);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        IEnergyReceiver energyReceiver = getEnergyReceiver();
        return energyReceiver != null ? energyReceiver.receiveEnergy(facing, maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        IEnergyProvider energyProvider = getEnergyProvider();
        return energyProvider != null ? energyProvider.extractEnergy(facing, maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        IEnergyHandler energyStorage = getEnergyStorage();
        return energyStorage.getEnergyStored(facing);
    }

    @Override
    public int getMaxEnergyStored() {
        IEnergyHandler energyStorage = getEnergyStorage();
        return energyStorage.getMaxEnergyStored(facing);
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
