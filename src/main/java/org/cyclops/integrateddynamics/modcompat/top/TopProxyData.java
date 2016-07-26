package org.cyclops.integrateddynamics.modcompat.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Data provider for proxies.
 * @author rubensworks
 */
public class TopProxyData implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Reference.MOD_ID + ":proxyData";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (world != null && blockState != null && data != null && player != null) {
            TileProxy tile = TileHelpers.getSafeTile(world, data.getPos(), TileProxy.class);
            if (tile != null) {
                probeInfo.text(L10NHelpers.localize(L10NValues.GENERAL_ITEM_ID, tile.getProxyId()));
            }
        }
    }
}
