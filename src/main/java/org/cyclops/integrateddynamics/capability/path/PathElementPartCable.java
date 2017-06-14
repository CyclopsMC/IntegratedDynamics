package org.cyclops.integrateddynamics.capability.path;

import mcmultipart.multipart.IMultipart;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.modcompat.mcmultipart.PartCable;

import java.util.Set;

/**
 * Implementation of {@link IPathElement} for {@link IMultipart}.
 * @author rubensworks
 */
public class PathElementPartCable extends PathElementPart<PartCable> {

    public PathElementPartCable(PartCable part, ICable cable) {
        super(part, cable);
    }

    @Override
    public Set<IPathElement> getReachableElements() {
        // Add the reachable path elements from the parts that provide one.
        Set<IPathElement> pathElements = super.getReachableElements();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (getPart().getPartContainer().hasCapability(PathElementConfig.CAPABILITY, side)) {
                pathElements.addAll(getPart().getPartContainer()
                        .getCapability(PathElementConfig.CAPABILITY, side).getReachableElements());
            }
        }
        return pathElements;
    }
}
