package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.cyclopscore.recipe.type.InventoryFluid;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasinConfig;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the mechanical drying basin.
 * @author rubensworks
 */
public class BlockEntityMechanicalDryingBasin extends BlockEntityMechanicalMachine<Pair<ItemStack, FluidStack>, RecipeMechanicalDryingBasin>
        implements MenuProvider {

    public static final int INVENTORY_SIZE = 5;

    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};

    private final SingleUseTank tankIn = new SingleUseTank(FluidHelpers.BUCKET_VOLUME * 10);
    private final SingleUseTank tankOut = new SingleUseTank(FluidHelpers.BUCKET_VOLUME * 100);

    public BlockEntityMechanicalDryingBasin(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_MECHANICAL_DRYING_BASIN, blockPos, blockState, INVENTORY_SIZE);

        // Add fluid tank capability
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN, LazyOptional.of(() -> tankOut));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.NORTH, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.SOUTH, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.WEST, LazyOptional.of(() -> tankIn));
        addCapabilitySided(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.EAST, LazyOptional.of(() -> tankIn));

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerDryingBasin(this::getLevel)));

        // Add tank update listeners
        tankIn.addDirtyMarkListener(this::onTankChanged);
        tankOut.addDirtyMarkListener(this::onTankChanged);
    }

    @Override
    protected SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeMechanicalDryingBasin>> createCacheUpdater() {
        return new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeMechanicalDryingBasin>>() {
            @Override
            public Optional<RecipeMechanicalDryingBasin> getNewValue(Pair<ItemStack, FluidStack> key) {
                IInventoryFluid recipeInput = new InventoryFluid(
                        NonNullList.of(ItemStack.EMPTY, key.getLeft()),
                        NonNullList.of(FluidStack.EMPTY, key.getRight()));
                return CraftingHelpers.findServerRecipe(getRecipeRegistry(), recipeInput, getLevel());
            }

            @Override
            public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                return cacheKey == null || newKey == null ||
                        (ItemStack.matches(cacheKey.getLeft(), newKey.getLeft()) &&
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
        return getLevel().getBlockState(getBlockPos()).getValue(BlockMechanicalDryingBasin.LIT);
    }

    @Override
    public void setWorking(boolean working) {
        getLevel().setBlockAndUpdate(getBlockPos(), getLevel().getBlockState(getBlockPos())
                .setValue(BlockMechanicalDryingBasin.LIT, working));
    }

    public SingleUseTank getTankInput() {
        return tankIn;
    }

    public SingleUseTank getTankOutput() {
        return tankOut;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        getTankInput().readFromNBT(tag.getCompound("tankIn"));
        getTankOutput().readFromNBT(tag.getCompound("tankOut"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("tankIn", getTankInput().writeToNBT(new CompoundTag()));
        tag.put("tankOut", getTankOutput().writeToNBT(new CompoundTag()));
        super.saveAdditional(tag);
    }

    @Override
    protected RecipeType<RecipeMechanicalDryingBasin> getRecipeRegistry() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN;
    }

    @Override
    protected Pair<ItemStack, FluidStack> getCurrentRecipeCacheKey() {
        return Pair.of(getInventory().getItem(SLOT_INPUT).copy(), FluidHelpers.copy(getTankInput().getFluid()));
    }

    @Override
    public int getRecipeDuration(RecipeMechanicalDryingBasin recipe) {
        return recipe.getDuration();
    }

    @Override
    protected boolean finalizeRecipe(RecipeMechanicalDryingBasin recipe, boolean simulate) {
        IFluidHandler.FluidAction fluidAction = FluidHelpers.simulateBooleanToAction(simulate);

        // Output items
        ItemStack outputStack = recipe.getOutputItemFirst().copy();
        if (!outputStack.isEmpty()) {
            if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, NonNullList.withSize(1, outputStack), simulate).isEmpty()) {
                return false;
            }
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutputFluid();
        if (outputFluid != null) {
            if (getTankOutput().fill(outputFluid.copy(), fluidAction) != outputFluid.getAmount()) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            if (!recipe.getInputIngredient().isEmpty()) {
                getInventory().removeItem(SLOT_INPUT, 1);
            }
        }

        // Consume fluid
        FluidStack inputFluid = recipe.getInputFluid();
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
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerMechanicalDryingBasin(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.integrateddynamics.mechanical_drying_basin");
    }
}
