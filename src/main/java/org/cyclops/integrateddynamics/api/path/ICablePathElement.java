package org.cyclops.integrateddynamics.api.path;

import org.cyclops.integrateddynamics.api.block.cable.ICable;

/**
 * A path element for {@link ICable}.
 * @author rubensworks
 */
public interface ICablePathElement extends IPathElement<ICablePathElement> {

    /**
     * @return The cable.
     */
    public ICable getCable();

}
