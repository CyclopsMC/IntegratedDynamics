package org.cyclops.integrateddynamics.infobook;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemStackRecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockDryingBasinConfig;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.block.BlockSqueezerConfig;
import org.cyclops.integrateddynamics.infobook.pageelement.DryingBasinRecipeAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.SqueezerRecipeAppendix;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Infobook class for the On the Dynamics of Integration.
 * @author rubensworks
 */
public class OnTheDynamicsOfIntegrationBook extends InfoBook {

    private static OnTheDynamicsOfIntegrationBook _instance = null;

    static {
        if (ConfigHandler.isEnabled(BlockSqueezerConfig.class)) {
            InfoBookParser.registerFactory(Reference.MOD_ID + ":squeezerRecipe", new InfoBookParser.IAppendixFactory() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                    ItemStack itemStack;
                    try {
                        itemStack = InfoBookParser.createStack((Element) node.getElementsByTagName("item").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        itemStack = null;
                    }
                    FluidStack fluidStack;
                    try {
                        fluidStack = InfoBookParser.createFluidStack((Element) node.getElementsByTagName("fluid").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        fluidStack = null;
                    }

                    List<IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent>>
                            recipes = BlockSqueezer.getInstance().getRecipeRegistry().
                            findRecipesByOutput(new ItemAndFluidStackRecipeComponent(itemStack, fluidStack));
                    int index = InfoBookParser.getIndex(node);
                    if(index >= recipes.size()) {
                        throw new InfoBookParser.InvalidAppendixException("Could not find Squeezer recipe for "
                                + itemStack.getItem().getUnlocalizedName() + "with index " + index);
                    }
                    return new SqueezerRecipeAppendix(infoBook, recipes.get(index));
                }
            });

            InfoBookParser.registerFactory(Reference.MOD_ID + ":squeezerRecipe", new InfoBookParser.IAppendixItemFactory<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent>() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) throws InfoBookParser.InvalidAppendixException {
                    return new SqueezerRecipeAppendix(infoBook, recipe);
                }

            });
        } else {
            InfoBookParser.registerIgnoredFactory(Reference.MOD_ID + ":squeezerRecipe");
        }
        if (ConfigHandler.isEnabled(BlockDryingBasinConfig.class)) {
            InfoBookParser.registerFactory(Reference.MOD_ID + ":dryingBasinRecipe", new InfoBookParser.IAppendixFactory() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                    ItemStack itemStack;
                    try {
                        itemStack = InfoBookParser.createStack((Element) node.getElementsByTagName("item").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        itemStack = null;
                    }
                    FluidStack fluidStack;
                    try {
                        fluidStack = InfoBookParser.createFluidStack((Element) node.getElementsByTagName("fluid").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        fluidStack = null;
                    }

                    List<IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>>
                            recipes = BlockDryingBasin.getInstance().getRecipeRegistry().
                            findRecipesByOutput(new ItemAndFluidStackRecipeComponent(itemStack, fluidStack));
                    int index = InfoBookParser.getIndex(node);
                    if(index >= recipes.size()) {
                        throw new InfoBookParser.InvalidAppendixException("Could not find Drying Basin recipe for "
                                + itemStack.getItem().getUnlocalizedName() + "with index " + index);
                    }
                    return new DryingBasinRecipeAppendix(infoBook, recipes.get(index));
                }
            });

            InfoBookParser.registerFactory(Reference.MOD_ID + ":dryingBasinRecipe", new InfoBookParser.IAppendixItemFactory<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) throws InfoBookParser.InvalidAppendixException {
                    return new DryingBasinRecipeAppendix(infoBook, recipe);
                }

            });
        } else {
            InfoBookParser.registerIgnoredFactory(Reference.MOD_ID + ":dryingBasinRecipe");
        }
    }

    private OnTheDynamicsOfIntegrationBook() {
        super(IntegratedDynamics._instance, 2);
    }

    public static OnTheDynamicsOfIntegrationBook getInstance() {
        if(_instance == null) {
            _instance = new OnTheDynamicsOfIntegrationBook();
        }
        return _instance;
    }
}
