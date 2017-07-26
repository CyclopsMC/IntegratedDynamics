package org.cyclops.integrateddynamics.modcompat.jei.dryingbasin;

import com.google.common.collect.Lists;
import lombok.Data;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.modcompat.jei.RecipeRegistryJeiRecipeWrapper;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;

import java.util.List;

/**
 * Recipe wrapper for Drying Basin recipes
 * @author rubensworks
 */
@Data
public class DryingBasinRecipeJEI extends RecipeRegistryJeiRecipeWrapper<BlockDryingBasin, ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties, DryingBasinRecipeJEI> {

    private final List<ItemStack> inputItem;
    private final FluidStack inputFluid;
    private final List<ItemStack> outputItem;
    private final FluidStack outputFluid;

    public DryingBasinRecipeJEI(IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        super(recipe);
        this.inputItem = recipe.getInput().getItemStacks();
        this.inputFluid = recipe.getInput().getFluidStack();
        this.outputItem = recipe.getOutput().getItemStacks();
        this.outputFluid = recipe.getOutput().getFluidStack();
    }

    protected DryingBasinRecipeJEI() {
        super(null);
        this.inputItem = null;
        this.inputFluid = null;
        this.outputItem = null;
        this.outputFluid = null;
    }

    @Override
    protected IRecipeRegistry<BlockDryingBasin, ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getRecipeRegistry() {
        return BlockDryingBasin.getInstance().getRecipeRegistry();
    }

    @Override
    protected DryingBasinRecipeJEI newInstance(IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> input) {
        return new DryingBasinRecipeJEI(input);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, getInputItem());
        ingredients.setOutputs(ItemStack.class, getOutputItem());
        ingredients.setInput(FluidStack.class, getInputFluid());
        ingredients.setOutput(FluidStack.class, getOutputFluid());
    }

    @Override
    public List getInputs() {
        return getInputItem();
    }

    @Override
    public List getOutputs() {
        return getOutputItem();
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return Lists.newArrayList(getInputFluid());
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return Lists.newArrayList(getOutputFluid());
    }

    public static List<DryingBasinRecipeJEI> getAllRecipes() {
        return new DryingBasinRecipeJEI().createAllRecipes();
    }
}
