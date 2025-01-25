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

/**
 * @author rubensworks
 */
public class ItemStackBlockEntityVariableRender implements SpecialModelRenderer<ItemStack> {

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void render(@Nullable ItemStack itemStackIn, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.ofClient(), itemStackIn);
        variableFacade.renderISTER(itemStackIn, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ItemStackBlockEntityVariableRender.Unbaked> MAP_CODEC = MapCodec.unit(ItemStackBlockEntityVariableRender.Unbaked::new);

        @Override
        public MapCodec<ItemStackBlockEntityVariableRender.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new ItemStackBlockEntityVariableRender();
        }
    }
}
