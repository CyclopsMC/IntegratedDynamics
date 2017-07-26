package org.cyclops.integrateddynamics.modcompat.minetweaker.handlers;

import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import org.cyclops.cyclopscore.modcompat.minetweaker.handlers.RecipeRegistryHandler;
import org.cyclops.cyclopscore.recipe.custom.Recipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.integrateddynamics.DryingBasin")
public class DryingBasinHandler extends RecipeRegistryHandler<BlockDryingBasin, ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> {

    private static final DryingBasinHandler INSTANCE = new DryingBasinHandler();

    @Override
    protected BlockDryingBasin getMachine() {
        return BlockDryingBasin.getInstance();
    }

    @Override
    protected String getRegistryName() {
        return "DryingBasin";
    }

    @ZenMethod
    public static void addRecipe(@Optional IItemStack inputStack, @Optional ILiquidStack inputFluid,
                           @Optional IItemStack outputStack, @Optional ILiquidStack outputFluid, int duration) {
        INSTANCE.add(new Recipe<>(
                new ItemAndFluidStackRecipeComponent(RecipeRegistryHandler.toStack(inputStack), RecipeRegistryHandler.toFluid(inputFluid)),
                new ItemAndFluidStackRecipeComponent(RecipeRegistryHandler.toStack(outputStack), RecipeRegistryHandler.toFluid(outputFluid)),
                new DurationRecipeProperties(duration)));
    }

    @ZenMethod
    public static void removeRecipe(@Optional IItemStack inputStack, @Optional ILiquidStack inputFluid,
                              @Optional IItemStack outputStack, @Optional ILiquidStack outputFluid, int duration) {
        INSTANCE.remove(new Recipe<>(
                new ItemAndFluidStackRecipeComponent(RecipeRegistryHandler.toStack(inputStack), RecipeRegistryHandler.toFluid(inputFluid)),
                new ItemAndFluidStackRecipeComponent(RecipeRegistryHandler.toStack(outputStack), RecipeRegistryHandler.toFluid(outputFluid)),
                new DurationRecipeProperties(duration)));
    }

    @ZenMethod
    public static void removeRecipesWithOutput(@Optional IItemStack outputStack, @Optional ILiquidStack outputFluid) {
        INSTANCE.remove(
                new ItemAndFluidStackRecipeComponent(RecipeRegistryHandler.toStack(outputStack), RecipeRegistryHandler.toFluid(outputFluid))
        );
    }
}
