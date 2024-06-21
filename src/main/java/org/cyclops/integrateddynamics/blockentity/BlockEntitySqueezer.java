package org.cyclops.integrateddynamics.blockentity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.capability.registrar.BlockEntityCapabilityRegistrar;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventoryState;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A part entity for squeezing stuff.
 * @author rubensworks
 */
public class BlockEntitySqueezer extends CyclopsBlockEntity {

    private final SimpleInventory inventory;
    private final SingleUseTank tank;

    @NBTPersist
    @Getter
    private int itemHeight = 1;

    private SingleCache<ItemStack, Optional<RecipeHolder<RecipeSqueezer>>> recipeCache;

    public BlockEntitySqueezer(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_SQUEEZER.get(), blockPos, blockState);

        // Create inventory and tank
        this.inventory = new SimpleInventory(1, 1) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack itemStack) {
                return getLevel().getBlockState(getBlockPos()).getValue(BlockSqueezer.HEIGHT) == 1
                        && getItem(0).isEmpty() && super.canPlaceItem(slot, itemStack);
            }

            @Override
            public void setItem(int slotId, ItemStack itemstack) {
                super.setItem(slotId, itemstack);
                itemHeight = 1;
                sendUpdate();
            }
        };
        this.tank = new SingleUseTank(FluidHelpers.BUCKET_VOLUME);

        // Add dirty mark listeners to inventory and tank
        this.inventory.addDirtyMarkListener(this::sendUpdate);
        this.tank.addDirtyMarkListener(this.inventory::setChanged);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<ItemStack, Optional<RecipeHolder<RecipeSqueezer>>>() {
                    @Override
                    public Optional<RecipeHolder<RecipeSqueezer>> getNewValue(ItemStack key) {
                        Container recipeInput = new SimpleContainer(key);
                        return CraftingHelpers.findServerRecipe(getRegistry(), recipeInput, getLevel());
                    }

                    @Override
                    public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                        return ItemStack.matches(cacheKey, newKey);
                    }
                });
    }

    public static class CapabilityRegistrar extends BlockEntityCapabilityRegistrar<BlockEntitySqueezer> {
        public CapabilityRegistrar(Supplier<BlockEntityType<? extends BlockEntitySqueezer>> blockEntityType) {
            super(blockEntityType);
        }

        @Override
        public void populate() {
            add(
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    (blockEntity, direction) -> blockEntity.getInventory().getItemHandler()
            );
            add(
                    org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK,
                    (blockEntity, direction) -> new SimpleInventoryState(blockEntity.getInventory())
            );
            add(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (blockEntity, direction) -> blockEntity.getTank()
            );
            add(
                    org.cyclops.commoncapabilities.api.capability.Capabilities.RecipeHandler.BLOCK,
                    (blockEntity, direction) -> new RecipeHandlerSqueezer<>(blockEntity::getLevel, RegistryEntries.RECIPETYPE_SQUEEZER.get())
            );
        }
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    public SingleUseTank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundTag tag) {
        inventory.readFromNBT(tag, "inventory");
        tank.readFromNBT(tag, "tank");
        super.read(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        inventory.writeToNBT(tag, "inventory");
        tank.writeToNBT(tag, "tank");
        super.saveAdditional(tag);
    }

    protected RecipeType<RecipeSqueezer> getRegistry() {
        return RegistryEntries.RECIPETYPE_SQUEEZER.get();
    }

    public Optional<RecipeHolder<RecipeSqueezer>> getCurrentRecipe() {
        return recipeCache.get(getInventory().getItem(0).copy());
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
        getInventory().setChanged();
    }

    public static class Ticker extends BlockEntityTickerDelayed<BlockEntitySqueezer> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntitySqueezer blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if(!blockEntity.getTank().isEmpty()) {
                Direction.Axis axis = blockState.getValue(BlockSqueezer.AXIS);
                Arrays.stream(Direction.AxisDirection.values())
                        .map(axisDirection -> Direction.get(axisDirection, axis))
                        .forEach(side -> {
                            if (!blockEntity.getTank().isEmpty()) {
                                BlockEntityHelpers.getCapability(level, pos.relative(side), side.getOpposite(), net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK)
                                        .ifPresent(handler -> {
                                            FluidStack fluidStack = new FluidStack(blockEntity.getTank().getFluid(),
                                                    Math.min(100, blockEntity.getTank().getFluidAmount()));
                                            if (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                                                int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                                blockEntity.getTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                            }
                                        });
                            }
                        });
            } else {
                if (blockEntity.itemHeight == 7) {
                    Optional<RecipeHolder<RecipeSqueezer>> recipeOptional = blockEntity.getCurrentRecipe();
                    if (recipeOptional.isPresent()) {
                        RecipeSqueezer recipe = recipeOptional.get().value();
                        blockEntity.getInventory().setItem(0, ItemStack.EMPTY);
                        for (RecipeSqueezer.IngredientChance itemStackChance : recipe.getOutputItems()) {
                            if (itemStackChance.getChance() == 1.0F || itemStackChance.getChance() >= level.random.nextFloat()) {
                                ItemStack resultStack = itemStackChance.getIngredientFirst().copy();
                                for (Direction side : Direction.values()) {
                                    if (!resultStack.isEmpty() && side != Direction.UP) {
                                        IItemHandler itemHandler = BlockEntityHelpers.getCapability(level, pos.relative(side), side.getOpposite(), net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK).orElse(null);
                                        if (itemHandler != null) {
                                            resultStack = ItemHandlerHelper.insertItem(itemHandler, resultStack, false);
                                        }
                                    }
                                }
                                if (!resultStack.isEmpty()) {
                                    ItemStackHelpers.spawnItemStack(level, pos, resultStack);
                                }
                            }
                        }
                        if (recipe.getOutputFluid().isPresent()) {
                            blockEntity.getTank().fill(recipe.getOutputFluid().get(), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }
}
