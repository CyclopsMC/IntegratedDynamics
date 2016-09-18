package org.cyclops.integrateddynamics.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.energybattery.EnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectable;
import org.cyclops.integrateddynamics.modcompat.rf.RfHelpers;
import org.cyclops.integrateddynamics.modcompat.tesla.TeslaHelpers;
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
public class TileEnergyBattery extends TileCableConnectable implements IEnergyBattery, IEnergyProvider, IEnergyReceiver {

    @NBTPersist
    private int energy;

    public TileEnergyBattery() {
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(EnergyBatteryConfig.CAPABILITY, this);
    }

    protected boolean isCreative() {
        return ((BlockEnergyBatteryBase) getBlock()).isCreative();
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

    protected int addEnergyRf(int energy, boolean simulate) {
        int filled = RfHelpers.fillNeigbours(getWorld(), getPos(), energy, simulate);
        consume(filled, simulate);
        return filled;
    }

    protected int addEnergyTesla(int energy, boolean simulate) {
        int filled = TeslaHelpers.fillNeigbours(getWorld(), getPos(), energy, simulate);
        consume(filled, simulate);
        return filled;
    }

    protected boolean isRf() {
        return RfHelpers.isRf();
    }

    protected boolean isTesla() {
        return TeslaHelpers.isTesla();
    }

    protected int addEnergy(int energy) {
        int toFill = energy;
        if(toFill > 0 && isRf()) {
            toFill -= addEnergyRf(toFill, false);
        }
        if(toFill > 0 && isTesla()) {
            toFill -= addEnergyTesla(toFill, false);
        }
        return energy - toFill;
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (getStoredEnergy() > 0 && getWorld().isBlockPowered(getPos())) {
            addEnergy(Math.min(BlockEnergyBatteryConfig.energyPerTick, getStoredEnergy()));
            markDirty();
        }
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
