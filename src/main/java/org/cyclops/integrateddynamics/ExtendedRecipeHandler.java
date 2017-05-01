package org.cyclops.integrateddynamics;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.recipe.ItemBlockEnergyContainerCombinationRecipe;
import org.cyclops.integrateddynamics.item.ItemFacadeConfig;
import org.cyclops.integrateddynamics.item.ItemVariableConfig;
import org.cyclops.integrateddynamics.recipe.ItemFacadeRecipe;
import org.cyclops.integrateddynamics.recipe.ItemPartClearRecipe;
import org.cyclops.integrateddynamics.recipe.ItemVariableClearRecipe;
import org.cyclops.integrateddynamics.recipe.ItemVariableCopyRecipe;

/**
 * An extended recipe handler.
 * @author rubensworks
 */
public class ExtendedRecipeHandler extends RecipeHandler {

    public ExtendedRecipeHandler(ModBase mod, String... fileNames) {
        super(mod, fileNames);
    }

    @Override
    protected void registerRecipeSorters() {
        super.registerRecipeSorters();
        RecipeSorter.register(Reference.MOD_ID + "facadecombination", ItemFacadeRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
        RecipeSorter.register(Reference.MOD_ID + "variablecopy", ItemVariableCopyRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
        RecipeSorter.register(Reference.MOD_ID + "variableclear", ItemVariableClearRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
        RecipeSorter.register(Reference.MOD_ID + "partclear", ItemPartClearRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
        RecipeSorter.register(Reference.MOD_ID + "energybatterycombination", ItemBlockEnergyContainerCombinationRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
    }

    @Override
    protected void registerCustomRecipes() {
        super.registerCustomRecipes();

        // Facade recipes
        if(ConfigHandler.isEnabled(ItemFacadeConfig.class)) {
            GameRegistry.addRecipe(new ItemFacadeRecipe());
        }

        // Variable copy and clear recipes
        if(ConfigHandler.isEnabled(ItemVariableConfig.class)) {
            GameRegistry.addRecipe(new ItemVariableCopyRecipe());
            GameRegistry.addRecipe(new ItemVariableClearRecipe());
        }

        // Part item clear recipe
        GameRegistry.addRecipe(new ItemPartClearRecipe());

        // Energy battery upgrades
        if(ConfigHandler.isEnabled(BlockEnergyBatteryConfig.class)) {
            for(int i = 1; i < 9; i++) {
                ItemBlockEnergyContainer tankItem = (ItemBlockEnergyContainer) Item.getItemFromBlock(BlockEnergyBattery.getInstance());
                GameRegistry.addRecipe(new ItemBlockEnergyContainerCombinationRecipe(i, tankItem, BlockEnergyBatteryConfig.maxCapacity));
            }
        }
    }

}
