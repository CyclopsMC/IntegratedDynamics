package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.DisplayPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;

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
        Optional<IIngredients> ingredientsOptional = ((ValueObjectTypeIngredients.ValueIngredients) value).getRawValue();
        if(ingredientsOptional.isPresent()) {
            IIngredients ingredients = ingredientsOptional.get();

            // Get a list of all values
            List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks = ingredients.getItemStacksRaw();
            List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks = ingredients.getFluidStacksRaw();
            List<List<ValueTypeInteger.ValueInteger>> energies = ingredients.getEnergiesRaw();
            int ingredientCount = itemStacks.size() + fluidStacks.size() + energies.size();
            List<IValue> values = Lists.newArrayListWithExpectedSize(ingredientCount);

            // For ingredients with multiple possibilities, vary them based on the current tick
            int tick = ((int) Minecraft.getMinecraft().world.getWorldTime()) / 30;
            itemStacks.forEach(itemStack -> values.add(prepareElementForTick(itemStack, tick)));
            fluidStacks.forEach(fluidStack -> values.add(prepareElementForTick(fluidStack, tick)));
            energies.forEach(energy -> values.add(prepareElementForTick(energy, tick)));

            // Render ingredients in a square matrix
            GlStateManager.pushMatrix();
            int matrixRadius = getSmallestSquareFrom(ingredientCount);
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
    }

    @Nullable
    protected <T> T prepareElementForTick(List<T> elements, int tick) {
        return elements.size() > 0 ? elements.get(tick % elements.size()) : null;
    }

    private static int getSmallestSquareFrom(int n) {
        for (;!isInt(Math.sqrt(n));n++);
        return (int) Math.sqrt(n);
    }

    private static final boolean isInt(double n) {
        return n == Math.floor(n) && !Double.isInfinite(n);
    }
}
