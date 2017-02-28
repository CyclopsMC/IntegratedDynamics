package org.cyclops.integrateddynamics.part.aspect.write.redstone;

import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class WriteRedstoneComponent implements IWriteRedstoneComponent {
    @Override
    public void setRedstoneLevel(PartTarget target, int level, boolean strongPower) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstone block = getDynamicRedstoneBlock(dimPos, target.getCenter().getSide());
        if(block != null) {
            block.setRedstoneLevel(level, strongPower);
        }
    }

    @Override
    public void deactivate(PartTarget target) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstone block = getDynamicRedstoneBlock(dimPos, target.getCenter().getSide());
        if(block != null && !dimPos.getWorld().isRemote) {
            block.setRedstoneLevel(-1, false);
        }
    }

    @Override
    public IDynamicRedstone getDynamicRedstoneBlock(DimPos dimPos, EnumFacing side) {
        return TileHelpers.getCapability(dimPos, side, DynamicRedstoneConfig.CAPABILITY);
    }
}
