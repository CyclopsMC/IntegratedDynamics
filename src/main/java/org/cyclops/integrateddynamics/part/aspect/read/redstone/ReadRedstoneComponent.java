package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class ReadRedstoneComponent implements IReadRedstoneComponent {
    @Override
    public void setAllowRedstoneInput(PartTarget target, boolean allow) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstoneBlock block = getDynamicRedstoneBlock(dimPos);
        if(block != null) {
            block.setAllowRedstoneInput(dimPos.getWorld(), dimPos.getBlockPos(), target.getCenter().getSide(), allow);
        }
    }

    @Override
    public IDynamicRedstoneBlock getDynamicRedstoneBlock(DimPos dimPos) {
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        if(block instanceof IDynamicRedstoneBlock) {
            return (IDynamicRedstoneBlock) block;
        }
        return null;
    }
}
