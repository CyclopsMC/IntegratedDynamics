package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventoryState;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.cyclopscore.recipe.type.InventoryFluid;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

import java.util.Optional;

/**
 * A part entity for drying stuff.
 * @author rubensworks
 */
public class BlockEntityDryingBasin extends CyclopsBlockEntity {

    private static final int WOOD_IGNITION_TEMPERATURE = 573; // 300 degrees celcius

    private final SimpleInventory inventory;
    private final SingleUseTank tank;

    @NBTPersist
    private Float randomRotation = 0F;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int fire = 0;

    private SingleCache<Pair<ItemStack, FluidStack>, Optional<RecipeDryingBasin>> recipeCache;

    public BlockEntityDryingBasin(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_DRYING_BASIN, blockPos, blockState);

        // Create inventory and tank
        this.inventory = new SimpleInventory(1, 1) {
            @Override
            public boolean canPlaceItem(int i, ItemStack itemstack) {
                return getItem(0).isEmpty();
            }

            @Override
            public void setItem(int slotId, ItemStack itemstack) {
                super.setItem(slotId, itemstack);
                BlockEntityDryingBasin.this.randomRotation = level.random.nextFloat() * 360;
                sendUpdate();
            }
        };
        this.tank = new SingleUseTank(FluidHelpers.BUCKET_VOLUME);
        addCapabilityInternal(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, LazyOptional.of(this.getInventory()::getItemHandler));
        addCapabilityInternal(Capabilities.INVENTORY_STATE, LazyOptional.of(() -> new SimpleInventoryState(getInventory())));
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(this::getTank));

        // Add dirty mark listeners to inventory and tank
        this.inventory.addDirtyMarkListener(this::sendUpdate);
        this.tank.addDirtyMarkListener(this.inventory::setChanged);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeDryingBasin>>() {
            @Override
            public Optional<RecipeDryingBasin> getNewValue(Pair<ItemStack, FluidStack> key) {
                IInventoryFluid recipeInput = new InventoryFluid(
                        NonNullList.of(ItemStack.EMPTY, key.getLeft()),
                        NonNullList.of(FluidStack.EMPTY, key.getRight()));
                return CraftingHelpers.findServerRecipe(getRegistry(), recipeInput, getLevel());
            }

            @Override
            public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                return cacheKey == null || newKey == null ||
                        (ItemStack.matches(cacheKey.getLeft(), newKey.getLeft()) &&
                                FluidStack.areFluidStackTagsEqual(cacheKey.getRight(), newKey.getRight())) &&
                                FluidHelpers.getAmount(cacheKey.getRight()) == FluidHelpers.getAmount(newKey.getRight());
            }
        });

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerDryingBasin(this::getLevel)));
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getFire() {
        return fire;
    }

    public void setFire(int fire) {
        this.fire = fire;
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

    protected RecipeType<RecipeDryingBasin> getRegistry() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN;
    }

    public Optional<RecipeDryingBasin> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getInventory().getItem(0).copy(), FluidHelpers.copy(getTank().getFluid())));
    }

    /**
     * Get the random rotation for displaying the item.
     * @return The random rotation.
     */
    public float getRandomRotation() {
        return randomRotation;
    }

    public static class TickerServer extends BlockEntityTickerDelayed<BlockEntityDryingBasin> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityDryingBasin blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            Optional<RecipeDryingBasin> currentRecipe = blockEntity.getCurrentRecipe();
            if (!blockEntity.getTank().isEmpty() && blockEntity.getTank().getFluid().getFluid().getAttributes().getTemperature(blockEntity.getTank().getFluid()) >= WOOD_IGNITION_TEMPERATURE) {
                blockEntity.setFire(blockEntity.getFire() + 1);
                if (blockEntity.getFire() >= 100) {
                    level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                } else if (level.isEmptyBlock(pos.relative(Direction.UP)) && level.random.nextInt(10) == 0) {
                    level.setBlockAndUpdate(pos.relative(Direction.UP), Blocks.FIRE.defaultBlockState());
                }

            } else if (currentRecipe.isPresent()) {
                RecipeDryingBasin recipe = currentRecipe.get();
                if (blockEntity.getProgress() >= recipe.getDuration()) {
                    // Consume input fluid
                    int amount = FluidHelpers.getAmount(recipe.getInputFluid());
                    blockEntity.getTank().drain(amount, IFluidHandler.FluidAction.EXECUTE);

                    // Produce output item
                    ItemStack output = recipe.getOutputItem();
                    if (!output.isEmpty()) {
                        output = output.copy();
                        blockEntity.getInventory().setItem(0, output);
                    } else {
                        blockEntity.getInventory().setItem(0, ItemStack.EMPTY);
                    }

                    // Produce output fluid
                    if (!recipe.getOutputFluid().isEmpty()) {
                        if (blockEntity.getTank().fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE) == 0) {
                            IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR, "Encountered an invalid recipe: " + recipe.getId());
                        }
                    }

                    blockEntity.setProgress(0);
                } else {
                    blockEntity.setProgress(blockEntity.getProgress() + 1);
                    blockEntity.setChanged();
                }
                blockEntity.setFire(0);
            } else {
                if ((blockEntity.getProgress() > 0) || (blockEntity.getFire() > 0)) {
                    blockEntity.setProgress(0);
                    blockEntity.setFire(0);
                    blockEntity.setChanged();
                }
            }
        }
    }

    public static class TickerClient implements BlockEntityTicker<BlockEntityDryingBasin> {
        @Override
        public void tick(Level level, BlockPos pos, BlockState blockState, BlockEntityDryingBasin blockEntity) {
            if(blockEntity.getProgress() > 0 && level.random.nextInt(5) == 0) {
                if(!blockEntity.getTank().isEmpty()) {
                    BlockState blockStateFluid = blockEntity.getTank().getFluid().getFluid().getAttributes().getBlock(level, pos,
                            blockEntity.getTank().getFluid().getFluid().defaultFluidState());
                    if(blockStateFluid != null) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, blockStateFluid),
                                pos.getX() + Math.random() * 0.8D + 0.1D, pos.getY() + Math.random() * 0.1D + 0.9D,
                                pos.getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0);
                    }
                }
                if(!blockEntity.getInventory().getItem(0).isEmpty()) {
                    ItemStack itemStack = blockEntity.getInventory().getItem(0);
                    level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                            pos.getX() + Math.random() * 0.8D + 0.1D, pos.getY() + Math.random() * 0.1D + 0.9D,
                            pos.getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0);
                }
            }
        }
    }
}
