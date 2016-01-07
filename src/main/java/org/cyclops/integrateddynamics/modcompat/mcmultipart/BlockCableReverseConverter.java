package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IPartConverter;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.Collection;

/**
 * Converter for the multipart form to its original cable block.
 * @author rubensworks
 */
public class BlockCableReverseConverter implements IPartConverter.IReversePartConverter {

    @Override
    public boolean convertToBlock(IMultipartContainer multipartContainer) {
        Collection<? extends IMultipart> parts = multipartContainer.getParts();
        for(IMultipart part : parts) {
            if(!(part instanceof PartCable)) { // TODO: also check attached parts
                return false;
            }
        }
        if(parts.size() > 0) {
            for(IMultipart part : parts) {
                if(part instanceof PartCable) {
                    multipartContainer.getWorldIn().setBlockState(multipartContainer.getPosIn(), BlockCable.getInstance().getDefaultState());
                }
            }
            // TODO: also convert attached parts
            return true;
        }
        return false;
    }
}
