package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezerConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

/**
 * A part entity for the mechanical squeezer.
 * @author rubensworks
 */
public class TileMechanicalSqueezer extends TileMechanicalMachine<ItemStack, BlockMechanicalSqueezer,
        IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> {

    private static final int SLOTS = 5;
    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};
    private static final int TANK_SIZE = Fluid.BUCKET_VOLUME * 100;

    @NBTPersist
    private boolean autoEjectFluids = false;

    private final SingleUseTank tank = new SingleUseTank(TANK_SIZE, this);

    public TileMechanicalSqueezer() {
        super(SLOTS);

        // Add fluid tank capability
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.tank);
    }

    @Override
    protected SingleCache.ICacheUpdater<ItemStack, IRecipe<IngredientRecipeComponent,
            IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>> createCacheUpdater() {
        return new SingleCache.ICacheUpdater<ItemStack,
                IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
            @Override
            public IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>
            getNewValue(ItemStack key) {
                IngredientRecipeComponent recipeInput = new IngredientRecipeComponent(key);
                return getRecipeRegistry().findRecipeByInput(recipeInput);
            }

            @Override
            public boolean isKeyEqual(ItemStack cacheKey, ItemStack newKey) {
                return ItemStack.areItemStacksEqual(cacheKey, newKey);
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{SLOT_INPUT};
    }

    @Override
    public int[] getOutputSlots() {
        return SLOTS_OUTPUT;
    }

    @Override
    public boolean wasWorking() {
        return getWorld().getBlockState(getPos()).getValue(BlockMechanicalSqueezer.ON);
    }

    @Override
    public void setWorking(boolean working) {
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos())
                .withProperty(BlockMechanicalSqueezer.ON, working));
    }

    public SingleUseTank getTank() {
        return tank;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        getTank().readFromNBT(tag.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("tank", getTank().writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(tag);
    }

    @Override
    protected IRecipeRegistry<BlockMechanicalSqueezer, IngredientRecipeComponent,
                IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return BlockMechanicalSqueezer.getInstance().getRecipeRegistry();
    }

    @Override
    protected ItemStack getCurrentRecipeCacheKey() {
        return getStackInSlot(SLOT_INPUT).copy();
    }

    @Override
    public IngredientRecipeComponent getRecipeInput(NonNullList<ItemStack> inputStacks) {
        return new IngredientRecipeComponent(inputStacks.get(0));
    }

    @Override
    public int getRecipeDuration(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        return recipe.getProperties().getDuration();
    }

    @Override
    protected boolean finalizeRecipe(IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> recipe, boolean simulate) {
        // Output items
        NonNullList<ItemStack> outputStacks = NonNullList.create();
        for (IngredientRecipeComponent recipeComponent : recipe.getOutput().getSubIngredientComponents()) {
            ItemStack outputStack = recipeComponent.getFirstItemStack();
            if (!outputStack.isEmpty() && (simulate || recipeComponent.getChance() == 1.0F
                    || recipeComponent.getChance() >= getWorld().rand.nextFloat())) {
                InventoryHelpers.addStackToList(outputStacks, outputStack);
            }
        }
        if (!InventoryHelpers.addToInventory(getInventory(), SLOTS_OUTPUT, outputStacks, simulate).isEmpty()) {
            return false;
        }

        // Output fluid
        FluidStack outputFluid = recipe.getOutput().getFluidStack();
        if (outputFluid != null) {
            if (getTank().fill(outputFluid.copy(), !simulate) != outputFluid.amount) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            this.decrStackSize(SLOT_INPUT, 1);
        }

        return true;
    }

    @Override
    public int getEnergyConsumptionRate() {
        return BlockMechanicalSqueezerConfig.consumptionRate;
    }

    @Override
    public int getMaxEnergyStored() {
        return BlockMechanicalSqueezerConfig.capacity;
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!world.isRemote) {
            // Auto-eject fluid
            if (isAutoEjectFluids() && !getTank().isEmpty()) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    IFluidHandler handler = TileHelpers.getCapability(getWorld(), getPos().offset(side),
                            side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(handler != null) {
                        FluidStack fluidStack = getTank().getFluid().copy();
                        fluidStack.amount = Math.min(BlockMechanicalSqueezerConfig.autoEjectFluidRate, fluidStack.amount);
                        if (handler.fill(fluidStack, false) > 0) {
                            getTank().drain(handler.fill(fluidStack, true), true);
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isAutoEjectFluids() {
        return autoEjectFluids;
    }

    public void setAutoEjectFluids(boolean autoEjectFluids) {
        this.autoEjectFluids = autoEjectFluids;
        sendUpdate();
    }
}
