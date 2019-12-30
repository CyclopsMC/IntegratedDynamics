package org.cyclops.integrateddynamics.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.custom.RecipeHandlerDryingBasin;

/**
 * A part entity for drying stuff.
 * @author rubensworks
 */
public class TileDryingBasin extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    private static final int WOOD_IGNITION_TEMPERATURE = 573; // 300 degrees celcius

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private final SimpleInventory inventory;
    private final SingleUseTank tank;

    @NBTPersist
    private Float randomRotation = 0F;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int fire = 0;

    private SingleCache<Pair<ItemStack, FluidStack>,
            IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>> recipeCache;

    public TileDryingBasin() {
        super(RegistryEntries.TILE_ENTITY_DRYING_BASIN);

        // Create inventory and tank
        this.inventory = new SimpleInventory(1, 1) {
            @Override
            public boolean isItemValidForSlot(int i, ItemStack itemstack) {
                return getStackInSlot(0).isEmpty();
            }

            @Override
            public void setInventorySlotContents(int slotId, ItemStack itemstack) {
                super.setInventorySlotContents(slotId, itemstack);
                TileDryingBasin.this.randomRotation = world.rand.nextFloat() * 360;
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
                            } else if(key.getRight().getAmount() >= recipe.getInput().getFluidStack().getAmount()
                                    && (maxRecipe == null
                                        || recipe.getInput().getFluidStack().getAmount() > maxRecipe.getInput().getFluidStack().getAmount())) {
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
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerDryingBasin<>(RegistryEntries.BLOCK_DRYING_BASIN)));
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

    protected IRecipeRegistry<BlockDryingBasin, IngredientAndFluidStackRecipeComponent,
            IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getRegistry() {
        return RegistryEntries.BLOCK_DRYING_BASIN.getRecipeRegistry();
    }

    public IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getInventory().getStackInSlot(0).copy(), FluidHelpers.copy(getTank().getFluid())));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!world.isRemote()) {
            if (!getTank().isEmpty() && getTank().getFluid().getFluid().getAttributes().getTemperature(getTank().getFluid()) >= WOOD_IGNITION_TEMPERATURE) {
                if (++fire >= 100) {
                    getWorld().setBlockState(getPos(), Blocks.FIRE.getDefaultState());
                } else if (getWorld().isAirBlock(getPos().offset(Direction.UP)) && world.rand.nextInt(10) == 0) {
                    getWorld().setBlockState(getPos().offset(Direction.UP), Blocks.FIRE.getDefaultState());
                }

            } else if (getCurrentRecipe() != null) {
                IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = getCurrentRecipe();
                if (progress >= recipe.getProperties().getDuration()) {
                    ItemStack output = recipe.getOutput().getFirstItemStack();
                    if (!output.isEmpty()) {
                        output = output.copy();
                        getInventory().setInventorySlotContents(0, output);
                        int amount = FluidHelpers.getAmount(recipe.getInput().getFluidStack());
                        getTank().drain(amount, IFluidHandler.FluidAction.EXECUTE);
                        if (recipe.getOutput().getFluidStack() != null) {
                            if (getTank().fill(recipe.getOutput().getFluidStack(), IFluidHandler.FluidAction.EXECUTE) == 0) {
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
                BlockState blockState = getTank().getFluid().getFluid().getAttributes().getBlock(getWorld(), getPos(),
                        getTank().getFluid().getFluid().getDefaultState());
                if(blockState != null) {
                    getWorld().addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, blockState),
                            getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                            getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0);
                }
            }
            if(!getInventory().getStackInSlot(0).isEmpty()) {
                ItemStack itemStack = getInventory().getStackInSlot(0);
                getWorld().addParticle(new ItemParticleData(ParticleTypes.ITEM, itemStack),
                        getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                        getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0);
            }
        }
    }

    /**
     * Get the random rotation for displaying the item.
     * @return The random rotation.
     */
    public float getRandomRotation() {
        return randomRotation;
    }
}
