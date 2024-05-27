package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectable;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class BlockEntityEnergyBattery extends BlockEntityCableConnectable implements IEnergyStorageCapacity {

    @NBTPersist
    private int energy;
    @NBTPersist(useDefaultValue = false)
    private int capacity = BlockEnergyBatteryConfig.capacity;

    public BlockEntityEnergyBattery(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_ENERGY_BATTERY.get(), blockPos, blockState);
    }

    public static void registerEnergyBatteryCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityEnergyBattery> blockEntityType) {
        BlockEntityCableConnectable.registerCableConnectableCapabilities(event, blockEntityType);

        event.registerBlockEntity(
                Capabilities.NetworkElementProvider.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getNetworkElementProvider()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                blockEntityType,
                (blockEntity, context) -> ((BlockEntityEnergyBattery) blockEntity)
        );
    }

    @Override
    public INetworkElementProvider getNetworkElementProvider() {
        return new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
            }
        };
    }

    public boolean isCreative() {
        Block block = getBlockState().getBlock();
        return block instanceof BlockEnergyBatteryBase && ((BlockEnergyBatteryBase) block).isCreative();
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;
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
                setChanged();
                sendUpdate();
            }
        }
    }

    @Override
    public int getUpdateBackoffTicks() {
        return 20;
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
        return EnergyHelpers.fillNeigbours(getLevel(), getBlockPos(), energy, simulate);
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public static class Ticker extends BlockEntityCableConnectable.Ticker<BlockEntityEnergyBattery> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityEnergyBattery blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.getEnergyStored() > 0 && level.hasNeighborSignal(pos)) {
                blockEntity.addEnergy(Math.min(blockEntity.getEnergyPerTick(), blockEntity.getEnergyStored()));
            }
        }

        @Override
        protected void onSendUpdate(Level level, BlockPos pos) {
            BlockState blockState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, blockState, blockState,
                    MinecraftHelpers.BLOCK_NOTIFY | MinecraftHelpers.BLOCK_NOTIFY_CLIENT | MinecraftHelpers.BLOCK_NOTIFY_NO_RERENDER);
        }
    }
}
