package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityBakedModel;
import org.cyclops.integrateddynamics.block.Reader;
import org.cyclops.integrateddynamics.block.Writer;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Map;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable extends RenderTileEntityBakedModel<TileMultipartTicking> {

    private IBlockState tempBlockState;

    protected void renderTileEntityAt(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                      int destroyStage) {
        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : tile.getParts().entrySet()) {
            // TODO: improve model type retrieval
            if(entry.getValue().getType() == PartTypes.REDSTONE_READER) {
                tempBlockState = Reader.getInstance().getDefaultState().withProperty(Reader.FACING, entry.getKey());
            } else if(entry.getValue().getType() == PartTypes.REDSTONE_WRITER) {
                tempBlockState = Writer.getInstance().getDefaultState().withProperty(Reader.FACING, entry.getKey());
            }
            super.renderTileEntityAt(tile, x, y, z, partialTick, destroyStage);
        }
    }

    @Override
    protected IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                        int destroyStage) {
        return tempBlockState;
    }

}
