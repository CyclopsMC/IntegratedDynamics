package org.cyclops.integrateddynamics.part.aspect.write.redstone;

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
public class WriteRedstoneComponent implements IWriteRedstoneComponent {
    @Override
    public void setRedstoneLevel(PartTarget target, int level, boolean strongPower) {
        DimPos dimPos = target.getCenter().getPos();
        getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                .ifPresent(block -> block.setRedstoneLevel(level, strongPower));
    }

    @Override
    public void setLastPulseValue(PartTarget target, int value) {
        DimPos dimPos = target.getCenter().getPos();
        getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                .ifPresent(block -> block.setLastPulseValue(value));
    }

    @Override
    public int getLastPulseValue(PartTarget target) {
        DimPos dimPos = target.getCenter().getPos();
        return getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                .map(block -> block.getLastPulseValue())
                .orElse(0);
    }

    @Override
    public void deactivate(PartTarget target) {
        DimPos dimPos = target.getCenter().getPos();
        getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                .ifPresent(block -> block.setRedstoneLevel(-1, block.isDirect()));
    }

    @Override
    public LazyOptional<IDynamicRedstone> getDynamicRedstoneBlock(DimPos dimPos, Direction side) {
        return TileHelpers.getCapability(dimPos, side, DynamicRedstoneConfig.CAPABILITY);
    }
}
