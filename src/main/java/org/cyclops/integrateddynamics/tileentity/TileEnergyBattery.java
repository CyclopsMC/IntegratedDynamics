package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
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
public class TileEnergyBattery extends TileCableConnectable implements IEnergyStorageCapacity {

    @NBTPersist
    private int energy;
    @NBTPersist(useDefaultValue = false)
    private int capacity = BlockEnergyBatteryConfig.capacity;

    public TileEnergyBattery() {
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(CapabilityEnergy.ENERGY, this);
    }

    public boolean isCreative() {
        Block block = getBlockType();
        return block instanceof BlockEnergyBatteryBase && ((BlockEnergyBatteryBase) block).isCreative();
    }

    @Override
    public int getEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    protected void setEnergy(int energy) {
        if(!isCreative()) {
            int lastEnergy = this.energy;
            if (lastEnergy != energy) {
                this.energy = energy;
                markDirty();
                sendUpdate();
            }
        }
    }

    @Override
    protected int getUpdateBackoffTicks() {
        return 20;
    }

    @Override
    protected void onSendUpdate() {
        IBlockState blockState = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, blockState, blockState,
                MinecraftHelpers.BLOCK_NOTIFY | MinecraftHelpers.BLOCK_NOTIFY_CLIENT | MinecraftHelpers.BLOCK_NOTIFY_NO_RERENDER);
    }

    @Override
    public void onUpdateReceived() {
        super.onUpdateReceived();
    }

    public static int getEnergyPerTick(int capacity) {
        return Math.max(capacity / BlockEnergyBatteryConfig.energyRateCapacityFraction, BlockEnergyBatteryConfig.minEnergyRate);
    }

    protected int getEnergyPerTick() {
        return getEnergyPerTick(getMaxEnergyStored());
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        if(!isCreative()) {
            int stored = getEnergyStored();
            int energyReceived = Math.min(getMaxEnergyStored() - stored, energy);
            if(!simulate) {
                setEnergy(stored + energyReceived);
            }
            return energyReceived;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        if(isCreative()) return energy;
        energy = Math.max(0, Math.min(energy, getEnergyPerTick()));
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);;
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
        if (!getWorld().isRemote && getEnergyStored() > 0 && getWorld().isBlockPowered(getPos())) {
            addEnergy(Math.min(getEnergyPerTick(), getEnergyStored()));
        }
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
