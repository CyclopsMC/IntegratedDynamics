package org.cyclops.integrateddynamics.capability.path;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Implementation of {@link IPathElement} that only carries a position.
 *
 * Path elements are compared by position only, so this can be used to look up or remove
 * a path element in a network when the block entity it originated from is not available anymore.
 *
 * @author rubensworks
 */
public class PathElementPosition extends PathElementDefault {

    private final DimPos position;

    public PathElementPosition(DimPos position) {
        this.position = position;
    }

    @Override
    public DimPos getPosition() {
        return this.position;
    }
}
