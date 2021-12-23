package org.cyclops.integrateddynamics.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventoryState;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.core.recipe.handler.RecipeHandlerSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import java.util.Arrays;
import java.util.Optional;

/**
 * A part entity for squeezing stuff.
 * @author rubensworks
 */
public class TileSqueezer extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private final SimpleInventory inventory;
    private final SingleUseTank tank;

    @NBTPersist
    @Getter
    private int itemHeight = 1;

    private SingleCache<ItemStack, Optional<RecipeSqueezer>> recipeCache;

    public TileSqueezer() {
        super(RegistryEntries.TILE_ENTITY_SQUEEZER);

        // Create inventory and tank
        this.inventory = new SimpleInventory(1, 1) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack itemStack) {
                return getLevel().getBlockState(getBlockPos()).getValue(BlockSqueezer.HEIGHT) == 1
                        && getItem(0).isEmpty() && super.canPlaceItem(slot, itemStack);
            }

            @Override
            public void setItem(int slotId, ItemStack itemstack) {
                // super.setItem(slotId, itemstack); // TODO: restore
                itemHeight = 1;
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
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<ItemStack, Optional<RecipeSqueezer>>() {
                    @Override
                    public Optional<RecipeSqueezer> getNewValue(ItemStack key) {
                        IInventory recipeInput = new Inventory(key);
                        return CraftingHelpers.findServerRecipe(getRegistry(), recipeInput, getLevel());
                    }

                    @Override
                    public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                        return ItemStack.matches(cacheKey, newKey);
                    }
                });

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerSqueezer(this::getLevel)));
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    public SingleUseTank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundNBT tag) {
        inventory.readFromNBT(tag, "inventory");
        tank.readFromNBT(tag, "tank");
        super.read(tag);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        inventory.writeToNBT(tag, "inventory");
        tank.writeToNBT(tag, "tank");
        return super.save(tag);
    }

    protected IRecipeType<RecipeSqueezer> getRegistry() {
        return RegistryEntries.RECIPETYPE_SQUEEZER;
    }

    public Optional<RecipeSqueezer> getCurrentRecipe() {
        return recipeCache.get(getInventory().getItem(0).copy());
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!getLevel().isClientSide) {
            if(!getTank().isEmpty()) {
                Direction.Axis axis = getLevel().getBlockState(getBlockPos()).getValue(BlockSqueezer.AXIS);
                Arrays.stream(Direction.AxisDirection.values())
                        .map(axisDirection -> Direction.get(axisDirection, axis))
                        .forEach(side -> {
                            if (!getTank().isEmpty()) {
                                TileHelpers.getCapability(getLevel(), getBlockPos().relative(side), side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                                        .ifPresent(handler -> {
                                            FluidStack fluidStack = new FluidStack(getTank().getFluid(),
                                                    Math.min(100, getTank().getFluidAmount()));
                                            if (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                                                int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                                getTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                            }
                                        });
                            }
                        });
            } else {
                if (itemHeight == 7) {
                    Optional<RecipeSqueezer> recipeOptional = getCurrentRecipe();
                    if (recipeOptional.isPresent()) {
                        RecipeSqueezer recipe = recipeOptional.get();
                        getInventory().setItem(0, ItemStack.EMPTY);
                        for (RecipeSqueezer.ItemStackChance itemStackChance : recipe.getOutputItems()) {
                            if (itemStackChance.getChance() == 1.0F || itemStackChance.getChance() >= getLevel().random.nextFloat()) {
                                ItemStack resultStack = itemStackChance.getItemStack().copy();
                                for (Direction side : Direction.values()) {
                                    if (!resultStack.isEmpty() && side != Direction.UP) {
                                        IItemHandler itemHandler = TileHelpers.getCapability(getLevel(), getBlockPos().relative(side), side.getOpposite(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                                        if (itemHandler != null) {
                                            resultStack = ItemHandlerHelper.insertItem(itemHandler, resultStack, false);
                                        }
                                    }
                                }
                                if (!resultStack.isEmpty()) {
                                    ItemStackHelpers.spawnItemStack(getLevel(), getBlockPos(), resultStack);
                                }
                            }
                        }
                        if (!recipe.getOutputFluid().isEmpty()) {
                            getTank().fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
        getInventory().setChanged();
    }
}
