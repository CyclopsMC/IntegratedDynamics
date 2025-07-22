package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

/**
 * @author rubensworks
 */
public class SpecialModelRendererVariableOverlay implements SpecialModelRenderer<ItemStack> {

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void render(@Nullable ItemStack itemStackIn, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (displayContext == ItemDisplayContext.GUI) {
            IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.ofClient(), itemStackIn);
            displayContext = ItemDisplayContext.GUI;
            poseStack.translate(0.5F, 0.5F, 0.7F);
            variableFacade.getClient().renderISTER(itemStackIn, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
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
