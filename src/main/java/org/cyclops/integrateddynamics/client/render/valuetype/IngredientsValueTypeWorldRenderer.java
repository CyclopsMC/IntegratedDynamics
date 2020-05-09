package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.DisplayPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A value type world renderer for blocks.
 * @author rubensworks
 */
public class IngredientsValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float alpha) {
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
            renderGrid(partContainer, x, y, z, partialTick, destroyStage, direction, partType, values, rendererDispatcher, alpha);
        }
    }

    public static void renderGrid(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                  int destroyStage, EnumFacing direction, IPartType partType, List<IValue> values,
                                  TileEntityRendererDispatcher rendererDispatcher, float alpha) {
        GlStateManager.pushMatrix();
        int matrixRadius = getSmallestSquareFrom(values.size());
        double scale = (double) 1 / matrixRadius;
        GlStateManager.scale(scale, scale, 1);
        for (int i = 0; i < matrixRadius; i++) {
            for (int j = 0; j < matrixRadius; j++) {
                int realIndex = i * matrixRadius + j;
                if (realIndex >= values.size()) {
                    break;
                }
                IValue renderValue = values.get(realIndex);
                if (renderValue != null) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(j * DisplayPartOverlayRenderer.MAX, i * DisplayPartOverlayRenderer.MAX, 0);

                    if (renderValue instanceof ValueObjectTypeItemStack.ValueItemStack
                            && ((ValueObjectTypeItemStack.ValueItemStack) renderValue).getRawValue().getMetadata()
                            == OreDictionary.WILDCARD_VALUE) {
                        NonNullList<ItemStack> subItems = ItemStackHelpers.getSubItems(
                                ((ValueObjectTypeItemStack.ValueItemStack) renderValue).getRawValue());
                        int subtick = ((int) Minecraft.getMinecraft().world.getWorldTime()) / 10;
                        ItemStack itemStack = prepareElementForTick(subItems, subtick, () -> ItemStack.EMPTY);
                        renderValue = ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                    }

                    // Call value renderer for each value
                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(renderValue.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }
                    renderer.renderValue(partContainer, x, y, z, partialTick,
                            destroyStage, direction, partType, renderValue, rendererDispatcher, alpha);
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
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
