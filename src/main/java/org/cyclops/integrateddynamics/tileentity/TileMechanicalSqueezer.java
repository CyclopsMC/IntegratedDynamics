package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezerConfig;
import org.cyclops.integrateddynamics.core.recipe.custom.RecipeHandlerSqueezer;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A part entity for the mechanical squeezer.
 * @author rubensworks
 */
public class TileMechanicalSqueezer extends TileMechanicalMachine<ItemStack, BlockMechanicalSqueezer,
        IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>
        implements INamedContainerProvider {

    public static final int INVENTORY_SIZE = 5;

    private static final int SLOT_INPUT = 0;
    private static final int[] SLOTS_OUTPUT = {1, 2, 3, 4};
    private static final int TANK_SIZE = FluidHelpers.BUCKET_VOLUME * 100;

    @NBTPersist
    private boolean autoEjectFluids = false;

    private final SingleUseTank tank = new SingleUseTank(TANK_SIZE);

    public TileMechanicalSqueezer() {
        super(RegistryEntries.TILE_ENTITY_MECHANICAL_SQUEEZER, INVENTORY_SIZE);

        // Add fluid tank capability
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(() -> this.tank));

        // Add recipe handler capability
        addCapabilityInternal(Capabilities.RECIPE_HANDLER, LazyOptional.of(() -> new RecipeHandlerSqueezer<>(RegistryEntries.BLOCK_MECHANICAL_SQUEEZER)));

        // Add tank update listeners
        tank.addDirtyMarkListener(this::onTankChanged);
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
        return getWorld().getBlockState(getPos()).get(BlockMechanicalSqueezer.LIT);
    }

    @Override
    public void setWorking(boolean working) {
        getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos())
                .with(BlockMechanicalSqueezer.LIT, working));
    }

    public SingleUseTank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        getTank().readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("tank", getTank().writeToNBT(new CompoundNBT()));
        return super.write(tag);
    }

    @Override
    protected IRecipeRegistry<BlockMechanicalSqueezer, IngredientRecipeComponent,
                IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.getRecipeRegistry();
    }

    @Override
    protected ItemStack getCurrentRecipeCacheKey() {
        return getInventory().getStackInSlot(SLOT_INPUT).copy();
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
            ItemStack outputStack = recipeComponent.getFirstItemStack().copy();
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
            if (getTank().fill(outputFluid.copy(), FluidHelpers.simulateBooleanToAction(simulate)) != outputFluid.getAmount()) {
                return false;
            }
        }

        // Only consume items if we are not simulating
        if (!simulate) {
            getInventory().decrStackSize(SLOT_INPUT, 1);
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
        if (!world.isRemote()) {
            // Auto-eject fluid
            if (isAutoEjectFluids() && !getTank().isEmpty()) {
                for (Direction side : Direction.values()) {
                    IFluidHandler handler = TileHelpers.getCapability(getWorld(), getPos().offset(side),
                            side.getOpposite(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
                    if(handler != null) {
                        FluidStack fluidStack = getTank().getFluid().copy();
                        fluidStack.setAmount(Math.min(BlockMechanicalSqueezerConfig.autoEjectFluidRate, fluidStack.getAmount()));
                        if (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0) {
                            getTank().drain(handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
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

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerMechanicalSqueezer(id, playerInventory, this.getInventory(), Optional.of(this));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.integrateddynamics.mechanical_squeezer");
    }
}
