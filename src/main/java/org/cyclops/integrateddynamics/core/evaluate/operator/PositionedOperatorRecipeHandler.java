package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An operator related to a recipe handler.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandler<T extends IValueType<V>, V extends IValue> extends PositionedOperator {

    private final String unlocalizedType;

    public PositionedOperatorRecipeHandler(String name, Function function, IValueType output, DimPos pos, EnumFacing side) {
        super(name, name, new IValueType[]{ValueTypes.OBJECT_INGREDIENTS},
                output, function, IConfigRenderPattern.PREFIX_1, pos, side);
        this.unlocalizedType = "virtual";
        ((Function) this.getFunction()).setOperator(this);
    }

    public PositionedOperatorRecipeHandler(String name, Function function, DimPos pos, EnumFacing side) {
        super(name, name, new IValueType[]{ValueTypes.OBJECT_INGREDIENTS},
                ValueTypes.OBJECT_INGREDIENTS, function, IConfigRenderPattern.PREFIX_1, pos, side);
        this.unlocalizedType = "virtual";
        ((Function) this.getFunction()).setOperator(this);
    }

    @Nullable
    protected IRecipeHandler getRecipeHandler() {
        return Helpers.getTileOrBlockCapability(getPos().getWorld(), getPos().getBlockPos(), getSide(),
                Capabilities.RECIPE_HANDLER);
    }

    @Override
    protected String getUnlocalizedType() {
        return unlocalizedType;
    }

    @Override
    public IOperator materialize() {
        return this;
    }

    public static abstract class Function implements IFunction {

        private PositionedOperatorRecipeHandler operator;

        public void setOperator(PositionedOperatorRecipeHandler operator) {
            this.operator = operator;
        }

        public PositionedOperatorRecipeHandler getOperator() {
            return operator;
        }
    }

    public static boolean validateIngredientsExact(IMixedIngredients ingredients, IMixedIngredients givenIngredients) {
        for (IngredientComponent component : ingredients.getComponents()) {
            List<?> actualComponents = ingredients.getInstances(component);
            List<?> givenComponents = givenIngredients.getInstances(component);

            if (actualComponents.size() != givenComponents.size()) {
                return false;
            }

            // All components must be valid
            for (int i = 0; i < actualComponents.size(); i++) {
                Object actualIngredient = actualComponents.get(i);
                Object givenIngredient = givenComponents.get(i);
                if (!component.getMatcher().matchesExactly(givenIngredient, actualIngredient)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean validateIngredientsPartial(IMixedIngredients ingredients, IMixedIngredients givenIngredients) {
        for (IngredientComponent component : ingredients.getComponents()) {
            List<?> actualComponents = ingredients.getInstances(component);
            List<?> givenComponents = givenIngredients.getInstances(component);

            // At least all given components must match,
            // the actual component count may be larger.
            if (actualComponents.size() < givenComponents.size()) {
                return false;
            }

            // All GIVEN ingredients must match,
            // and all actual components may only be matched at most ONCE.
            List<Integer> actualIndexBlacklist = Lists.newLinkedList();
            for (Object givenIngredient : givenComponents) {
                boolean match = false;
                for (int i = 0; i < actualComponents.size(); i++) {
                    if (!actualIndexBlacklist.contains(i)
                            && component.getMatcher().matchesExactly(givenIngredient, actualComponents.get(i))) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    return false;
                }
            }
        }
        return true;
    }

}
