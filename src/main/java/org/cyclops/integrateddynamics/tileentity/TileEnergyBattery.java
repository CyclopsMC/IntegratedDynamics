package org.cyclops.integrateddynamics.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectable;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "cofh.api.energy.IEnergyProvider", modid = Reference.MOD_RF_API, striprefs = true),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = Reference.MOD_RF_API, striprefs = true)
})
public class TileEnergyBattery extends TileCableConnectable implements IEnergyStorage, IEnergyProvider, IEnergyReceiver {

    @NBTPersist
    private int energy;

    public TileEnergyBattery() {
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(CapabilityEnergy.ENERGY, this);
    }

    protected boolean isCreative() {
        return ((BlockEnergyBatteryBase) getBlock()).isCreative();
    }

    @Override
    public int getEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return BlockEnergyBatteryConfig.capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void updateBlockState() {
        if(!isCreative()) {
            IBlockState blockState = getWorld().getBlockState(getPos());
            if (blockState.getBlock() == BlockEnergyBattery.getInstance()) {
                int fill = (int) Math.floor(((float) energy * (BlockEnergyBattery.FILL.getAllowedValues().size() - 1)) / (float) getMaxEnergyStored());
                getWorld().setBlockState(getPos(), blockState.withProperty(BlockEnergyBattery.FILL, fill));
            }
        }
    }

    protected void setEnergy(int energy) {
        if(!isCreative()) {
            this.energy = energy;
            updateBlockState();
            sendUpdate();
        }
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        if(!isCreative()) {
            int stored = getEnergyStored();
            int newEnergy = Math.min(stored + energy, getMaxEnergyStored());
            if(!simulate) {
                setEnergy(newEnergy);
            }
            return newEnergy - stored;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        if(isCreative()) return energy;
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

    protected int addEnergy(int energy) {
        int filled = addEnergyFe(energy, false);
        extractEnergy(filled, false);
        return filled;
    }

    protected int addEnergyFe(int energy, boolean simulate) {
        return EnergyHelpers.fillNeigbours(getWorld(), getPos(), energy, simulate);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (getEnergyStored() > 0 && getWorld().isBlockPowered(getPos())) {
            addEnergy(Math.min(BlockEnergyBatteryConfig.energyPerTick, getEnergyStored()));
            markDirty();
        }
    }

    /*
     * ------------------ RF API ------------------
     */

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return extractEnergy(maxExtract, simulate);
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getEnergyStored(EnumFacing from) {
        return getMaxEnergyStored();
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return getMaxEnergyStored();
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return receiveEnergy(maxReceive, simulate);
    }
}
