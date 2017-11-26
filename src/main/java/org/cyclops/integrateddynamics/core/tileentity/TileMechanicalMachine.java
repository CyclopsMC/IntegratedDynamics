package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeInput;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeOutput;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.network.MechanicalSqueezerNetworkElement;

import java.util.Set;

/**
 * An abstract machine base tile entity that is able to process recipes by consuming energy.
 * @param <RCK> The recipe cache key type.
 * @param <M> The machine type.
 * @param <I> The recipe input type.
 * @param <O> The recipe output type.
 * @param <P> The recipe properties type.
 */
public abstract class TileMechanicalMachine<RCK, M extends IMachine<M, I, O, P>, I extends IRecipeInput,
        O extends IRecipeOutput, P extends IRecipeProperties> extends TileCableConnectableInventory
        implements IEnergyStorage, SingleUseTank.IUpdateListener {

    /**
     * The number of ticks to sleep when the recipe could not be finalized.
     */
    private static int SLEEP_TIME = 40;

    @NBTPersist
    private int energy;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int sleep = -1;

    private SingleCache<RCK, IRecipe<I, O, P>> recipeCache;

    public TileMechanicalMachine(int inventorySize) {
        super(inventorySize, "machine", 64);

        // Add energy capability
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new MechanicalSqueezerNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(CapabilityEnergy.ENERGY, this);

        // Set inventory sides
        Set<Integer> in = Sets.newHashSet(ArrayUtils.toObject(getInputSlots()));
        Set<Integer> out = Sets.newHashSet(ArrayUtils.toObject(getOutputSlots()));
        addSlotsToSide(EnumFacing.UP, in);
        addSlotsToSide(EnumFacing.DOWN, out);
        addSlotsToSide(EnumFacing.NORTH, in);
        addSlotsToSide(EnumFacing.SOUTH, out);
        addSlotsToSide(EnumFacing.WEST, in);
        addSlotsToSide(EnumFacing.EAST, out);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(createCacheUpdater());
    }

    /**
     * @return A new cache updater instance.
     */
    protected abstract SingleCache.ICacheUpdater<RCK, IRecipe<I, O, P>> createCacheUpdater();

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
        return this.progress > 0 && this.sleep == -1;
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

    public IEnergyNetwork getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    @Override
    public void onTankChanged() {
        sendUpdate();
        updateInventoryHash();
    }

    @Override
    protected void onInventoryChanged() {
        super.onInventoryChanged();
        this.sleep = 0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (ArrayUtils.contains(getInputSlots(), index)) {
            NonNullList<ItemStack> inputStacks = NonNullList.create();
            for (int slot : getInputSlots()) {
                if (slot == index) {
                    inputStacks.add(stack);
                } else {
                    inputStacks.add(getStackInSlot(slot));
                }
            }
            // Only allow items to be inserted that are used in at least once recipe.
            return getRecipeRegistry().findRecipeByInput(getRecipeInput(inputStacks)) != null;
        }
        return super.isItemValidForSlot(index, stack);
    }

    /**
     * @return The recipe registry this machine should work with..
     */
    protected abstract IRecipeRegistry<M, I, O, P> getRecipeRegistry();

    /**
     * @return The current recipe cache key that is used to determine the current input of a recipe.
     */
    protected abstract RCK getCurrentRecipeCacheKey();

    /**
     * @return The currently applicable recipe.
     */
    public IRecipe<I, O, P> getCurrentRecipe() {
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
        return this.getCurrentRecipe() != null ? getRecipeDuration(getCurrentRecipe()) : 0;
    }

    /**
     * Create a new recipe input holder for the given input stacks.
     * This is used to check which items can be inserted into which slots.
     * @param inputStacks The given input stacks.
     *                    These are not guaranteed to be the stacks that are currently in the inventory.
     * @return A recipe input holder.
     */
    public abstract I getRecipeInput(NonNullList<ItemStack> inputStacks);

    /**
     * @param recipe A recipe.
     * @return The duration of a given recipe.
     */
    public abstract int getRecipeDuration(IRecipe<I, O, P> recipe);

    /**
     * Finalize a recipe.
     * This should insert the recipe output in the machine, and consume the input.
     * If the output could not be added, this method should return false.
     * @param recipe A recipe.
     * @param simulate If finalization should be simulated.
     * @return If finalization was successful.
     */
    protected abstract boolean finalizeRecipe(IRecipe<I, O, P> recipe, boolean simulate);

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!world.isRemote) {
            if (isSleeping()) {
                this.sleep--;
                this.markDirty();
            } else if (canWork()) {
                IRecipe<I, O, P> recipe = getCurrentRecipe();
                if (recipe != null) {
                    if (progress == 0 && !finalizeRecipe(recipe, true)) {
                        sleep = SLEEP_TIME;
                    } else if (progress < getMaxProgress()) {
                        // // Consume energy while progressing
                        int toDrain = getEnergyConsumptionRate();
                        if (drainEnergy(toDrain, true) == toDrain) {
                            drainEnergy(toDrain, false);
                            progress++;
                            sleep = -1;
                            sendUpdate();
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
                    this.progress = 0;
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
            IEnergyNetwork energyNetwork = getEnergyNetwork();
            if (energyNetwork != null) {
                return energyNetwork.extractEnergy(toDrain, simulate);
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
            sendUpdate();
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int stored = getEnergyStored();
        int newEnergy = Math.min(stored + maxReceive, getMaxEnergyStored());
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return newEnergy - stored;
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
