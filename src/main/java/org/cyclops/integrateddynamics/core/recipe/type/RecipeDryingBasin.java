package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplayDryingBasin;

import java.util.List;
import java.util.Optional;

/**
 * Drying basin recipe
 * @author rubensworks
 */
public class RecipeDryingBasin implements Recipe<IInventoryFluid> {

    private final Optional<Ingredient> inputIngredient;
    private final Optional<FluidStackTemplate> inputFluid;
    private final Optional<Either<ItemStackTemplate, ItemStackFromIngredient>> outputItem;
    private final Optional<FluidStackTemplate> outputFluid;
    private final int duration;

    private PlacementInfo placementInfo;

    public RecipeDryingBasin(Optional<Ingredient> inputIngredient, Optional<FluidStackTemplate> inputFluid,
                             Optional<Either<ItemStackTemplate, ItemStackFromIngredient>> outputIngredient, Optional<FluidStackTemplate> outputFluid, int duration) {
        this.inputIngredient = inputIngredient;
        this.inputFluid = inputFluid;
        this.outputItem = outputIngredient;
        this.outputFluid = outputFluid;
        this.duration = duration;
    }

    public Optional<Ingredient> getInputIngredient() {
        return inputIngredient;
    }

    public Optional<FluidStackTemplate> getInputFluidTemplate() {
        return inputFluid;
    }

    public Optional<FluidStack> getInputFluid() {
        return inputFluid.map(FluidStackTemplate::create);
    }

    public Optional<Either<ItemStackTemplate, ItemStackFromIngredient>> getOutputItemTemplate() {
        return outputItem;
    }

    public Optional<Either<ItemStack, ItemStackFromIngredient>> getOutputItem() {
        return outputItem.map(either -> either.map(
                t -> Either.left(t.create()),
                Either::right));
    }

    public Optional<FluidStackTemplate> getOutputFluidTemplate() {
        return outputFluid;
    }

    public Optional<FluidStack> getOutputFluid() {
        return outputFluid.map(FluidStackTemplate::create);
    }

    public Optional<ItemStack> getOutputItemFirst() {
        return outputItem.map(either -> either.map(ItemStackTemplate::create, ItemStackFromIngredient::getFirstItemStack));
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(IInventoryFluid inv, Level worldIn) {
        return inputIngredient.map(p -> p.test(inv.getItem(0))).orElse(inv.getItem(0).isEmpty())
                && inputFluid.map(f -> f.fluid().value() == inv.getFluidHandler().getResource(0).getFluid()).orElse(inv.getFluidHandler().getResource(0).isEmpty())
                && inputFluid.map(f -> f.amount() <= inv.getFluidHandler().getAmountAsInt(0)).orElse(inv.getFluidHandler().getAmountAsInt(0) == 0);
    }

    @Override
    public ItemStack assemble(IInventoryFluid inv) {
        return this.getOutputItemFirst().get().copy();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeSerializer<? extends Recipe<IInventoryFluid>> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_DRYING_BASIN.get();
    }

    @Override
    public RecipeType<? extends Recipe<IInventoryFluid>> getType() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.inputIngredient.orElse(Ingredient.of(Items.BUCKET)));
        }
        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RegistryEntries.RECIPEBOOKCATEGORY_DRYING_BASIN.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecipeDisplayDryingBasin(
                this.getInputIngredient().map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
                this.getInputFluid().orElse(FluidStack.EMPTY),
                this.getOutputItemFirst().<SlotDisplay>map(stack -> new SlotDisplay.ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(stack))).orElse(SlotDisplay.Empty.INSTANCE),
                this.getOutputFluid().orElse(FluidStack.EMPTY),
                new SlotDisplay.ItemSlotDisplay(RegistryEntries.BLOCK_DRYING_BASIN.get().asItem()),
                this.getDuration()
        ));
    }
}
