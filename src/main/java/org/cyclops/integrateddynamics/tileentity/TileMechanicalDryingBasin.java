package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasinConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

/**
 * A part entity for the mechanical drying basin.
 * @author rubensworks
 */
public class TileMechanicalDryingBasin extends TileMechanicalMachine<Pair<ItemStack, FluidStack>, BlockMechanicalDryingBasin,
        IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> {

    private static final int SLOTS = 5;
    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};

    private final SingleUseTank tankIn = new SingleUseTank(Fluid.BUCKET_VOLUME * 10, this);
    private final SingleUseTank tankOut = new SingleUseTank(Fluid.BUCKET_VOLUME * 100, this);

    public TileMechanicalDryingBasin() {
        super(SLOTS);

        // Add fluid tank capability
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP, tankIn);
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN, tankOut);
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.NORTH, tankIn);
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.SOUTH, tankIn);
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.WEST, tankIn);
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.EAST, tankIn);
    }

    @Override
    protected SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, IRecipe<IngredientAndFluidStackRecipeComponent,
            IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>> createCacheUpdater() {
        return new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>,
                IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
            @Override
            public IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getNewValue(Pair<ItemStack, FluidStack> key) {
                IngredientAndFluidStackRecipeComponent recipeInput =
                        new IngredientAndFluidStackRecipeComponent(key.getLeft(), key.getRight());
                IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> maxRecipe = null;
                for (IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe : getRecipeRegistry().findRecipesByInput(recipeInput)) {
                    if(key.getRight() == null) {
                        return recipe;
                    } else if(key.getRight().amount >= recipe.getInput().getFluidStack().amount
                            && (maxRecipe == null
                            || recipe.getInput().getFluidStack().amount > maxRecipe.getInput().getFluidStack().amount)) {
                        maxRecipe = recipe;
                    }
                }
                return maxRecipe;
            }

            @Override
            public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                return cacheKey == null || newKey == null ||
                        (ItemStack.areItemStacksEqual(cacheKey.getLeft(), newKey.getLeft()) &&
                                FluidStack.areFluidStackTagsEqual(cacheKey.getRight(), newKey.getRight())) &&
                                FluidHelpers.getAmount(cacheKey.getRight()) == FluidHelpers.getAmount(newKey.getRight());
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{SLOT_INPUT};
    }

    @Override
    public int[] getOutputSlots() {
        return SLOTS_OUTPUT;
    }

    @Override
    public boolean wasWorking() {
        return getWorld().getBlockState(getPos()).getValue(BlockMechanicalDryingBasin.ON);
    }

    @Override
    public void setWorking(boolean working) {
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos())
                .withProperty(BlockMechanicalDryingBasin.ON, working));
    }

    public SingleUseTank getTankInput() {
        return tankIn;
    }

    public SingleUseTank getTankOutput() {
        return tankOut;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        getTankInput().readFromNBT(tag.getCompoundTag("tankIn"));
        getTankOutput().readFromNBT(tag.getCompoundTag("tankOut"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("tankIn", getTankInput().writeToNBT(new NBTTagCompound()));
        tag.setTag("tankOut", getTankOutput().writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(tag);
    }

    @Override
    protected IRecipeRegistry<BlockMechanicalDryingBasin, IngredientAndFluidStackRecipeComponent,
            IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return BlockMechanicalDryingBasin.getInstance().getRecipeRegistry();
    }

    @Override
    protected Pair<ItemStack, FluidStack> getCurrentRecipeCacheKey() {
        return Pair.of(getStackInSlot(SLOT_INPUT).copy(), FluidHelpers.copy(getTankInput().getFluid()));
    }

    @Override
    public IngredientAndFluidStackRecipeComponent getRecipeInput(NonNullList<ItemStack> inputStacks) {
        return new IngredientAndFluidStackRecipeComponent(inputStacks.get(SLOT_INPUT), FluidHelpers.copy(getTankInput().getFluid()));
    }

    @Override
    public int getRecipeDuration(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return recipe.getProperties().getDuration();
    }

    @Override
    protected boolean finalizeRecipe(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe, boolean simulate) {
        // Output items
        ItemStack outputStack = recipe.getOutput().getFirstItemStack();
        if (!outputStack.isEmpty()) {
            if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, NonNullList.withSize(1, outputStack), simulate).isEmpty()) {
                return false;
            }
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutput().getFluidStack();
        if (outputFluid != null) {
            if (getTankOutput().fill(outputFluid.copy(), !simulate) != outputFluid.amount) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            if (!recipe.getInput().getFirstItemStack().isEmpty()) {
                this.decrStackSize(SLOT_INPUT, 1);
            }
        }

        // Consume fluid
        FluidStack inputFluid = recipe.getInput().getFluidStack();
        if (inputFluid != null) {
            if (FluidHelpers.getAmount(getTankInput().drain(inputFluid, !simulate)) != inputFluid.amount) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getEnergyConsumptionRate() {
        return BlockMechanicalDryingBasinConfig.consumptionRate;
    }

    @Override
    public int getMaxEnergyStored() {
        return BlockMechanicalDryingBasinConfig.capacity;
    }
}
