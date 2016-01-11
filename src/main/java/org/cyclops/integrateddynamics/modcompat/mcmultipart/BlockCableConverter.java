package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartConverter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Converter for the original cable block to its multipart form.
 * @author rubensworks
 */
public class BlockCableConverter implements IPartConverter.IPartConverter2 {
    @Override
    public Collection<Block> getConvertableBlocks() {
        return Collections.<Block>singleton(BlockCable.getInstance());
    }

    @Override
    public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos blockPos, boolean simulate) {
        Collection<IMultipart> parts = Lists.newLinkedList();
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, blockPos, TileMultipartTicking.class);

        // Add parts
        Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData = Maps.newHashMap(tile.getPartData());
        for(Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
            parts.add(new PartPartType(entry.getKey(), entry.getValue().getPart()));
        }
        if(!simulate) {
            tile.silentResetPartData();
        }

        // Add cable
        if(tile.isRealCable()) {
            PartCable partCable = new PartCable(partData);
            parts.add(partCable);
        }

        // Optionally drop facade
        if(!simulate && tile.hasFacade()) {
            IBlockState blockState = tile.getFacade();
            ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
            ItemFacade.getInstance().writeFacadeBlock(itemStack, blockState);
            ItemStackHelpers.spawnItemStack(tile.getWorld(), blockPos, itemStack);
        }
        return parts;
    }
}
