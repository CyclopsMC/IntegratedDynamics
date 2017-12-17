package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

/**
 * A part entity for squeezing stuff.
 * @author rubensworks
 */
public class TileSqueezer extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    @Getter
    private int itemHeight = 1;

    private SingleCache<ItemStack,
            IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>> recipeCache;

    public TileSqueezer() {
        super(1, "squeezerInventory", 1, Fluid.BUCKET_VOLUME, "squeezerTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(0));

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
    }

    protected IRecipeRegistry<BlockSqueezer, IngredientRecipeComponent,
            IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> getRegistry() {
        return BlockSqueezer.getInstance().getRecipeRegistry();
    }

    public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> getCurrentRecipe() {
        return recipeCache.get(getStackInSlot(0).copy());
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!getWorld().isRemote) {
            if(!getTank().isEmpty()) {
                EnumFacing[] sides = getWorld().getBlockState(getPos()).getValue(BlockSqueezer.AXIS).getSides();
                for (EnumFacing side : sides) {
                    IFluidHandler handler = TileHelpers.getCapability(getWorld(), getPos().offset(side), side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if (!getTank().isEmpty() && handler != null) {
                        FluidStack fluidStack = new FluidStack(getTank().getFluid(),
                                Math.min(100, getTank().getFluidAmount()));
                        if (handler.fill(fluidStack, false) > 0) {
                            int filled = handler.fill(fluidStack, true);
                            drain(filled, true);
                        }
                    }
                }
            } else {
                if (itemHeight == 7 && getCurrentRecipe() != null) {
                    IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe = getCurrentRecipe();
                        setInventorySlotContents(0, ItemStack.EMPTY);
                        for (IngredientRecipeComponent recipeComponent : recipe.getOutput().getSubIngredientComponents()) {
                            if (recipeComponent.getChance() == 1.0F || recipeComponent.getChance() >= getWorld().rand.nextFloat()) {
                                ItemStack resultStack = recipeComponent.getFirstItemStack().copy();
                                for (EnumFacing side : EnumFacing.VALUES) {
                                    if (!resultStack.isEmpty() && side != EnumFacing.UP) {
                                        IItemHandler itemHandler = TileHelpers.getCapability(getWorld(), getPos().offset(side), side.getOpposite(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
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
                            fill(recipe.getOutput().getFluidStack(), true);
                        }
                } else {
                    sendUpdate();
                }
            }
        }
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return getWorld().getBlockState(getPos()).getValue(BlockSqueezer.HEIGHT) == 1 && getStackInSlot(0).isEmpty() && super.canInsertItem(slot, itemStack, side);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        itemHeight = 1;
        sendUpdate();
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
        updateInventoryHash();
    }
}
