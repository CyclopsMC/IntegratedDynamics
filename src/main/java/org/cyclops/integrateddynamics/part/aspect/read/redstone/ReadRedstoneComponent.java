package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.capability.DynamicRedstoneConfig;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class ReadRedstoneComponent implements IReadRedstoneComponent {
    @Override
    public void setAllowRedstoneInput(PartTarget target, boolean allow) {
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstone block = getDynamicRedstoneBlock(dimPos, target.getCenter().getSide());
        if(block != null) {
            block.setAllowRedstoneInput(allow);
        }
    }

    @Override
    public IDynamicRedstone getDynamicRedstoneBlock(DimPos dimPos, EnumFacing side) {
        return TileHelpers.getCapability(dimPos, side, DynamicRedstoneConfig.CAPABILITY);
    }
}
