package org.cyclops.integrateddynamics.core.path;

import org.cyclops.cyclopscore.datastructure.DimPos;

import java.util.Set;

/**
 * An element that can be used to construct paths using the
 * {@link org.cyclops.integrateddynamics.core.path.PathFinder}.
 * @author rubensworks
 */
public interface IPathElement<E extends IPathElement<E>> {

    /**
     * @return The position of this element.
     */
    public DimPos getPosition();

    /**
     * @return The set of all path elements that can be reached from here.
     */
    public Set<E> getReachableElements();

}
