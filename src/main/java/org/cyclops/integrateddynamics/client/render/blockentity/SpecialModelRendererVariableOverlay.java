package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

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
    public void render(@Nullable Pair<Boolean, ItemStack> stateIn, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (displayContext == ItemDisplayContext.GUI) {
            IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.ofClient(), stateIn.getRight());
            displayContext = ItemDisplayContext.GUI;
            poseStack.translate(0.5F, 0.5F, 0.7F);
            variableFacade.getClient().renderISTER(stateIn.getRight(), displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    @Override
    public void getExtents(Set<Vector3f> p_428206_) {

    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<SpecialModelRendererVariableOverlay.Unbaked> MAP_CODEC = MapCodec.unit(SpecialModelRendererVariableOverlay.Unbaked::new);

        @Override
        public MapCodec<SpecialModelRendererVariableOverlay.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new SpecialModelRendererVariableOverlay();
        }
    }
}
