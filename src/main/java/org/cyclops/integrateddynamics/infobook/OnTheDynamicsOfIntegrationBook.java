package org.cyclops.integrateddynamics.infobook;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.infobook.pageelement.AspectAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.DryingBasinRecipeAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.MechanicalDryingBasinRecipeAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.MechanicalSqueezerRecipeAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.OperatorAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.SqueezerRecipeAppendix;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Infobook class for the On the Dynamics of Integration.
 * @author rubensworks
 */
public class OnTheDynamicsOfIntegrationBook extends InfoBook {

    private static OnTheDynamicsOfIntegrationBook _instance = null;

    static {
        InfoBookParser.registerFactory(Reference.MOD_ID + ":squeezer_recipe", new InfoBookParser.IAppendixFactory() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                NonNullList<Ingredient> ingredients = InfoBookParser.createOptionalIngredientsFromIngredient(node, infoBook.getMod().getRecipeHandler());
                FluidStack fluidStack = InfoBookParser.createOptionalFluidStackFromIngredient(node, infoBook.getMod().getRecipeHandler());

                List<IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>>
                        recipes = RegistryEntries.BLOCK_SQUEEZER.getRecipeRegistry().
                        findRecipesByOutput(new IngredientsAndFluidStackRecipeComponent(ingredients, fluidStack));
                int index = InfoBookParser.getIndex(node);
                if(index >= recipes.size()) {
                    StringBuilder unlocalizedItems = new StringBuilder();
                    for (Ingredient ingredient : ingredients) {
                        unlocalizedItems.append(ingredient.getMatchingStacks()[0].getTranslationKey());
                    }
                    throw new InfoBookParser.InvalidAppendixException("Could not find Squeezer recipe for "
                            + unlocalizedItems + " and "
                            + (fluidStack != null ? fluidStack.getFluid().getRegistryName() : "null")
                            + " with index " + index);
                }
                return new SqueezerRecipeAppendix(infoBook, recipes.get(index));
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":squeezer_recipe", new InfoBookParser.IAppendixItemFactory<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) throws InfoBookParser.InvalidAppendixException {
                return new SqueezerRecipeAppendix(infoBook, recipe);
            }

        });
        InfoBookParser.registerFactory(Reference.MOD_ID + ":drying_basin_recipe", new InfoBookParser.IAppendixFactory() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                ItemStack itemStack = InfoBookParser.createOptionalStackFromIngredient(node, infoBook.getMod().getRecipeHandler());
                FluidStack fluidStack = InfoBookParser.createOptionalFluidStackFromIngredient(node, infoBook.getMod().getRecipeHandler());

                List<IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>>
                        recipes = RegistryEntries.BLOCK_DRYING_BASIN.getRecipeRegistry().
                        findRecipesByOutput(new IngredientAndFluidStackRecipeComponent(itemStack, fluidStack));
                int index = InfoBookParser.getIndex(node);
                if(index >= recipes.size()) {
                    throw new InfoBookParser.InvalidAppendixException("Could not find Drying Basin recipe for "
                            + itemStack.getItem().getTranslationKey() + "with index " + index);
                }
                return new DryingBasinRecipeAppendix(infoBook, recipes.get(index));
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":drying_basin_recipe", new InfoBookParser.IAppendixItemFactory<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) throws InfoBookParser.InvalidAppendixException {
                return new DryingBasinRecipeAppendix(infoBook, recipe);
            }

        });
        InfoBookParser.registerFactory(Reference.MOD_ID + ":mechanical_squeezer_recipe", new InfoBookParser.IAppendixFactory() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                NonNullList<Ingredient> ingredients = InfoBookParser.createOptionalIngredientsFromIngredient(node, infoBook.getMod().getRecipeHandler());
                FluidStack fluidStack = InfoBookParser.createOptionalFluidStackFromIngredient(node, infoBook.getMod().getRecipeHandler());

                List<IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties>>
                        recipes = RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.getRecipeRegistry().
                        findRecipesByOutput(new IngredientsAndFluidStackRecipeComponent(ingredients, fluidStack));
                int index = InfoBookParser.getIndex(node);
                if(index >= recipes.size()) {
                    StringBuilder unlocalizedItems = new StringBuilder();
                    for (Ingredient ingredient : ingredients) {
                        unlocalizedItems.append(ingredient.getMatchingStacks()[0].getTranslationKey());
                    }
                    throw new InfoBookParser.InvalidAppendixException("Could not find MechanicalSqueezer recipe for "
                            + unlocalizedItems + " and "
                            + (fluidStack != null ? fluidStack.getFluid().getRegistryName() : "null")
                            + " with index " + index);
                }
                return new MechanicalSqueezerRecipeAppendix(infoBook, recipes.get(index));
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":mechanical_squeezer_recipe", new InfoBookParser.IAppendixItemFactory<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe) throws InfoBookParser.InvalidAppendixException {
                return new MechanicalSqueezerRecipeAppendix(infoBook, recipe);
            }

        });
        InfoBookParser.registerFactory(Reference.MOD_ID + ":mechanical_drying_basin_recipe", new InfoBookParser.IAppendixFactory() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                ItemStack itemStack = InfoBookParser.createOptionalStackFromIngredient(node, infoBook.getMod().getRecipeHandler());
                FluidStack fluidStack = InfoBookParser.createOptionalFluidStackFromIngredient(node, infoBook.getMod().getRecipeHandler());

                List<IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>>
                        recipes = RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.getRecipeRegistry().
                        findRecipesByOutput(new IngredientAndFluidStackRecipeComponent(itemStack, fluidStack));
                int index = InfoBookParser.getIndex(node);
                if(index >= recipes.size()) {
                    throw new InfoBookParser.InvalidAppendixException("Could not find Drying Basin recipe for "
                            + itemStack.getItem().getTranslationKey() + "with index " + index);
                }
                return new MechanicalDryingBasinRecipeAppendix(infoBook, recipes.get(index));
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":mechanical_drying_basin_recipe", new InfoBookParser.IAppendixItemFactory<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>() {

            @Override
            public SectionAppendix create(IInfoBook infoBook, IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) throws InfoBookParser.InvalidAppendixException {
                return new MechanicalDryingBasinRecipeAppendix(infoBook, recipe);
            }

        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":aspect", new InfoBookParser.IAppendixFactory() {
            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                String aspectName = node.getTextContent();
                IAspect aspect = Aspects.REGISTRY.getAspect(new ResourceLocation(aspectName));
                if (aspect == null) {
                    throw new InfoBookParser.InvalidAppendixException(String.format("Could not find an aspect by name %s.", aspectName));
                }
                return new AspectAppendix(infoBook, aspect);
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":part_aspects", new InfoBookParser.IAppendixListFactory() {
            @Override
            public List<SectionAppendix> create(final IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                String partName = node.getTextContent();
                IPartType partType = PartTypes.REGISTRY.getPartType(new ResourceLocation(partName));
                if (partType == null) {
                    throw new InfoBookParser.InvalidAppendixException(String.format("Could not find a part type by name '%s'.", partName));
                }
                List<IAspect> aspects = Lists.newArrayList(Aspects.REGISTRY.getAspects(partType));
                return Lists.transform(aspects, new Function<IAspect, SectionAppendix>() {
                    @Nullable
                    @Override
                    public SectionAppendix apply(IAspect input) {
                        return new AspectAppendix(infoBook, input);
                    }
                });
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":operator", new InfoBookParser.IAppendixFactory() {
            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                String operatorName = node.getTextContent();
                IOperator operator = Operators.REGISTRY.getOperator(new ResourceLocation(operatorName));
                if (operator == null) {
                    throw new InfoBookParser.InvalidAppendixException(String.format("Could not find an operator by name %s.", operator));
                }
                return new OperatorAppendix(infoBook, operator);
            }
        });

        InfoBookParser.registerFactory(Reference.MOD_ID + ":operators_output", new InfoBookParser.IAppendixListFactory() {
            @Override
            public List<SectionAppendix> create(final IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                String categoryName = node.getTextContent();
                List<IOperator> operators = Lists.newArrayList("*".equals(categoryName) ? Operators.REGISTRY.getOperators() : Operators.REGISTRY.getOperatorsInCategory(categoryName));
                return Lists.transform(operators, new Function<IOperator, SectionAppendix>() {
                    @Nullable
                    @Override
                    public SectionAppendix apply(IOperator input) {
                        return new OperatorAppendix(infoBook, input);
                    }
                });
            }
        });
    }

    private OnTheDynamicsOfIntegrationBook() {
        super(IntegratedDynamics._instance, 2, Reference.BOOK_URL);
    }

    public static OnTheDynamicsOfIntegrationBook getInstance() {
        if(_instance == null) {
            _instance = new OnTheDynamicsOfIntegrationBook();
        }
        return _instance;
    }
}
