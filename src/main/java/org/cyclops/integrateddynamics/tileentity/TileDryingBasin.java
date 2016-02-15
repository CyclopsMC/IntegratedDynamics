package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.experimental.Delegate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;

/**
 * A tile entity for drying stuff.
 * @author rubensworks
 */
public class TileDryingBasin extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Float randomRotation = 0F;
    @NBTPersist
    private int progress = 0;

    private SingleCache<Pair<ItemStack, FluidStack>,
            IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>> recipeCache;

    public TileDryingBasin() {
        super(1, "dryingBasingInventory", 1, FluidContainerRegistry.BUCKET_VOLUME, "dryingBasingTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(1));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(1));

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>,
                        IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
                    @Override
                    public IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getNewValue(Pair<ItemStack, FluidStack> key) {
                        ItemAndFluidStackRecipeComponent recipeInput =
                                new ItemAndFluidStackRecipeComponent(key.getLeft(), key.getRight());
                        IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> maxRecipe = null;
                        for (IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe : getRegistry().findRecipesByInput(recipeInput)) {
                            if(key.getRight() == null) {
                                return recipe;
                            } else if(key.getRight().amount >= recipe.getInput().getFluidStack().amount
                                    && (maxRecipe == null
                                        || recipe.getInput().getFluidStack().amount > maxRecipe.getInput().getFluidStack().amount)) {
                                maxRecipe = recipe;
                            }
                        }
                        return maxRecipe;
                    }

                    @Override
                    public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                        return cacheKey == null || newKey == null ||
                                (ItemStack.areItemStacksEqual(cacheKey.getLeft(), newKey.getLeft()) &&
                                        FluidStack.areFluidStackTagsEqual(cacheKey.getRight(), newKey.getRight())) &&
                                        FluidHelpers.getAmount(cacheKey.getRight()) == FluidHelpers.getAmount(newKey.getRight());
                    }
                });
    }

    protected IRecipeRegistry<BlockDryingBasin, ItemAndFluidStackRecipeComponent,
            ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getRegistry() {
        return BlockDryingBasin.getInstance().getRecipeRegistry();
    }

    protected IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getStackInSlot(0), FluidHelpers.copy(getTank().getFluid())));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!worldObj.isRemote) {
            if (getCurrentRecipe() != null) {
                IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = getCurrentRecipe();
                if (progress >= recipe.getProperties().getDuration()) {
                    setInventorySlotContents(0, recipe.getOutput().getItemStack());
                    int amount = FluidHelpers.getAmount(recipe.getInput().getFluidStack());
                    drain(amount, true);
                    if (recipe.getOutput().getFluidStack() != null) {
                        if (fill(recipe.getOutput().getFluidStack(), true) == 0) {
                            IntegratedDynamics.clog(Level.ERROR, "Encountered an invalid recipe: " + recipe.getNamedId());
                        }
                    }
                    progress = 0;
                } else {
                    progress++;
                    sendUpdate();
                }
            } else {
                progress = 0;
            }
        }
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        this.randomRotation = worldObj.rand.nextFloat() * 360;
        sendUpdate();
    }

    /**
     * Get the random rotation for displaying the item.
     * @return The random rotation.
     */
    public float getRandomRotation() {
        return randomRotation;
    }
}
