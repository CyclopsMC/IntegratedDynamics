package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.DisplayPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A value type world renderer for blocks.
 * @author rubensworks
 */
public class IngredientsValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Optional<IMixedIngredients> ingredientsOptional = ((ValueObjectTypeIngredients.ValueIngredients) value).getRawValue();
        if(ingredientsOptional.isPresent()) {
            IMixedIngredients ingredients = ingredientsOptional.get();

            // Get a list of all values
            List<IValue> values = Lists.newArrayList();
            for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
                IIngredientComponentHandler componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                for (Object instance : ingredients.getInstances(component)) {
                    values.add(componentHandler.toValue(instance));
                }
            }

            // Render ingredients in a square matrix
            renderGrid(context, partContainer, direction, partType, values, partialTicks,
                    matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, alpha);
        }
    }

    public static void renderGrid(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                                  Direction direction, IPartType partType, List<IValue> values, float partialTicks,
                                  PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                                  int combinedLight, int combinedOverlay, float alpha) {
        matrixStack.pushPose();
        int matrixRadius = getSmallestSquareFrom(values.size());
        float scale = 1F / matrixRadius;
        matrixStack.scale(scale, scale, 1);
        for (int i = 0; i < matrixRadius; i++) {
            for (int j = 0; j < matrixRadius; j++) {
                int realIndex = i * matrixRadius + j;
                if (realIndex >= values.size()) {
                    break;
                }
                IValue renderValue = values.get(realIndex);
                if (renderValue != null) {
                    matrixStack.pushPose();
                    matrixStack.translate(j * DisplayPartOverlayRenderer.MAX, i * DisplayPartOverlayRenderer.MAX, 0);

                    // Call value renderer for each value
                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(renderValue.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }
                    renderer.renderValue(context, partContainer, direction, partType, renderValue,
                            partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, alpha);
                    matrixStack.popPose();
                }
            }
        }
        matrixStack.popPose();
    }

    @Nullable
    protected static <T> T prepareElementForTick(List<T> elements, int tick, Supplier<T> defaultFactory) {
        return elements.size() > 0 ? elements.get(tick % elements.size()) : defaultFactory.get();
    }

    protected static int getSmallestSquareFrom(int n) {
        for (;!isInt(Math.sqrt(n));n++);
        return (int) Math.sqrt(n);
    }

    protected static final boolean isInt(double n) {
        return n == Math.floor(n) && !Double.isInfinite(n);
    }
}
