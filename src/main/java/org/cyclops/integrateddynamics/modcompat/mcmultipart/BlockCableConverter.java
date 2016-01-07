package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartConverter;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.Collection;
import java.util.Collections;

/**
 * Converter for the original cable block to its multipart form.
 * @author rubensworks
 */
public class BlockCableConverter implements IPartConverter {
    @Override
    public Collection<Block> getConvertableBlocks() {
        return Collections.<Block>singleton(BlockCable.getInstance());
    }

    @Override
    public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos blockPos) {
        Collection<IMultipart> parts = Lists.newLinkedList();
        parts.add(new PartCable());
        // TODO: also add attached parts
        return parts;
    }
}
