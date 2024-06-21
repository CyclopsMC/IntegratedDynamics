package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.cyclopscore.recipe.type.InventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasinConfig;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

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
        super(RegistryEntries.BLOCK_ENTITY_MECHANICAL_DRYING_BASIN.get(), blockPos, blockState, INVENTORY_SIZE);

        // Add tank update listeners
        tankIn.addDirtyMarkListener(this::onTankChanged);
        tankOut.addDirtyMarkListener(this::onTankChanged);
    }

    public static class CapabilityRegistrar extends BlockEntityMechanicalMachine.CapabilityRegistrar<BlockEntityMechanicalDryingBasin> {
        public CapabilityRegistrar(Supplier<BlockEntityType<? extends BlockEntityMechanicalDryingBasin>> blockEntityType) {
            super(blockEntityType);
        }

        @Override
        public void populate() {
            super.populate();

            add(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (blockEntity, direction) -> direction == Direction.DOWN ? blockEntity.getTankOutput() : blockEntity.getTankInput()
            );
            add(
                    org.cyclops.commoncapabilities.api.capability.Capabilities.RecipeHandler.BLOCK,
                    (blockEntity, direction) -> new RecipeHandlerDryingBasin<>(blockEntity::getLevel, RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get())
            );
        }
    }

    @Override
    protected SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeHolder<RecipeMechanicalDryingBasin>>> createCacheUpdater() {
        return new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeHolder<RecipeMechanicalDryingBasin>>>() {
            @Override
            public Optional<RecipeHolder<RecipeMechanicalDryingBasin>> getNewValue(Pair<ItemStack, FluidStack> key) {
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
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    protected Pair<ItemStack, FluidStack> getCurrentRecipeCacheKey() {
        return Pair.of(getInventory().getItem(SLOT_INPUT).copy(), FluidHelpers.copy(getTankInput().getFluid()));
    }

    @Override
    public int getRecipeDuration(RecipeHolder<RecipeMechanicalDryingBasin> recipe) {
        return recipe.value().getDuration();
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
        Optional<FluidStack> outputFluid = recipe.getOutputFluid();
        if (outputFluid.isPresent()) {
            if (getTankOutput().fill(outputFluid.get().copy(), fluidAction) != outputFluid.get().getAmount()) {
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
        Optional<FluidStack> inputFluid = recipe.getInputFluid();
        if (inputFluid.isPresent()) {
            if (FluidHelpers.getAmount(getTankInput().drain(inputFluid.get(), fluidAction)) != inputFluid.get().getAmount()) {
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
        return Component.translatable("block.integrateddynamics.mechanical_drying_basin");
    }
}
