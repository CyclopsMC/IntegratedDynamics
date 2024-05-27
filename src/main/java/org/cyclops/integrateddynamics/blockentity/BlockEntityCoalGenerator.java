package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.datastructure.DataSlotSupplied;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.block.BlockCoalGenerator;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.network.CoalGeneratorNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the coal energy generator.
 * @author rubensworks
 */
public class BlockEntityCoalGenerator extends BlockEntityCableConnectableInventory implements IEnergyStorage, MenuProvider {

    public static final int INVENTORY_SIZE = 1;
    public static final int MAX_PROGRESS = 13;
    public static final int ENERGY_PER_TICK = 20;
    public static final int SLOT_FUEL = 0;

    @NBTPersist
    private int currentlyBurningMax;
    @NBTPersist
    private int currentlyBurning;

    public BlockEntityCoalGenerator(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_COAL_GENERATOR.get(), blockPos, blockState, BlockEntityCoalGenerator.INVENTORY_SIZE, 64);
    }

    public static void registerCoalGeneratorCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityCoalGenerator> blockEntityType) {
        BlockEntityCableConnectableInventory.registerCableConnectableInventoryCapabilities(event, blockEntityType);

        event.registerBlockEntity(
                Capabilities.NetworkElementProvider.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getNetworkElementProvider()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity
        );
    }

    @Override
    public INetworkElementProvider getNetworkElementProvider() {
        return new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new CoalGeneratorNetworkElement(DimPos.of(world, blockPos));
            }
        };
    }

    public Optional<IEnergyNetwork> getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    public void updateBlockState() {
        boolean wasBurning = getLevel().getBlockState(getBlockPos()).getValue(BlockCoalGenerator.LIT);
        boolean isBurning = isBurning();
        if (isBurning != wasBurning) {
            getLevel().setBlockAndUpdate(getBlockPos(),
                    getLevel().getBlockState(getBlockPos()).setValue(BlockCoalGenerator.LIT, isBurning));
        }
    }

    public int getProgress() {
        float current = currentlyBurning;
        float max = currentlyBurningMax;
        if (max == 0) {
            return -1;
        }
        return Math.round((current / max) * (float) MAX_PROGRESS);
    }

    public boolean isBurning() {
        return currentlyBurning < currentlyBurningMax;
    }

    public boolean canAddEnergy(int energy) {
        IEnergyNetwork network = getEnergyNetwork().orElse(null);
        if(network != null && network.getChannel(IPositionedAddonsNetwork.DEFAULT_CHANNEL).insert((long) energy, true) == 0) {
            return true;
        }
        return addEnergyFe(energy, true) == energy;
    }

    protected int addEnergy(int energy) {
        IEnergyNetwork network = getEnergyNetwork().orElse(null);
        int toFill = energy;
        if(network != null) {
            toFill = Helpers.castSafe(network.getChannel(IPositionedAddonsNetwork.DEFAULT_CHANNEL).insert((long) toFill, false));
        }
        if(toFill > 0) {
            toFill -= addEnergyFe(toFill, false);
        }
        return energy - toFill;
    }

    protected int addEnergyFe(int energy, boolean simulate) {
        return EnergyHelpers.fillNeigbours(getLevel(), getBlockPos(), energy, simulate);
    }

    public static int getFuelTime(ItemStack itemStack) {
        return CommonHooks.getBurnTime(itemStack, RecipeType.SMELTING);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integrateddynamics.coal_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerCoalGenerator(id, playerInventory, this.getInventory(), new DataSlotSupplied(this::getProgress));
    }

    public static class Ticker extends BlockEntityCableConnectableInventory.Ticker<BlockEntityCoalGenerator> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityCoalGenerator blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if((!blockEntity.getInventory().getItem(SLOT_FUEL).isEmpty() || blockEntity.isBurning()) && blockEntity.canAddEnergy(ENERGY_PER_TICK)) {
                if (blockEntity.isBurning()) {
                    if (blockEntity.currentlyBurning++ >= blockEntity.currentlyBurningMax) {
                        blockEntity.currentlyBurning = 0;
                        blockEntity.currentlyBurningMax = 0;
                    }
                    int toFill = ENERGY_PER_TICK;
                    blockEntity.addEnergy(toFill);
                    blockEntity.setChanged();
                }
                if (!blockEntity.isBurning()) {
                    ItemStack fuel;
                    if (getFuelTime(blockEntity.getInventory().getItem(SLOT_FUEL)) > 0
                            && !(fuel = blockEntity.getInventory().removeItem(SLOT_FUEL, 1)).isEmpty()) {
                        if(blockEntity.getInventory().getItem(SLOT_FUEL).isEmpty()) {
                            blockEntity.getInventory().setItem(SLOT_FUEL, fuel.getItem().getCraftingRemainingItem(fuel));
                        }
                        blockEntity.currentlyBurningMax = getFuelTime(fuel);
                        blockEntity.currentlyBurning = 0;
                        blockEntity.setChanged();
                    }
                    blockEntity.updateBlockState();
                }
            }
        }
    }
}
