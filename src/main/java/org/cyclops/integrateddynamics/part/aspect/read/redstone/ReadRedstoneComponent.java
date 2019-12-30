package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class ReadRedstoneComponent implements IReadRedstoneComponent {
    @Override
    public void setAllowRedstoneInput(PartTarget target, boolean allow) {
        DimPos dimPos = target.getCenter().getPos();
        getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                .ifPresent(block -> block.setAllowRedstoneInput(allow));
    }

    @Override
    public LazyOptional<IDynamicRedstone> getDynamicRedstoneBlock(DimPos dimPos, Direction side) {
        return TileHelpers.getCapability(dimPos, side, DynamicRedstoneConfig.CAPABILITY);
    }
}
