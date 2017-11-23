package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezerConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableTankInventory;
import org.cyclops.integrateddynamics.network.MechanicalSqueezerNetworkElement;

import java.util.Set;

/**
 * A part entity for the mechanical squeezer.
 * @author rubensworks
 */
public class TileMechanicalSqueezer extends TileCableConnectableTankInventory implements IEnergyStorage {

    public static final int SLOTS = 5;
    public static final int SLOT_INPUT = 0;
    public static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};
    public static final int TANK_SIZE = Fluid.BUCKET_VOLUME * 100;

    @NBTPersist
    private int energy;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int sleep = -1;
    @NBTPersist
    private boolean autoEjectFluids = false;

    private SingleCache<ItemStack,
            IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>> recipeCache;

    public TileMechanicalSqueezer() {
        super(SLOTS, "mechanicalSqueezerSlots", 64, TANK_SIZE, "mechanicalSqueezerTank");
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new MechanicalSqueezerNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(CapabilityEnergy.ENERGY, this);

        Set<Integer> in = Sets.newHashSet(SLOT_INPUT);
        Set<Integer> out = Sets.newHashSet(1, 2, 3, 4);
        addSlotsToSide(EnumFacing.UP, in);
        addSlotsToSide(EnumFacing.DOWN, out);
        addSlotsToSide(EnumFacing.NORTH, in);
        addSlotsToSide(EnumFacing.SOUTH, out);
        addSlotsToSide(EnumFacing.WEST, in);
        addSlotsToSide(EnumFacing.EAST, out);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<ItemStack,
                        IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
                    @Override
                    public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getNewValue(ItemStack key) {
                        IngredientRecipeComponent recipeInput = new IngredientRecipeComponent(key);
                        return getRegistry().findRecipeByInput(recipeInput);
                    }

                    @Override
                    public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                        return ItemStack.areItemStacksEqual(cacheKey, newKey);
                    }
                });
    }

    public IEnergyNetwork getEnergyNetwork() {
        return NetworkHelpers.getEnergyNetwork(getNetwork());
    }

    public void updateBlockState() {
        boolean wasWorking = getWorld().getBlockState(getPos()).getValue(BlockMechanicalSqueezer.ON);
        boolean isWorking = isWorking();
        if (isWorking != wasWorking) {
            getWorld().setBlockState(getPos(),
                    getWorld().getBlockState(getPos()).withProperty(BlockMechanicalSqueezer.ON, isWorking));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_INPUT) {
            // Only allow items to be inserted that are used in at least once recipe.
            return getRegistry().findRecipeByInput(new IngredientRecipeComponent(stack)) != null;
        }
        return super.isItemValidForSlot(index, stack);
    }

    public boolean isWorking() {
        return this.progress > 0 && this.sleep == -1;
    }

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

    public int extractEnergyInternal(int energy, boolean simulate) {
        energy = Math.max(0, energy);
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

    protected IRecipeRegistry<BlockMechanicalSqueezer, IngredientRecipeComponent,
                IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getRegistry() {
        return BlockMechanicalSqueezer.getInstance().getRecipeRegistry();
    }

    public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getCurrentRecipe() {
        return recipeCache.get(getStackInSlot(SLOT_INPUT).copy());
    }

    protected boolean finalizeRecipe(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> recipe, boolean simulate) {
        // Output items
        NonNullList<ItemStack> outputStacks = NonNullList.create();
        for (IngredientRecipeComponent recipeComponent : recipe.getOutput().getSubIngredientComponents()) {
            ItemStack outputStack = recipeComponent.getFirstItemStack().copy();
            if (!outputStack.isEmpty() && (simulate || recipeComponent.getChance() == 1.0F
                    || recipeComponent.getChance() >= getWorld().rand.nextFloat())) {
                // Try to add the stack to one of the already-present stacks before adding a new element
                boolean added = false;
                ItemStack toAdd = outputStack;
                for (ItemStack existingOutputStack: outputStacks) {
                    toAdd = InventoryHelpers.addToStack(existingOutputStack, toAdd);
                    if (toAdd.isEmpty()) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    outputStacks.add(toAdd);
                }
            }
        }
        if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, outputStacks, simulate).isEmpty()) {
            return false;
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutput().getFluidStack();
        if (outputFluid != null) {
            if (fill(outputFluid.copy(), !simulate) != outputFluid.amount) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            this.decrStackSize(SLOT_INPUT, 1);
        }

        return true;
    }

    @Override
    protected void onInventoryChanged() {
        super.onInventoryChanged();
        this.sleep = 0;
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!world.isRemote) {
            if (this.sleep > 0) {
                this.sleep--;
                this.markDirty();
            } else if (!world.isBlockPowered(getPos())) {
                IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = getCurrentRecipe();
                if (recipe != null) {
                    if (progress == 0 && !finalizeRecipe(recipe, true)) {
                        sleep = 40;
                    } else if (progress < recipe.getProperties().getDuration()) {
                        // // Consume energy while progressing
                        int toDrain = BlockMechanicalSqueezerConfig.consumptionRate;
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

                        // First check if we have enough room in the output slots and tank for the recipe output,
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

            // Check if a block update is needed.
            updateBlockState();

            // Auto-eject fluid
            if (isAutoEjectFluids() && !getTank().isEmpty()) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    IFluidHandler handler = TileHelpers.getCapability(world, getPos().offset(side),
                            side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(handler != null) {
                        FluidStack fluidStack = new FluidStack(getTank().getFluid(),
                                Math.min(BlockMechanicalSqueezerConfig.autoEjectFluidRate, getTank().getFluidAmount()));
                        if (handler.fill(fluidStack, false) > 0) {
                            drain(handler.fill(fluidStack, true), true);
                            break;
                        }
                    }
                }
            }
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return this.getCurrentRecipe() != null ? this.getCurrentRecipe().getProperties().getDuration() : 0;
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
    public int getMaxEnergyStored() {
        return BlockMechanicalSqueezerConfig.capacity;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public boolean isAutoEjectFluids() {
        return autoEjectFluids;
    }

    public void setAutoEjectFluids(boolean autoEjectFluids) {
        this.autoEjectFluids = autoEjectFluids;
        sendUpdate();
    }
}
