package org.cyclops.integrateddynamics.part.aspect.write.redstone;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneHolderGlobal;

/**
 * Default component for writing redstone levels.
 * @author rubensworks
 */
public class WriteRedstoneComponent implements IWriteRedstoneComponent {

    public IDynamicRedstone getGlobalDynamicRedstone(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        Direction side = target.getTarget().getSide();
        // We offset one block in the opposite direction of the target side, so that we output the redstone towards the target position
        return DynamicRedstoneHolderGlobal.getInstance().getDynamicRedstone(dimPos.withPosition(dimPos.getBlockPos().relative(side)), side);
    }

    @Override
    public void setRedstoneLevel(PartTarget target, int level, boolean strongPower) {
        if (target.isDefaultTarget()) {
            DimPos dimPos = target.getCenter().getPos();
            getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                    .ifPresent(block -> block.setRedstoneLevel(level, strongPower));
        } else {
            getGlobalDynamicRedstone(target).setRedstoneLevel(level, strongPower);
        }
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
        if (target.isDefaultTarget()) {
            DimPos dimPos = target.getCenter().getPos();
            getDynamicRedstoneBlock(dimPos, target.getCenter().getSide())
                    .ifPresent(block -> block.setRedstoneLevel(-1, block.isDirect()));
        } else {
            IDynamicRedstone block = getGlobalDynamicRedstone(target);
            block.setRedstoneLevel(-1, block.isDirect());
        }
    }

    @Override
    public LazyOptional<IDynamicRedstone> getDynamicRedstoneBlock(DimPos dimPos, Direction side) {
        return BlockEntityHelpers.getCapability(dimPos, side, DynamicRedstoneConfig.CAPABILITY);
    }
}
