package org.cyclops.integrateddynamics;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.RecipeSorter;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.integrateddynamics.block.BlockDelay;
import org.cyclops.integrateddynamics.block.BlockDelayConfig;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.block.BlockProxyConfig;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.recipe.ItemBlockEnergyContainerCombinationRecipe;
import org.cyclops.integrateddynamics.item.ItemFacadeConfig;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.item.ItemVariableConfig;
import org.cyclops.integrateddynamics.recipe.ItemFacadeRecipe;
import org.cyclops.integrateddynamics.recipe.ItemNbtClearRecipe;
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
        RecipeSorter.register(Reference.MOD_ID + "itemclear", ItemNbtClearRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
        RecipeSorter.register(Reference.MOD_ID + "energybatterycombination", ItemBlockEnergyContainerCombinationRecipe.class,
                RecipeSorter.Category.SHAPELESS, "after:forge:shapedore");
    }

    @Override
    protected void registerCustomRecipes() {
        super.registerCustomRecipes();

        // Facade recipes
        if(Configs.isEnabled(ItemFacadeConfig.class)) {
            CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "facade_0"), new ItemFacadeRecipe());
        }

        // Variable copy and clear recipes
        if(Configs.isEnabled(ItemVariableConfig.class)) {
            CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "variable_copy"),
                    new ItemVariableCopyRecipe());
            CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "variable_clear"),
                    new ItemNbtClearRecipe(ItemVariable.getInstance()));
        }

        // Part item clear recipe
        CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "part_clear"),
                new ItemNbtClearRecipe(ItemPart.class, PartTypes.REDSTONE_READER.getItem()));

        // Proxy clear
        if(Configs.isEnabled(BlockProxyConfig.class)) {
            CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "proxy_clear"),
                    new ItemNbtClearRecipe(Item.getItemFromBlock(BlockProxy.getInstance())));
        }

        // Delay clear
        if(Configs.isEnabled(BlockDelayConfig.class)) {
            CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "delay_clear"),
                    new ItemNbtClearRecipe(Item.getItemFromBlock(BlockDelay.getInstance())));
        }

        // Energy battery upgrades
        if(Configs.isEnabled(BlockEnergyBatteryConfig.class)) {
            for(int i = 1; i < 9; i++) {
                ItemBlockEnergyContainer tankItem = (ItemBlockEnergyContainer) Item.getItemFromBlock(BlockEnergyBattery.getInstance());
                CraftingHelpers.registerRecipe(new ResourceLocation(getMod().getModId(), "combine_battery_" + i),
                        new ItemBlockEnergyContainerCombinationRecipe(i, tankItem, BlockEnergyBatteryConfig.maxCapacity));
            }
        }
    }

}
