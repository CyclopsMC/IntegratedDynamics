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
public class PathElementPart<T extends IMultipart> extends PathElementCable {

    private final T part;
    private final ICable cable;

    public PathElementPart(T part, ICable cable) {
        this.part = part;
        this.cable = cable;
    }

    protected T getPart() {
        return part;
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
