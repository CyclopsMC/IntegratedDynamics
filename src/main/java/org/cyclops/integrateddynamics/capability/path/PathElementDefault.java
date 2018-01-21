package org.cyclops.integrateddynamics.capability.path;

import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import java.util.Collections;
import java.util.Set;

/**
 * Default implementation of {@link IPathElement}.
 * @author rubensworks
 */
public abstract class PathElementDefault implements IPathElement {

    @Override
    public Set<ISidedPathElement> getReachableElements() {
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IPathElement && compareTo((IPathElement)o) == 0;
    }

    @Override
    public int compareTo(IPathElement o) {
        return getPosition().compareTo(o.getPosition());
    }

    @Override
    public int hashCode() {
        return getPosition().hashCode();
    }
}
