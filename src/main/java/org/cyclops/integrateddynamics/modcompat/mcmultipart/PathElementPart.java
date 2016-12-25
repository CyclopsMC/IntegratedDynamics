package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.multipart.IMultipart;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementCable;

/**
 * Implementation of {@link IPathElement} for {@link IMultipart}.
 * @author rubensworks
 */
public class PathElementPart extends PathElementCable {

    private final IMultipart part;
    private final ICable cable;

    public PathElementPart(IMultipart part, ICable cable) {
        this.part = part;
        this.cable = cable;
    }

    @Override
    protected ICable getCable() {
        return cable;
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(part.getWorld(), part.getPos());
    }
}
