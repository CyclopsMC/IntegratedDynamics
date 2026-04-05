package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * @author rubensworks
 */
public class SpecialModelRendererVariableOverlay implements SpecialModelRenderer<Pair<Boolean, ItemStack>> {

    @Override
    public @Nullable Pair<Boolean, ItemStack> extractArgument(ItemStack stack) {
        // Store shift state to force re-rendering when shift is held/unheld.
        return Pair.of(IModHelpers.get().getMinecraftClientHelpers().isShifted(), stack);
    }

    @Override
    public void submit(@Nullable Pair<Boolean, ItemStack> stateIn, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.ofClient(), stateIn.getRight());
        poseStack.translate(0.5F, 0.5F, 0.7F);
        variableFacade.getClient().renderISTER(stateIn.getRight(), ItemDisplayContext.GUI, poseStack, submitNodeCollector, lightCoords, overlayCoords);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {

    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked<Pair<Boolean, ItemStack>> {
        public static final MapCodec<SpecialModelRendererVariableOverlay.Unbaked> MAP_CODEC = MapCodec.unit(SpecialModelRendererVariableOverlay.Unbaked::new);

        @Override
        public MapCodec<SpecialModelRendererVariableOverlay.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Pair<Boolean, ItemStack>> bake(BakingContext bakingContext) {
            return new SpecialModelRendererVariableOverlay();
        }
    }
}
