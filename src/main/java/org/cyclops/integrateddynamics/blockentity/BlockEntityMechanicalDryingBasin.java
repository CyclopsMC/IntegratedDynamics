package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
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

    private final SingleUseTank tankIn = new SingleUseTank(IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume() * 10);
    private final SingleUseTank tankOut = new SingleUseTank(IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume() * 100);

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
                    net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,
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
                // First, try matching with both item and fluid inputs
                IInventoryFluid recipeInput = new InventoryFluid(
                        NonNullList.of(ItemStack.EMPTY, key.getLeft()),
                        NonNullList.of(FluidStack.EMPTY, key.getRight()));
                Optional<RecipeHolder<RecipeMechanicalDryingBasin>> recipe = IModHelpers.get().getCraftingHelpers().findRecipe(getRecipeRegistry(), recipeInput, getLevel());
                if (recipe.isPresent()) {
                    return recipe;
                }

                // If both item and fluid are present but no combined recipe was found,
                // try item-only, then fluid-only, to handle the case where the machine has
                // two separate types of inputs and should process one at a time.
                if (!key.getLeft().isEmpty() && !key.getRight().isEmpty()) {
                    recipeInput = new InventoryFluid(
                            NonNullList.of(ItemStack.EMPTY, key.getLeft()),
                            NonNullList.of(FluidStack.EMPTY, FluidStack.EMPTY));
                    recipe = IModHelpers.get().getCraftingHelpers().findRecipe(getRecipeRegistry(), recipeInput, getLevel());
                    if (recipe.isPresent()) {
                        return recipe;
                    }

                    recipeInput = new InventoryFluid(
                            NonNullList.of(ItemStack.EMPTY, ItemStack.EMPTY),
                            NonNullList.of(FluidStack.EMPTY, key.getRight()));
                    return IModHelpers.get().getCraftingHelpers().findRecipe(getRecipeRegistry(), recipeInput, getLevel());
                }

                return Optional.empty();
            }

            @Override
            public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                return cacheKey == null || newKey == null ||
                        (ItemStack.matches(cacheKey.getLeft(), newKey.getLeft()) &&
                                FluidStack.matches(cacheKey.getRight(), newKey.getRight()));
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
    public void read(ValueInput input) {
        super.read(input);
        getTankInput().deserialize(input, "tankIn");
        getTankOutput().deserialize(input, "tankOut");
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        getTankInput().serialize(output, "tankIn");
        getTankOutput().serialize(output, "tankOut");
        super.saveAdditional(output);
    }

    @Override
    protected RecipeType<RecipeMechanicalDryingBasin> getRecipeRegistry() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    protected Pair<ItemStack, FluidStack> getCurrentRecipeCacheKey() {
        return Pair.of(getInventory().getItem(SLOT_INPUT).copy(), IModHelpersNeoForge.get().getFluidHelpers().copy(getTankInput().getFluid()));
    }

    @Override
    public int getRecipeDuration(RecipeHolder<RecipeMechanicalDryingBasin> recipe) {
        return recipe.value().getDuration();
    }

    @Override
    protected boolean finalizeRecipe(RecipeMechanicalDryingBasin recipe, boolean simulate) {
        // Output items
        ItemStack outputStack = recipe.getOutputItemFirst().orElse(ItemStack.EMPTY).copy();
        if (!outputStack.isEmpty()) {
            if (!IModHelpers.get().getInventoryHelpers().addToInventory(getInventory(), SLOTS_OUTPUT, NonNullList.withSize(1, outputStack), simulate).isEmpty()) {
                return false;
            }
        }

        // Output fluid
        Optional<FluidStack> outputFluid = recipe.getOutputFluid();
        if (outputFluid.isPresent()) {
            try (var tx = Transaction.openRoot()) {
                int inserted = getTankOutput().insert(FluidResource.of(outputFluid.get()), outputFluid.get().getAmount(), tx);
                if (!simulate) {
                    tx.commit();
                }
                if (inserted != outputFluid.get().getAmount()) {
                    return false;
                }
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
            try (var tx = Transaction.openRoot()) {
                int extracted = getTankInput().extract(FluidResource.of(inputFluid.get()), inputFluid.get().getAmount(), tx);
                if (!simulate) {
                    tx.commit();
                }
                if (extracted != inputFluid.get().getAmount()) {
                    return false;
                }
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
