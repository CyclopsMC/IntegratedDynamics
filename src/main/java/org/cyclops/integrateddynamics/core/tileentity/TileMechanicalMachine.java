package org.cyclops.integrateddynamics.core.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.capability.item.ItemHandlerSlotMasked;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.network.MechanicalMachineNetworkElement;

import java.util.Optional;

/**
 * An abstract machine base tile entity that is able to process recipes by consuming energy.
 * @param <RCK> The recipe cache key type.
 * @param <R> The recipe type.
 */
public abstract class TileMechanicalMachine<RCK, R extends IRecipe> extends TileCableConnectableInventory
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

    private SingleCache<RCK, Optional<R>> recipeCache;

    public TileMechanicalMachine(TileEntityType<?> type, int inventorySize) {
        super(type, inventorySize, 64);

        // Add energy capability
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new MechanicalMachineNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        addCapabilityInternal(CapabilityEnergy.ENERGY, LazyOptional.of(() -> this));

        // Set inventory sides
        LazyOptional<IItemHandler> itemHandlerInput = LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), getInputSlots()));
        LazyOptional<IItemHandler> itemHandlerOutput = LazyOptional.of(() -> new ItemHandlerSlotMasked(getInventory(), getOutputSlots()));
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP, itemHandlerInput);
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN, itemHandlerOutput);
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH, itemHandlerInput);
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.SOUTH, itemHandlerOutput);
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.WEST, itemHandlerInput);
        addCapabilitySided(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.EAST, itemHandlerOutput);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(createCacheUpdater());
    }

    /**
     * @return A new cache updater instance.
     */
    protected abstract SingleCache.ICacheUpdater<RCK, Optional<R>> createCacheUpdater();

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
        return drainEnergy(rate, true) == rate && !world.isBlockPowered(getPos());
    }

    /**
     * @return If the machine is currently sleeping due to a recipe that could not be finalized.
     */
    public boolean isSleeping() {
        return this.sleep > 0;
    }

    public LazyOptional<IEnergyNetwork> getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    public void onTankChanged() {
        markDirty();
        getInventory().markDirty();
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean isItemValidForSlot(int i, ItemStack itemstack) {
                return ArrayUtils.contains(getInputSlots(), i) && super.isItemValidForSlot(i, itemstack);
            }

            @Override
            protected void onInventoryChanged() {
                super.onInventoryChanged();
                TileMechanicalMachine.this.sleep = -1;
            }
        };
    }

    /**
     * @return The recipe registry this machine should work with..
     */
    protected abstract IRecipeType<? extends R> getRecipeRegistry();

    /**
     * @return The current recipe cache key that is used to determine the current input of a recipe.
     */
    protected abstract RCK getCurrentRecipeCacheKey();

    /**
     * @return The currently applicable recipe.
     */
    public Optional<R> getCurrentRecipe() {
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
    public abstract int getRecipeDuration(R recipe);

    /**
     * Finalize a recipe.
     * This should insert the recipe output in the machine, and consume the input.
     * If the output could not be added, this method should return false.
     * @param recipe A recipe.
     * @param simulate If finalization should be simulated.
     * @return If finalization was successful.
     */
    protected abstract boolean finalizeRecipe(R recipe, boolean simulate);

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!world.isRemote()) {
            if (isSleeping()) {
                this.sleep--;
                this.markDirty();
            } else if (canWork()) {
                Optional<R> recipeOptional = getCurrentRecipe();
                if (recipeOptional.isPresent()) {
                    R recipe = recipeOptional.get();
                    if (progress == 0 && !finalizeRecipe(recipe, true)) {
                        sleep = SLEEP_TIME;
                    } else if (progress < getMaxProgress()) {
                        // // Consume energy while progressing
                        int toDrain = getEnergyConsumptionRate();
                        if (drainEnergy(toDrain, true) == toDrain) {
                            drainEnergy(toDrain, false);
                            progress++;
                            sleep = -1;
                        } else {
                            sleep = 1;
                        }
                    } else {
                        // Otherwise, finish and output

                        // First check if we have enough room for the recipe output,
                        // if not, we sleep for a while.
                        if (finalizeRecipe(recipe, true)) {
                            progress = 0;
                            finalizeRecipe(recipe, false);
                        } else {
                            sleep = 40;
                        }
                    }
                } else {
                    this.progress = -1;
                    this.sleep = -1;
                }
            }

            // Check if a state update is needed.
            updateWorkingState();
        }
    }

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

    protected void setEnergy(int energy) {
        int lastEnergy = this.energy;
        if (lastEnergy != energy) {
            this.energy = energy;
            markDirty();
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
}
