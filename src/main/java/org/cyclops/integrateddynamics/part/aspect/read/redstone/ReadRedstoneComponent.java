package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import net.minecraft.core.Direction;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;

import java.util.Optional;

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
    public Optional<IDynamicRedstone> getDynamicRedstoneBlock(DimPos dimPos, Direction side) {
        return BlockEntityHelpers.getCapability(dimPos, side, Capabilities.DynamicRedstone.BLOCK);
    }
}
