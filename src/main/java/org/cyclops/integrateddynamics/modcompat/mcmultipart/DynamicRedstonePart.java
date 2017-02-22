package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Dynamic redstone for MCMP parts.
 * @author rubensworks
 */
public class DynamicRedstonePart implements IDynamicRedstone {

    private final PartCable partCable;

    public DynamicRedstonePart(PartCable partCable) {
        this.partCable = partCable;
    }

    @Override
    public void setRedstoneLevel(int level, boolean strongPower) {
        partCable.setRedstoneLevel(level);
        partCable.setRedstoneStrong(strongPower);
        partCable.sendUpdate();
    }

    @Override
    public int getRedstoneLevel() {
        return partCable.getRedstoneLevel();
    }

    @Override
    public boolean isStrong() {
        return partCable.isRedstoneStrong();
    }

    @Override
    public void setAllowRedstoneInput(boolean allow) {
        partCable.setAllowsRedstone(allow);
        partCable.sendUpdate();
    }

    @Override
    public boolean isAllowRedstoneInput() {
        return partCable.isAllowsRedstone();
    }
}
