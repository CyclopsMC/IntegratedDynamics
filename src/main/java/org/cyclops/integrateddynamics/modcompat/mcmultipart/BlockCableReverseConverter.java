package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Maps;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IPartConverter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Collection;
import java.util.Map;

/**
 * Converter for the multipart form to its original cable block.
 * @author rubensworks
 */
public class BlockCableReverseConverter implements IPartConverter.IReversePartConverter {

    @Override
    public boolean convertToBlock(IMultipartContainer multipartContainer) {
        Collection<? extends IMultipart> parts = multipartContainer.getParts();
        for(IMultipart part : parts) {
            if(!(part instanceof PartCable) && !(part instanceof PartPartType)) {
                return false;
            }
        }
        if(parts.size() > 0) {
            Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData = null;
            boolean hasParts = false;
            for(IMultipart part : parts) {
                if(part instanceof PartCable) {
                    partData = ((PartCable) part).getPartData();
                } else if(part instanceof PartPartType) {
                    hasParts = true;
                }
            }
            // Check
            World world = multipartContainer.getWorldIn();
            BlockPos pos = multipartContainer.getPosIn();
            world.setBlockState(pos, BlockCable.getInstance().getDefaultState());
            TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
            if(partData == null) {
                // The cable is not real
                // We now have to check manually for disabled parts
                tile.setRealCable(false);
                partData = Maps.newHashMap();
                if(hasParts) {
                    for (IMultipart part : parts) {
                        if (part instanceof PartPartType) {
                            PartPartType partPart = (PartPartType) part;
                            partData.put(partPart.getFacing(), PartHelpers.PartStateHolder.of(partPart.getPartType(), partPart.getPartType().getDefaultState()));
                        }
                    }
                }
            }
            tile.setPartData(partData);
            tile.sendImmediateUpdate();
            return true;
        }
        return false;
    }
}
