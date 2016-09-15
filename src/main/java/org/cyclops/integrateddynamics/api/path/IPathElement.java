package org.cyclops.integrateddynamics.api.path;

import org.cyclops.cyclopscore.datastructure.DimPos;

import java.util.Set;

/**
 * An element that can be used to construct paths using the
 * {@link org.cyclops.integrateddynamics.core.path.PathFinder}.
 * Multiple instances for the same 'element' can be created, so the comparator implementation must
 * make sure that these instances are considered equal.
 * These instances are used as a simple way of referring to these elements.
 * @author rubensworks
 */
public interface IPathElement extends Comparable {

    /**
     * @return The position of this element.
     */
    public DimPos getPosition();

    /**
     * @return The set of all path elements that can be reached from here.
     */
    public Set<IPathElement> getReachableElements();

}
