package org.cyclops.integrateddynamics.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectable;

/**
 * A tile entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "cofh.api.energy.IEnergyProvider", modid = Reference.MOD_RF_API, striprefs = true),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = Reference.MOD_RF_API, striprefs = true)
})
public class TileEnergyBattery extends TileCableConnectable implements IEnergyBattery, IEnergyProvider, IEnergyReceiver {

    @NBTPersist
    private int energy;

    protected boolean isCreative() {
        return ((BlockEnergyBatteryBase) getBlock()).isCreative();
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public int getStoredEnergy() {
        if(isCreative()) return Integer.MAX_VALUE;
        return this.energy;
    }

    @Override
    public int getMaxStoredEnergy() {
        if(isCreative()) return Integer.MAX_VALUE;
        return BlockEnergyBatteryConfig.capacity;
    }

    public void updateBlockState() {
        if(!isCreative()) {
            IBlockState blockState = getWorld().getBlockState(getPos());
            if (blockState.getBlock() == BlockEnergyBattery.getInstance()) {
                int fill = (int) Math.floor(((float) energy * (BlockEnergyBattery.FILL.getAllowedValues().size() - 1)) / (float) getMaxStoredEnergy());
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
    public int addEnergy(int energy, boolean simulate) {
        if(!isCreative()) {
            int stored = getStoredEnergy();
            int newEnergy = Math.min(stored + energy, getMaxStoredEnergy());
            if(!simulate) {
                setEnergy(newEnergy);
            }
            return newEnergy - stored;
        }
        return 0;
    }

    @Override
    public int consume(int energy, boolean simulate) {
        if(isCreative()) return energy;
        int stored = getStoredEnergy();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

    /*
     * ------------------ RF API ------------------
     */

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return consume(maxExtract, simulate);
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getEnergyStored(EnumFacing from) {
        return getStoredEnergy();
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return getMaxStoredEnergy();
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Optional.Method(modid = Reference.MOD_RF_API)
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return addEnergy(maxReceive, simulate);
    }
}
