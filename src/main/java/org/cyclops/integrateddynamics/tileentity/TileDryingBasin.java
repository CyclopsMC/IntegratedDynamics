package org.cyclops.integrateddynamics.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.cyclopscore.recipe.type.InventoryFluid;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
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

    private SingleCache<Pair<ItemStack, FluidStack>, Optional<RecipeDryingBasin>> recipeCache;

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
        recipeCache = new SingleCache<>(new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>, Optional<RecipeDryingBasin>>() {
            @Override
            public Optional<RecipeDryingBasin> getNewValue(Pair<ItemStack, FluidStack> key) {
                IInventoryFluid recipeInput = new InventoryFluid(
                        NonNullList.from(ItemStack.EMPTY, key.getLeft()),
                        NonNullList.from(FluidStack.EMPTY, key.getRight()));
                return CraftingHelpers.findServerRecipe(getRegistry(), recipeInput, getWorld());
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
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerDryingBasin(this::getWorld)));
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

    protected IRecipeType<RecipeDryingBasin> getRegistry() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN;
    }

    public Optional<RecipeDryingBasin> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getInventory().getStackInSlot(0).copy(), FluidHelpers.copy(getTank().getFluid())));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!world.isRemote()) {
            Optional<RecipeDryingBasin> currentRecipe = getCurrentRecipe();
            if (!getTank().isEmpty() && getTank().getFluid().getFluid().getAttributes().getTemperature(getTank().getFluid()) >= WOOD_IGNITION_TEMPERATURE) {
                if (++fire >= 100) {
                    getWorld().setBlockState(getPos(), Blocks.FIRE.getDefaultState());
                } else if (getWorld().isAirBlock(getPos().offset(Direction.UP)) && world.rand.nextInt(10) == 0) {
                    getWorld().setBlockState(getPos().offset(Direction.UP), Blocks.FIRE.getDefaultState());
                }

            } else if (currentRecipe.isPresent()) {
                RecipeDryingBasin recipe = currentRecipe.get();
                if (progress >= recipe.getDuration()) {
                    ItemStack output = recipe.getOutputItem();
                    if (!output.isEmpty()) {
                        output = output.copy();
                        getInventory().setInventorySlotContents(0, output);
                        int amount = FluidHelpers.getAmount(recipe.getInputFluid());
                        getTank().drain(amount, IFluidHandler.FluidAction.EXECUTE);
                        if (!recipe.getOutputFluid().isEmpty()) {
                            if (getTank().fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE) == 0) {
                                IntegratedDynamics.clog(Level.ERROR, "Encountered an invalid recipe: " + recipe.getId());
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
                    // TODO: send via packet to client
                    getWorld().addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, blockState),
                            getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                            getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0);
                }
            }
            if(!getInventory().getStackInSlot(0).isEmpty()) {
                ItemStack itemStack = getInventory().getStackInSlot(0);
                // TODO: send via packet to client
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
