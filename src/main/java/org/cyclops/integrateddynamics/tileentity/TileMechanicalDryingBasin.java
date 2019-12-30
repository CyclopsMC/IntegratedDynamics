package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasinConfig;
import org.cyclops.integrateddynamics.core.recipe.custom.RecipeHandlerDryingBasin;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the mechanical drying basin.
 * @author rubensworks
 */
public class TileMechanicalDryingBasin extends TileMechanicalMachine<Pair<ItemStack, FluidStack>, BlockMechanicalDryingBasin,
        IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>
        implements INamedContainerProvider {

    public static final int INVENTORY_SIZE = 5;

    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};

    private final SingleUseTank tankIn = new SingleUseTank(FluidHelpers.BUCKET_VOLUME * 10);
    private final SingleUseTank tankOut = new SingleUseTank(FluidHelpers.BUCKET_VOLUME * 100);

    public TileMechanicalDryingBasin() {
        super(RegistryEntries.TILE_ENTITY_MECHANICAL_DRYING_BASIN, INVENTORY_SIZE);

        // Add fluid tank capability
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN, LazyOptional.of(() -> tankOut));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.NORTH, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.SOUTH, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.WEST, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.EAST, LazyOptional.of(() -> tankIn));

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerDryingBasin<>(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN)));

        // Add tank update listeners
        tankIn.addDirtyMarkListener(this::onTankChanged);
        tankOut.addDirtyMarkListener(this::onTankChanged);
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
                    } else if(key.getRight().getAmount() >= recipe.getInput().getFluidStack().getAmount()
                            && (maxRecipe == null
                            || recipe.getInput().getFluidStack().getAmount() > maxRecipe.getInput().getFluidStack().getAmount())) {
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
        return getWorld().getBlockState(getPos()).get(BlockMechanicalDryingBasin.LIT);
    }

    @Override
    public void setWorking(boolean working) {
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos())
                .with(BlockMechanicalDryingBasin.LIT, working));
    }

    public SingleUseTank getTankInput() {
        return tankIn;
    }

    public SingleUseTank getTankOutput() {
        return tankOut;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        getTankInput().readFromNBT(tag.getCompound("tankIn"));
        getTankOutput().readFromNBT(tag.getCompound("tankOut"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("tankIn", getTankInput().writeToNBT(new CompoundNBT()));
        tag.put("tankOut", getTankOutput().writeToNBT(new CompoundNBT()));
        return super.write(tag);
    }

    @Override
    protected IRecipeRegistry<BlockMechanicalDryingBasin, IngredientAndFluidStackRecipeComponent,
            IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.getRecipeRegistry();
    }

    @Override
    protected Pair<ItemStack, FluidStack> getCurrentRecipeCacheKey() {
        return Pair.of(getInventory().getStackInSlot(SLOT_INPUT).copy(), FluidHelpers.copy(getTankInput().getFluid()));
    }

    @Override
    public int getRecipeDuration(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return recipe.getProperties().getDuration();
    }

    @Override
    protected boolean finalizeRecipe(IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe, boolean simulate) {
        IFluidHandler.FluidAction fluidAction = FluidHelpers.simulateBooleanToAction(simulate);

        // Output items
        ItemStack outputStack = recipe.getOutput().getFirstItemStack().copy();
        if (!outputStack.isEmpty()) {
            if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, NonNullList.withSize(1, outputStack), simulate).isEmpty()) {
                return false;
            }
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutput().getFluidStack();
        if (outputFluid != null) {
            if (getTankOutput().fill(outputFluid.copy(), fluidAction) != outputFluid.getAmount()) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            if (!recipe.getInput().getFirstItemStack().isEmpty()) {
                getInventory().decrStackSize(SLOT_INPUT, 1);
            }
        }

        // Consume fluid
        FluidStack inputFluid = recipe.getInput().getFluidStack();
        if (inputFluid != null) {
            if (FluidHelpers.getAmount(getTankInput().drain(inputFluid, fluidAction)) != inputFluid.getAmount()) {
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

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerMechanicalDryingBasin(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.integrateddynamics.mechanical_drying_basin");
    }
}
