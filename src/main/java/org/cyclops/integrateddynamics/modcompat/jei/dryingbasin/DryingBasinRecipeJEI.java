package org.cyclops.integrateddynamics.modcompat.jei.dryingbasin;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Recipe wrapper for Drying Basin recipes
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DryingBasinRecipeJEI extends BlankRecipeWrapper {

    private final List<ItemStack> inputItem;
    private final FluidStack inputFluid;
    private final List<ItemStack> outputItem;
    private final FluidStack outputFluid;

    public DryingBasinRecipeJEI(IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        this.inputItem = recipe.getInput().getItemStacks();
        this.inputFluid = recipe.getInput().getFluidStack();
        this.outputItem = recipe.getOutput().getItemStacks();
        this.outputFluid = recipe.getOutput().getFluidStack();
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
        return Lists.transform(BlockDryingBasin.getInstance().getRecipeRegistry().allRecipes(), new Function<IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>, DryingBasinRecipeJEI>() {
            @Nullable
            @Override
            public DryingBasinRecipeJEI apply(IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> input) {
                return new DryingBasinRecipeJEI(input);
            }
        });
    }
}
