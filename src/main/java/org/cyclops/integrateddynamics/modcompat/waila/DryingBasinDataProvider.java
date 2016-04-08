package org.cyclops.integrateddynamics.modcompat.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

import java.util.List;

/**
 * Waila data provider for the drying basin.
 * @author rubensworks
 *
 */
public class DryingBasinDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if(config.getConfig(Waila.getProxyConfigId())) {
            TileDryingBasin tile = TileHelpers.getSafeTile(accessor.getWorld(), accessor.getPosition(), TileDryingBasin.class);
            if (tile != null) {
                if(tile.getStackInSlot(0) != null) {
                    currenttip.add(L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.item",
                            tile.getStackInSlot(0).getDisplayName()));
                }
                if(!tile.getTank().isEmpty()) {
                    currenttip.add(L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.fluid",
                            tile.getTank().getFluid().getLocalizedName(), tile.getTank().getFluidAmount()));
                }
            }
        }
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        return tag;
    }

}
