package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.custom.RecipeHandlerDryingBasin;

/**
 * A part entity for drying stuff.
 * @author rubensworks
 */
public class TileDryingBasin extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    private static final int WOOD_IGNITION_TEMPERATURE = 573; // 300 degrees celcius

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Float randomRotation = 0F;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int fire = 0;

    private SingleCache<Pair<ItemStack, FluidStack>,
            IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>> recipeCache;

    public TileDryingBasin() {
        super(1, "dryingBasingInventory", 1, Fluid.BUCKET_VOLUME, "dryingBasingTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(0));

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>,
                        IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
                    @Override
                    public IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getNewValue(Pair<ItemStack, FluidStack> key) {
                        IngredientAndFluidStackRecipeComponent recipeInput =
                                new IngredientAndFluidStackRecipeComponent(key.getLeft(), key.getRight());
                        IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> maxRecipe = null;
                        for (IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe : getRegistry().findRecipesByInput(recipeInput)) {
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

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, new RecipeHandlerDryingBasin<>(BlockDryingBasin.getInstance()));
    }

    @Override
    public boolean isSendUpdateOnInventoryChanged() {
        return true;
    }

    @Override
    protected boolean isUpdateInventoryHashOnTankContentsChanged() {
        return true;
    }

    @Override
    public boolean isSendUpdateOnTankChanged() {
        return true;
    }

    protected IRecipeRegistry<BlockDryingBasin, IngredientAndFluidStackRecipeComponent,
            IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getRegistry() {
        return BlockDryingBasin.getInstance().getRecipeRegistry();
    }

    public IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getStackInSlot(0).copy(), FluidHelpers.copy(getTank().getFluid())));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!world.isRemote) {
            if (!getTank().isEmpty() && getTank().getFluid().getFluid().getTemperature(getTank().getFluid()) >= WOOD_IGNITION_TEMPERATURE) {
                if (++fire >= 100) {
                    getWorld().setBlockState(getPos(), Blocks.FIRE.getDefaultState());
                } else if (getWorld().isAirBlock(getPos().offset(EnumFacing.UP)) && world.rand.nextInt(10) == 0) {
                    getWorld().setBlockState(getPos().offset(EnumFacing.UP), Blocks.FIRE.getDefaultState());
                }

            } else if (getCurrentRecipe() != null) {
                IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = getCurrentRecipe();
                if (progress >= recipe.getProperties().getDuration()) {
                    ItemStack output = recipe.getOutput().getFirstItemStack();
                    if (!output.isEmpty()) {
                        output = output.copy();
                        setInventorySlotContents(0, output);
                        int amount = FluidHelpers.getAmount(recipe.getInput().getFluidStack());
                        drain(amount, true);
                        if (recipe.getOutput().getFluidStack() != null) {
                            if (fill(recipe.getOutput().getFluidStack(), true) == 0) {
                                IntegratedDynamics.clog(Level.ERROR, "Encountered an invalid recipe: " + recipe.getNamedId());
                            }
                        }
                    }
                    progress = 0;
                } else {
                    progress++;
                    markDirty();
                }
                fire = 0;
            } else {
                if ((progress > 0) || (fire > 0)) {
                    progress = 0;
                    fire = 0;
                    markDirty();
                }
            }
        } else if(progress > 0 && world.rand.nextInt(5) == 0) {
            if(!getTank().isEmpty()) {
                Block block = getTank().getFluid().getFluid().getBlock();
                if(block != null) {
                    int blockStateId = Block.getStateId(block.getDefaultState());
                    getWorld().spawnParticle(EnumParticleTypes.BLOCK_DUST,
                            getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                            getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0, blockStateId);
                }
            }
            if(!getStackInSlot(0).isEmpty()) {
                int itemId = Item.getIdFromItem(getStackInSlot(0).getItem());
                getWorld().spawnParticle(EnumParticleTypes.ITEM_CRACK,
                        getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                        getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0, itemId);
            }
        }
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return getStackInSlot(0).isEmpty();
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        this.randomRotation = world.rand.nextFloat() * 360;
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
