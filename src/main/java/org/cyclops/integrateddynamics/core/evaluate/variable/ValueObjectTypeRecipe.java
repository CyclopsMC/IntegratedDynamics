package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderProperties;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
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
    public MutableComponent toCompactString(ValueRecipe value) {
        if (value.getRawValue().isPresent()) {
            IRecipeDefinition recipe = value.getRawValue().get();
            MutableComponent sb = Component.literal("");

            sb.append(ValueObjectTypeIngredients.ingredientsToTextComponent(recipe.getOutput()));
            sb.append(Component.literal(" <- "));
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
                        sb.append(Component.literal(", "));
                    } else {
                        first = false;
                    }
                    sb.append(handler.toCompactString(v));
                }
            }
            return sb;
        }
        return Component.literal("");
    }

    @Override
    public Tag serialize(ValueRecipe value) {
        if(!value.getRawValue().isPresent()) return new CompoundTag();
        return IRecipeDefinition.serialize(value.getRawValue().get());
    }

    @Override
    public ValueRecipe deserialize(Tag value) {
        if (value.getId() == Tag.TAG_END || (value.getId() == Tag.TAG_COMPOUND && ((CompoundTag) value).isEmpty())) {
            return ValueRecipe.of(null);
        }
        try {
            return ValueRecipe.of(IRecipeDefinition.deserialize((CompoundTag) value));
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
    public BakedModel getVariableItemOverrideModel(ValueRecipe value, BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity) {
        if (!MinecraftHelpers.isShifted()) {
            return null;
        }
        return value.getRawValue()
                .map((recipe) -> {
                    List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                    if (!itemStacks.isEmpty()) {
                        return Minecraft.getInstance().getItemRenderer().getModel(itemStacks.get(0), world, livingEntity, 0);
                    }
                    return null;
                })
                .orElse(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderISTER(ValueRecipe value, ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (MinecraftHelpers.isShifted()) {
            value.getRawValue()
                    .ifPresent((recipe) -> {
                        List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                        if (!itemStacks.isEmpty()) {
                            ItemStack actualStack = itemStacks.get(0);
                            RenderProperties.get(actualStack.getItem()).getItemStackRenderer()
                                    .renderByItem(actualStack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
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
