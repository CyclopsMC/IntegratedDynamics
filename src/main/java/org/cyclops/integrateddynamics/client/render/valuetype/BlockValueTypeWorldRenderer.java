package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;

import java.util.Optional;

/**
 * A value type world renderer for blocks.
 * @author rubensworks
 */
public class BlockValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Optional<BlockState> blockOptional = ((ValueObjectTypeBlock.ValueBlock) value).getRawValue();
        if(blockOptional.isPresent()) {
            // ItemStack
            ItemStack itemStack = BlockHelpers.getItemStackFromBlockState(blockOptional.get());
            if(!itemStack.isEmpty()) {
                ItemValueTypeWorldRenderer.renderItemStack(matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, itemStack, alpha);
            }
        }
    }
}
