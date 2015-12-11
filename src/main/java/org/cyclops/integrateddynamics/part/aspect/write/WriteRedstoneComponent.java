package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class WriteRedstoneComponent implements IWriteRedstoneComponent {
    @Override
    public void setRedstoneLevel(PartTarget target, int level) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstoneBlock block = getDynamicRedstoneBlock(dimPos);
        if(block != null) {
            block.setRedstoneLevel(dimPos.getWorld(), dimPos.getBlockPos(), target.getCenter().getSide(), level);
        }
    }

    @Override
    public void deactivate(PartTarget target) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstoneBlock block = getDynamicRedstoneBlock(dimPos);
        if(block != null) {
            block.disableRedstoneAt(dimPos.getWorld(), dimPos.getBlockPos(), target.getCenter().getSide());
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
