package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
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
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.block.BlockSqueezer;

/**
 * A tile entity for squeezing stuff.
 * @author rubensworks
 */
public class TileSqueezer extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    @Getter
    private int itemHeight = 1;

    private SingleCache<ItemStack,
            IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent>> recipeCache;

    public TileSqueezer() {
        super(1, "squeezerInventory", 1, FluidContainerRegistry.BUCKET_VOLUME, "squeezerTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(0));

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<ItemStack,
                        IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent>>() {
                    @Override
                    public IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> getNewValue(ItemStack key) {
                        ItemStackRecipeComponent recipeInput = new ItemStackRecipeComponent(key);
                        return getRegistry().findRecipeByInput(recipeInput);
                    }

                    @Override
                    public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                        return ItemStack.areItemStacksEqual(cacheKey, newKey);
                    }
                });
    }

    protected IRecipeRegistry<BlockSqueezer, ItemStackRecipeComponent,
            ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> getRegistry() {
        return BlockSqueezer.getInstance().getRecipeRegistry();
    }

    public IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> getCurrentRecipe() {
        return recipeCache.get(getStackInSlot(0));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!getWorld().isRemote) {
            if(!getTank().isEmpty()) {
                EnumFacing[] sides = getWorld().getBlockState(getPos()).getValue(BlockSqueezer.AXIS).getSides();
                for (EnumFacing side : sides) {
                    IFluidHandler handler = TileHelpers.getSafeTile(getWorld(), getPos().offset(side), IFluidHandler.class);
                    if (!getTank().isEmpty() && handler != null) {
                        FluidStack fluidStack = new FluidStack(getTank().getFluidType(),
                                Math.min(100, getTank().getFluidAmount()));
                        if (handler.fill(side.getOpposite(), fluidStack, false) > 0) {
                            int filled = handler.fill(side.getOpposite(), fluidStack, true);
                            drain(filled, true);
                        }
                    }
                }
            } else {
                if (itemHeight == 7 && getCurrentRecipe() != null) {
                    IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe = getCurrentRecipe();
                        setInventorySlotContents(0, null);
                        ItemStack resultStack = recipe.getOutput().getItemStack();
                        if(resultStack != null) {
                            resultStack = resultStack.copy();
                            for(EnumFacing side : EnumFacing.VALUES) {
                                if(resultStack != null && side != EnumFacing.UP) {
                                    IItemHandler itemHandler = TileHelpers.getCapability(getWorld(), getPos().offset(side), side.getOpposite(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                                    resultStack = ItemHandlerHelper.insertItem(itemHandler, resultStack, false);
                                }
                            }
                            if(resultStack != null) {
                                ItemStackHelpers.spawnItemStack(getWorld(), getPos(), resultStack);
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
        return getWorld().getBlockState(getPos()).getValue(BlockSqueezer.HEIGHT) == 1 && super.canInsertItem(slot, itemStack, side);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        if(itemstack == null) {
            itemHeight = 1;
        }
        sendUpdate();
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        sendUpdate();
    }
}
