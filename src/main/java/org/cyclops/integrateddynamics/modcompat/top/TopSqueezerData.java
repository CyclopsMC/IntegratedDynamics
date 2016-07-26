package org.cyclops.integrateddynamics.modcompat.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * Data provider for Squeezers.
 * @author rubensworks
 */
public class TopSqueezerData implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Reference.MOD_ID + ":squeezerData";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (world != null && blockState != null && data != null && player != null) {
            TileSqueezer tile = TileHelpers.getSafeTile(world, data.getPos(), TileSqueezer.class);
            if (tile != null) {
                if(tile.getStackInSlot(0) != null) {
                    probeInfo.item(tile.getStackInSlot(0));
                }
                if(!tile.getTank().isEmpty()) {
                    probeInfo.horizontal()
                            .text(tile.getTank().getFluid().getLocalizedName())
                            .progress(tile.getTank().getFluidAmount(), tile.getTank().getCapacity());
                }
            }
        }
    }
}
