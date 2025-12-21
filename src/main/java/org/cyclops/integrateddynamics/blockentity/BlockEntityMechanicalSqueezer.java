package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezerConfig;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the mechanical squeezer.
 * @author rubensworks
 */
public class BlockEntityMechanicalSqueezer extends BlockEntityMechanicalMachine<ItemStack, RecipeMechanicalSqueezer>
        implements MenuProvider {

    public static final int INVENTORY_SIZE = 5;

    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};
    private static final int TANK_SIZE = FluidHelpers.BUCKET_VOLUME * 100;

    @NBTPersist
    private boolean autoEjectFluids = false;

    private final SingleUseTank tank = new SingleUseTank(TANK_SIZE);

    public BlockEntityMechanicalSqueezer(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_MECHANICAL_SQUEEZER, blockPos, blockState, INVENTORY_SIZE);

        // Add fluid tank capability
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(() -> this.tank));

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerSqueezer<>(this::getLevel, RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER)));

        // Add tank update listeners
        tank.addDirtyMarkListener(this::onTankChanged);
    }

    @Override
    protected SingleCache.ICacheUpdater<ItemStack, Optional<RecipeMechanicalSqueezer>> createCacheUpdater() {
        return new SingleCache.ICacheUpdater<ItemStack, Optional<RecipeMechanicalSqueezer>>() {
            @Override
            public Optional<RecipeMechanicalSqueezer> getNewValue(ItemStack key) {
                Container recipeInput = new SimpleContainer(key);
                return CraftingHelpers.findServerRecipe(getRecipeRegistry(), recipeInput, getLevel());
            }

            @Override
            public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                return ItemStack.matches(cacheKey, newKey);
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
        return getLevel().getBlockState(getBlockPos()).getValue(BlockMechanicalSqueezer.LIT);
    }

    @Override
    public void setWorking(boolean working) {
        getLevel().setBlockAndUpdate(getBlockPos(), getLevel().getBlockState(getBlockPos())
                .setValue(BlockMechanicalSqueezer.LIT, working));
    }

    public SingleUseTank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        getTank().readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("tank", getTank().writeToNBT(new CompoundTag()));
        super.saveAdditional(tag);
    }

    @Override
    protected RecipeType<RecipeMechanicalSqueezer> getRecipeRegistry() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER;
    }

    @Override
    protected ItemStack getCurrentRecipeCacheKey() {
        return getInventory().getItem(SLOT_INPUT).copy();
    }

    @Override
    public int getRecipeDuration(RecipeMechanicalSqueezer recipe) {
        return recipe.getDuration();
    }

    @Override
    protected boolean finalizeRecipe(RecipeMechanicalSqueezer recipe, boolean simulate) {
        // Output items
        NonNullList<ItemStack> outputStacks = NonNullList.create();
        for (RecipeSqueezer.IngredientChance itemStackChance : recipe.assemble(getInventory().getItem(SLOT_INPUT))) {
            ItemStack outputStack = itemStackChance.getIngredientFirst().copy();
            if (!outputStack.isEmpty() && (simulate || itemStackChance.getChance() == 1.0F
                    || itemStackChance.getChance() >= getLevel().random.nextFloat())) {
                InventoryHelpers.addStackToList(outputStacks, outputStack);
            }
        }
        if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, outputStacks, simulate).isEmpty()) {
            return false;
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutputFluid();
        if (outputFluid != null) {
            if (getTank().fill(outputFluid.copy(), FluidHelpers.simulateBooleanToAction(simulate)) != outputFluid.getAmount()) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            getInventory().removeItem(SLOT_INPUT, 1);
        }

        return true;
    }

    @Override
    public int getEnergyConsumptionRate() {
        return BlockMechanicalSqueezerConfig.consumptionRate;
    }

    @Override
    public int getMaxEnergyStored() {
        return BlockMechanicalSqueezerConfig.capacity;
    }

    public boolean isAutoEjectFluids() {
        return autoEjectFluids;
    }

    public void setAutoEjectFluids(boolean autoEjectFluids) {
        this.autoEjectFluids = autoEjectFluids;
        sendUpdate();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerMechanicalSqueezer(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integrateddynamics.mechanical_squeezer");
    }

    public static class Ticker extends BlockEntityMechanicalMachine.Ticker<ItemStack, RecipeMechanicalSqueezer, BlockEntityMechanicalSqueezer> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityMechanicalSqueezer blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            // Auto-eject fluid
            if (blockEntity.isAutoEjectFluids() && !blockEntity.getTank().isEmpty()) {
                for (Direction side : Direction.values()) {
                    IFluidHandler handler = BlockEntityHelpers.getCapability(level, pos.relative(side),
                            side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
                    if(handler != null) {
                        FluidStack fluidStack = blockEntity.getTank().getFluid().copy();
                        fluidStack.setAmount(Math.min(BlockMechanicalSqueezerConfig.autoEjectFluidRate, fluidStack.getAmount()));
                        if (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                            blockEntity.getTank().drain(handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                            break;
                        }
                    }
                }
            }
        }
    }
}
