package org.cyclops.integrateddynamics.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
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
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.core.recipe.custom.RecipeHandlerSqueezer;

import java.util.Arrays;

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

    private SingleCache<ItemStack,
            IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>> recipeCache;

    public TileSqueezer() {
        super(RegistryEntries.TILE_ENTITY_SQUEEZER);

        // Create inventory and tank
        this.inventory = new SimpleInventory(1, 1) {
            @Override
            public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
                return getWorld().getBlockState(getPos()).get(BlockSqueezer.HEIGHT) == 1
                        && getStackInSlot(0).isEmpty() && super.isItemValidForSlot(slot, itemStack);
            }

            @Override
            public void setInventorySlotContents(int slotId, ItemStack itemstack) {
                super.setInventorySlotContents(slotId, itemstack);
                itemHeight = 1;
                sendUpdate();
            }
        };
        this.tank = new SingleUseTank(FluidHelpers.BUCKET_VOLUME);
        addCapabilityInternal(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, LazyOptional.of(this.getInventory()::getItemHandler));
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(this::getTank));

        // Add dirty mark listeners to inventory and tank
        this.inventory.addDirtyMarkListener(this::sendUpdate);
        this.tank.addDirtyMarkListener(this.inventory::markDirty);

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<ItemStack,
                        IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>>() {
                    @Override
                    public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> getNewValue(ItemStack key) {
                        IngredientRecipeComponent recipeInput = new IngredientRecipeComponent(key);
                        return getRegistry().findRecipeByInput(recipeInput);
                    }

                    @Override
                    public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                        return ItemStack.areItemStacksEqual(cacheKey, newKey);
                    }
                });

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerSqueezer<>(RegistryEntries.BLOCK_SQUEEZER)));
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
    public CompoundNBT write(CompoundNBT tag) {
        inventory.writeToNBT(tag, "inventory");
        tank.writeToNBT(tag, "tank");
        return super.write(tag);
    }

    protected IRecipeRegistry<BlockSqueezer, IngredientRecipeComponent,
            IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> getRegistry() {
        return RegistryEntries.BLOCK_SQUEEZER.getRecipeRegistry();
    }

    public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> getCurrentRecipe() {
        return recipeCache.get(getInventory().getStackInSlot(0).copy());
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!getWorld().isRemote) {
            if(!getTank().isEmpty()) {
                Direction.Axis axis = getWorld().getBlockState(getPos()).get(BlockSqueezer.AXIS);
                Arrays.stream(Direction.AxisDirection.values())
                        .map(axisDirection -> Direction.getFacingFromAxis(axisDirection, axis))
                        .forEach(side -> {
                            IFluidHandler handler = TileHelpers.getCapability(getWorld(), getPos().offset(side), side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
                            if (!getTank().isEmpty() && handler != null) {
                                FluidStack fluidStack = new FluidStack(getTank().getFluid(),
                                        Math.min(100, getTank().getFluidAmount()));
                                if (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                                    int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                    getTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                }
                            }
                        });
            } else {
                if (itemHeight == 7 && getCurrentRecipe() != null) {
                    IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe = getCurrentRecipe();
                        getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                        for (IngredientRecipeComponent recipeComponent : recipe.getOutput().getSubIngredientComponents()) {
                            if (recipeComponent.getChance() == 1.0F || recipeComponent.getChance() >= getWorld().rand.nextFloat()) {
                                ItemStack resultStack = recipeComponent.getFirstItemStack().copy();
                                for (Direction side : Direction.values()) {
                                    if (!resultStack.isEmpty() && side != Direction.UP) {
                                        IItemHandler itemHandler = TileHelpers.getCapability(getWorld(), getPos().offset(side), side.getOpposite(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                                        if (itemHandler != null) {
                                            resultStack = ItemHandlerHelper.insertItem(itemHandler, resultStack, false);
                                        }
                                    }
                                }
                                if (!resultStack.isEmpty()) {
                                    ItemStackHelpers.spawnItemStack(getWorld(), getPos(), resultStack);
                                }
                            }
                        }
                        if (recipe.getOutput().getFluidStack() != null) {
                            getTank().fill(recipe.getOutput().getFluidStack(), IFluidHandler.FluidAction.EXECUTE);
                        }
                }
            }
        }
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
        getInventory().markDirty();
    }
}
