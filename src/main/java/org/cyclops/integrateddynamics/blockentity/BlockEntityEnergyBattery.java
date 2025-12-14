package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.energystorage.SimpleEnergyHandlerCapacity;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectable;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;

import java.util.function.Supplier;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class BlockEntityEnergyBattery extends BlockEntityCableConnectable {

    private SimpleEnergyHandlerCapacity energyHandler;

    public BlockEntityEnergyBattery(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_ENERGY_BATTERY.get(), blockPos, blockState);
        this.energyHandler = new SimpleEnergyHandlerCapacity(BlockEnergyBatteryConfig.capacity) {
            @Override
            protected void onEnergyChanged(int previousAmount) {
                super.onEnergyChanged(previousAmount);
                setChanged();
                sendUpdate();
            }
        };
    }

    public static class CapabilityRegistrar extends BlockEntityCableConnectable.CapabilityRegistrar<BlockEntityEnergyBattery> {
        public CapabilityRegistrar(Supplier<BlockEntityType<? extends BlockEntityEnergyBattery>> blockEntityType) {
            super(blockEntityType);
        }

        @Override
        public void populate() {
            super.populate();

            add(
                    Capabilities.NetworkElementProvider.BLOCK,
                    (blockEntity, context) -> blockEntity.getNetworkElementProvider()
            );
            add(
                    net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK,
                    (blockEntity, context) -> blockEntity.getEnergyHandler()
            );
        }
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

    @Override
    public void read(ValueInput input) {
        super.read(input);
        energyHandler.deserialize(input);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energyHandler.serialize(output);
    }

    public SimpleEnergyHandlerCapacity getEnergyHandler() {
        return energyHandler;
    }

    public boolean isCreative() {
        Block block = getBlockState().getBlock();
        return block instanceof BlockEnergyBatteryBase && ((BlockEnergyBatteryBase) block).isCreative();
    }

    public void setEnergyStored(int energy) {
        this.energyHandler.set(energy);
    }

    public int getEnergyStored() {
        return this.energyHandler.getAmountAsInt();
    }

    public int getMaxEnergyStored() {
        return this.energyHandler.getCapacityAsInt();
    }

    @Override
    public int getUpdateBackoffTicks() {
        return 20;
    }

    public static int getEnergyPerTick(long capacity) {
        return Math.max(IModHelpers.get().getBaseHelpers().castSafe(capacity) / BlockEnergyBatteryConfig.energyRateCapacityFraction, BlockEnergyBatteryConfig.minEnergyRate);
    }

    protected long getEnergyPerTick() {
        return getEnergyPerTick(getMaxEnergyStored());
    }

    protected int addEnergy(int energy) {
        int filled = addEnergyFe(energy, false);
        try (var tx = Transaction.openRoot()) {
            this.energyHandler.extract(filled, tx);
            tx.commit();
        }
        return filled;
    }

    protected int addEnergyFe(int energy, boolean simulate) {
        return EnergyHelpers.fillNeigbours(getLevel(), getBlockPos(), energy, simulate);
    }

    public static class Ticker extends BlockEntityCableConnectable.Ticker<BlockEntityEnergyBattery> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityEnergyBattery blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.getEnergyStored() > 0 && level.hasNeighborSignal(pos)) {
                blockEntity.addEnergy(Math.min((int) blockEntity.getEnergyPerTick(), blockEntity.getEnergyStored()));
            }
        }

        @Override
        protected void onSendUpdate(Level level, BlockPos pos) {
            BlockState blockState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, blockState, blockState,
                    IModHelpers.get().getMinecraftHelpers().getBlockNotify() | IModHelpers.get().getMinecraftHelpers().getBlockNotifyClient() | IModHelpers.get().getMinecraftHelpers().getBlockNotifyNoRerender());
        }
    }
}
