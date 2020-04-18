package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.base.Optional;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;

/**
 * A value type world renderer for blocks.
 * @author rubensworks
 */
public class BlockValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
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
