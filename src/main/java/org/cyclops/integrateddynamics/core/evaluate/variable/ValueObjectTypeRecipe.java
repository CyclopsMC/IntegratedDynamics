package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Value type with values that are recipes.
 * @author rubensworks
 */
public class ValueObjectTypeRecipe extends ValueObjectTypeBase<ValueObjectTypeRecipe.ValueRecipe> implements
        IValueTypeNamed<ValueObjectTypeRecipe.ValueRecipe>, IValueTypeNullable<ValueObjectTypeRecipe.ValueRecipe> {

    public ValueObjectTypeRecipe() {
        super("recipe", ValueObjectTypeRecipe.ValueRecipe.class);
    }

    @Override
    public ValueRecipe getDefault() {
        return ValueRecipe.of(null);
    }

    @Override
    public IFormattableTextComponent toCompactString(ValueRecipe value) {
        if (value.getRawValue().isPresent()) {
            IRecipeDefinition recipe = value.getRawValue().get();
            IFormattableTextComponent sb = new StringTextComponent("");

            sb.append(ValueObjectTypeIngredients.ingredientsToTextComponent(recipe.getOutput()));
            sb.append(new StringTextComponent(" <- "));
            boolean first = true;

            for (IngredientComponent<?, ?> component : recipe.getInputComponents()) {
                IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                for (IPrototypedIngredientAlternatives<?, ?> instances : recipe.getInputs(component)) {
                    IPrototypedIngredient<?, ?> prototypedIngredient = Iterables.getFirst(instances.getAlternatives(), null);
                    IValue v;
                    if (prototypedIngredient == null) {
                        v  = handler.getValueType().getDefault();
                    } else {
                        v = handler.toValue(prototypedIngredient.getPrototype());
                    }
                    if (!first) {
                        sb.append(new StringTextComponent(", "));
                    } else {
                        first = false;
                    }
                    sb.append(handler.toCompactString(v));
                }
            }
            return sb;
        }
        return new StringTextComponent("");
    }

    @Override
    public INBT serialize(ValueRecipe value) {
        if(!value.getRawValue().isPresent()) return new CompoundNBT();
        return IRecipeDefinition.serialize(value.getRawValue().get());
    }

    @Override
    public ValueRecipe deserialize(INBT value) {
        if (value.getId() == Constants.NBT.TAG_END || (value.getId() == Constants.NBT.TAG_COMPOUND && ((CompoundNBT) value).isEmpty())) {
            return ValueRecipe.of(null);
        }
        try {
            return ValueRecipe.of(IRecipeDefinition.deserialize((CompoundNBT) value));
        } catch (IllegalArgumentException e) {
            return ValueRecipe.of(null);
        }
    }

    @Override
    public String getName(ValueRecipe a) {
        return toCompactString(a).getString();
    }

    @Override
    public boolean isNull(ValueRecipe a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeRecipeLPElement();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getVariableItemOverrideModel(ValueRecipe value, IBakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity) {
        if (!MinecraftHelpers.isShifted()) {
            return null;
        }
        return value.getRawValue()
                .map((recipe) -> {
                    List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                    if (!itemStacks.isEmpty()) {
                        return Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(itemStacks.get(0), world, livingEntity);
                    }
                    return null;
                })
                .orElse(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderISTER(ValueRecipe value, ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (MinecraftHelpers.isShifted()) {
            value.getRawValue()
                    .ifPresent((recipe) -> {
                        List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                        if (!itemStacks.isEmpty()) {
                            ItemStack actualStack = itemStacks.get(0);
                            actualStack.getItem().getItemStackTileEntityRenderer().func_239207_a_(actualStack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
                        }
                    });
        }
    }

    @ToString
    public static class ValueRecipe extends ValueOptionalBase<IRecipeDefinition> {

        private ValueRecipe(IRecipeDefinition recipe) {
            super(ValueTypes.OBJECT_RECIPE, recipe);
        }

        public static ValueRecipe of(IRecipeDefinition recipe) {
            return new ValueRecipe(recipe);
        }

        @Override
        protected boolean isEqual(IRecipeDefinition a, IRecipeDefinition b) {
            return a.equals(b);
        }
    }

}
