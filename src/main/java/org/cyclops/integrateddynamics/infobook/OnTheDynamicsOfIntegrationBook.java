package org.cyclops.integrateddynamics.infobook;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.*;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockDryingBasinConfig;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.block.BlockSqueezerConfig;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.infobook.pageelement.AspectAppendix;
import org.cyclops.integrateddynamics.infobook.pageelement.DryingBasinRecipeAppendix;
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
        if (ConfigHandler.isEnabled(BlockSqueezerConfig.class)) {
            InfoBookParser.registerFactory(Reference.MOD_ID + ":squeezer_recipe", new InfoBookParser.IAppendixFactory() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                    NonNullList<Ingredient> ingredients = NonNullList.create();
                    String unlocalizedItems = "";
                    for (int i = 0; i < node.getElementsByTagName("item").getLength(); i++) {
                        try {
                            ItemStack itemStack = InfoBookParser.createStack((Element) node.getElementsByTagName("item").item(i), infoBook.getMod().getRecipeHandler());
                            unlocalizedItems += itemStack.getUnlocalizedName();
                            ingredients.add(Ingredient.fromStacks(itemStack));
                        } catch (InfoBookParser.InvalidAppendixException e) {

                        }
                    }
                    FluidStack fluidStack;
                    try {
                        fluidStack = InfoBookParser.createFluidStack((Element) node.getElementsByTagName("fluid").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        fluidStack = null;
                    }

                    List<IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DummyPropertiesComponent>>
                            recipes = BlockSqueezer.getInstance().getRecipeRegistry().
                            findRecipesByOutput(new IngredientsAndFluidStackRecipeComponent(ingredients, fluidStack));
                    int index = InfoBookParser.getIndex(node);
                    if(index >= recipes.size()) {
                        throw new InfoBookParser.InvalidAppendixException("Could not find Squeezer recipe for "
                                + unlocalizedItems + " and "
                                + (fluidStack != null ? fluidStack.getFluid().getName() : "null")
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
        } else {
            InfoBookParser.registerIgnoredFactory(Reference.MOD_ID + ":squeezer_recipe");
        }
        if (ConfigHandler.isEnabled(BlockDryingBasinConfig.class)) {
            InfoBookParser.registerFactory(Reference.MOD_ID + ":drying_basin_recipe", new InfoBookParser.IAppendixFactory() {

                @Override
                public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                    ItemStack itemStack;
                    try {
                        itemStack = InfoBookParser.createStack((Element) node.getElementsByTagName("item").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        itemStack = ItemStack.EMPTY;
                    }
                    FluidStack fluidStack;
                    try {
                        fluidStack = InfoBookParser.createFluidStack((Element) node.getElementsByTagName("fluid").item(0), infoBook.getMod().getRecipeHandler());
                    } catch (InfoBookParser.InvalidAppendixException e) {
                        fluidStack = null;
                    }

                    List<IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties>>
                            recipes = BlockDryingBasin.getInstance().getRecipeRegistry().
                            findRecipesByOutput(new IngredientAndFluidStackRecipeComponent(itemStack, fluidStack));
                    int index = InfoBookParser.getIndex(node);
                    if(index >= recipes.size()) {
                        throw new InfoBookParser.InvalidAppendixException("Could not find Drying Basin recipe for "
                                + itemStack.getItem().getUnlocalizedName() + "with index " + index);
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
        } else {
            InfoBookParser.registerIgnoredFactory(Reference.MOD_ID + ":drying_basin_recipe");
        }

        InfoBookParser.registerFactory(Reference.MOD_ID + ":aspect", new InfoBookParser.IAppendixFactory() {
            @Override
            public SectionAppendix create(IInfoBook infoBook, Element node) throws InfoBookParser.InvalidAppendixException {
                String aspectName = node.getTextContent();
                IAspect aspect = Aspects.REGISTRY.getAspect(aspectName);
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
                IPartType partType = PartTypes.REGISTRY.getPartType(partName);
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
                IOperator operator = Operators.REGISTRY.getOperator(operatorName);
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
        super(IntegratedDynamics._instance, 2);
    }

    public static OnTheDynamicsOfIntegrationBook getInstance() {
        if(_instance == null) {
            _instance = new OnTheDynamicsOfIntegrationBook();
        }
        return _instance;
    }
}
