package org.cyclops.integrateddynamics.core.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.network.MechanicalMachineNetworkElement;

import java.util.Optional;

/**
 * An abstract machine base tile entity that is able to process recipes by consuming energy.
 * @param <RCK> The recipe cache key type.
 * @param <R> The recipe type.
 */
public abstract class BlockEntityMechanicalMachine<RCK, R extends Recipe<?>> extends BlockEntityCableConnectableInventory
        implements IEnergyStorage {

    /**
     * The number of ticks to sleep when the recipe could not be finalized.
     */
    private static int SLEEP_TIME = 40;

    @NBTPersist
    private int energy;
    @NBTPersist
    private int progress = -1;
    @NBTPersist
    private int sleep = -1;

    private SingleCache<RCK, Optional<RecipeHolder<R>>> recipeCache;

    public BlockEntityMechanicalMachine(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int inventorySize) {
        super(type, blockPos, blockState, inventorySize, 64);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(createCacheUpdater());
    }

    public static <E> void registerMechanicalMachineCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityMechanicalMachine> blockEntityType) {
        BlockEntityCableConnectableInventory.registerCableConnectableInventoryCapabilities(event, blockEntityType);

        event.registerBlockEntity(
                Capabilities.NetworkElementProvider.BLOCK,
                blockEntityType,
                (blockEntity, direction) -> blockEntity.getNetworkElementProvider()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                blockEntityType,
                (blockEntity, direction) -> blockEntity
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                blockEntityType,
                (blockEntity, direction) -> {
                    int[] slots = direction == Direction.DOWN ? blockEntity.getOutputSlots() : blockEntity.getInputSlots();
                    return new ItemHandlerSlotMasked(blockEntity.getInventory(), slots);
                }
        );
    }

    @Override
    public INetworkElementProvider getNetworkElementProvider() {
        return new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new MechanicalMachineNetworkElement(DimPos.of(world, blockPos));
            }
        };
    }

    /**
     * @return A new cache updater instance.
     */
    protected abstract SingleCache.ICacheUpdater<RCK, Optional<RecipeHolder<R>>> createCacheUpdater();

    /**
     * @return The available input slots.
     */
    public abstract int[] getInputSlots();

    /**
     * @return The available output slots.
     */
    public abstract int[] getOutputSlots();

    /**
     * @return If the machine was in a working state.
     */
    public abstract boolean wasWorking();

    /**
     * Set the new working state.
     * @param working If the machine is working.
     */
    public abstract void setWorking(boolean working);

    /**
     * @return If the machine currently has any work to process.
     */
    public boolean hasWork() {
        return getCurrentRecipe() != null;
    }

    /**
     * @return If the machine is currently working.
     */
    public boolean isWorking() {
        return this.progress >= 0 && this.sleep == -1;
    }

    /**
     * @return If the machine is able to work in its current state.
     *         This for example takes into account the available energy.
     */
    public boolean canWork() {
        int rate = getEnergyConsumptionRate();
        return drainEnergy(rate, true) == rate && !level.hasNeighborSignal(getBlockPos());
    }

    /**
     * @return If the machine is currently sleeping due to a recipe that could not be finalized.
     */
    public boolean isSleeping() {
        return this.sleep > 0;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getSleep() {
        return sleep;
    }

    public Optional<IEnergyNetwork> getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    public void onTankChanged() {
        setChanged();
        getInventory().setChanged();
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean canPlaceItem(int i, ItemStack itemstack) {
                return ArrayUtils.contains(getInputSlots(), i) && super.canPlaceItem(i, itemstack);
            }

            @Override
            protected void onInventoryChanged() {
                super.onInventoryChanged();
                BlockEntityMechanicalMachine.this.sleep = -1;
            }
        };
    }

    /**
     * @return The recipe registry this machine should work with..
     */
    protected abstract RecipeType<? extends R> getRecipeRegistry();

    /**
     * @return The current recipe cache key that is used to determine the current input of a recipe.
     */
    protected abstract RCK getCurrentRecipeCacheKey();

    /**
     * @return The currently applicable recipe.
     */
    public Optional<RecipeHolder<R>> getCurrentRecipe() {
        return recipeCache.get(getCurrentRecipeCacheKey());
    }

    /**
     * @return The current recipe progress, going from 0 to maxProgress.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return The current maximum progress.
     */
    public int getMaxProgress() {
        return this.getCurrentRecipe()
                .map(this::getRecipeDuration)
                .orElse(0);
    }

    /**
     * @param recipe A recipe.
     * @return The duration of a given recipe.
     */
    public abstract int getRecipeDuration(RecipeHolder<R> recipe);

    /**
     * Finalize a recipe.
     * This should insert the recipe output in the machine, and consume the input.
     * If the output could not be added, this method should return false.
     * @param recipe A recipe.
     * @param simulate If finalization should be simulated.
     * @return If finalization was successful.
     */
    protected abstract boolean finalizeRecipe(R recipe, boolean simulate);

    /**
     * Update the working state.
     */
    public void updateWorkingState() {
        boolean wasWorking = wasWorking();
        boolean isWorking = isWorking();
        if (isWorking != wasWorking) {
            setWorking(isWorking);
        }
    }

    /**
     * @return The energy consumption rate per (working) tick.
     */
    public abstract int getEnergyConsumptionRate();

    /**
     * Drain energy from the internal buffer or the attached network.
     * @param amount The amount of energy.
     * @param simulate If drainage should be simulated.
     * @return The drained energy.
     */
    protected int drainEnergy(int amount, boolean simulate) {
        int toDrain = amount;

        // First, check internal buffer
        toDrain -= this.extractEnergyInternal(toDrain, simulate);

        if (toDrain > 0) {
            // If we still need energy, ask it from the network.
            IEnergyNetwork energyNetwork = getEnergyNetwork().orElse(null);
            if (energyNetwork != null) {
                toDrain -= energyNetwork.getChannel(IPositionedAddonsNetwork.DEFAULT_CHANNEL).extract(toDrain, simulate);
            }
        }
        return amount - toDrain;
    }

    protected int extractEnergyInternal(int energy, boolean simulate) {
        energy = Math.max(0, energy);
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        int lastEnergy = this.energy;
        if (lastEnergy != energy) {
            this.energy = energy;
            setChanged();
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int stored = getEnergyStored();
        int energyReceived = Math.min(getMaxEnergyStored() - stored, maxReceive);
        if(!simulate) {
            setEnergy(stored + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public static class Ticker<RCK, R extends Recipe<?>, BE extends BlockEntityMechanicalMachine<RCK, R>> extends BlockEntityCableConnectableInventory.Ticker<BE> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BE blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.isSleeping()) {
                blockEntity.setSleep(blockEntity.getSleep() - 1);
                blockEntity.setChanged();
            } else if (blockEntity.canWork()) {
                Optional<RecipeHolder<R>> recipeOptional = blockEntity.getCurrentRecipe();
                if (recipeOptional.isPresent()) {
                    RecipeHolder<R> recipe = recipeOptional.get();
                    if (blockEntity.getProgress() == 0 && !blockEntity.finalizeRecipe(recipe.value(), true)) {
                        blockEntity.setSleep(SLEEP_TIME);
                    } else if (blockEntity.getProgress() < blockEntity.getMaxProgress()) {
                        // // Consume energy while progressing
                        int toDrain = blockEntity.getEnergyConsumptionRate();
                        if (blockEntity.drainEnergy(toDrain, true) == toDrain) {
                            blockEntity.drainEnergy(toDrain, false);
                            blockEntity.setProgress(blockEntity.getProgress() + 1);
                            blockEntity.setSleep(-1);
                        } else {
                            blockEntity.setSleep(1);
                        }
                    } else {
                        // Otherwise, finish and output

                        // First check if we have enough room for the recipe output,
                        // if not, we sleep for a while.
                        if (blockEntity.finalizeRecipe(recipe.value(), true)) {
                            blockEntity.setProgress(0);
                            blockEntity.finalizeRecipe(recipe.value(), false);
                        } else {
                            blockEntity.setSleep(40);
                        }
                    }
                } else {
                    blockEntity.setProgress(-1);
                    blockEntity.setSleep(-1);
                }
            }

            // Check if a state update is needed.
            blockEntity.updateWorkingState();
        }
    }
}
