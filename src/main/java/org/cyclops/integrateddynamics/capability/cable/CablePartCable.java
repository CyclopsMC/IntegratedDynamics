package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.modcompat.mcmultipart.PartCable;

/**
 * Implementation of {@link ICable} for a {@link PartCable}.
 * @author rubensworks
 */
public class CablePartCable extends CablePart<PartCable> {

    public CablePartCable(PartCable part) {
        super(part);
    }

    @Override
    protected boolean isForceDisconnectable() {
        return true;
    }

    @Override
    protected EnumFacingMap<Boolean> getForceDisconnected() {
        return part.getForceDisconnected();
    }

    @Override
    protected EnumFacingMap<Boolean> getConnected() {
        return part.getConnected();
    }

    @Override
    public boolean isForceDisconnected(EnumFacing side) {
        if(part.getPartContainer().hasPart(side)) return true;
        return super.isForceDisconnected(side);
    }

    @Override
    public void destroy() {
        part.getContainer().removePart(part);
    }
}
