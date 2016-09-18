package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartConverter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
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
public class BlockCableConverter implements IPartConverter {
    @Override
    public Collection<Block> getConvertableBlocks() {
        return Collections.<Block>singleton(BlockCable.getInstance());
    }

    @Override
    public Collection<? extends IMultipart> convertBlock(IBlockAccess world, BlockPos blockPos, boolean simulate) {
        Collection<IMultipart> parts = Lists.newLinkedList();
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, blockPos, TileMultipartTicking.class);

        // Add parts
        EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData = EnumFacingMap.newMap(tile.getPartContainer().getPartData());
        EnumFacingMap<Boolean> forceDisconnected = EnumFacingMap.newMap(tile.getForceDisconnected());
        for(Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
            parts.add(new PartPartType(entry.getKey(), entry.getValue().getPart()));
        }
        boolean wasRealCable = tile.getCableFakeable().isRealCable();
        INetwork network = null;
        if(!simulate) {
            tile.getPartContainer().silentResetPartData();
            network = tile.getNetwork();
            tile.getNetworkCarrier().setNetwork(null);
            tile.getCableFakeable().setRealCable(false);
            BlockCable.IS_MCMP_CONVERTING = true;
        }

        // Add cable
        if(wasRealCable) {
            PartCable partCable = new PartCable(partData, forceDisconnected);
            if(!simulate) {
                partCable.getNetworkCarrier().setNetwork(network);
            }
            partCable.setAddSilent(true);
            parts.add(partCable);
        }

        // Optionally drop facade
        IFacadeable facadeable = tile.hasCapability(FacadeableConfig.CAPABILITY, null)
                ? tile.getCapability(FacadeableConfig.CAPABILITY, null) : null;
        if(facadeable != null && !simulate && facadeable.hasFacade()) {
            IBlockState blockState = facadeable.getFacade();
            ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
            ItemFacade.getInstance().writeFacadeBlock(itemStack, blockState);
            ItemStackHelpers.spawnItemStack(tile.getWorld(), blockPos, itemStack);
        }
        return parts;
    }
}
