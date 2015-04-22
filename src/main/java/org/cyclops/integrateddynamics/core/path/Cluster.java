package org.cyclops.integrateddynamics.core.path;

import lombok.Data;
import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.Set;

/**
 * A cluster for a collection of path elements.
 * @author rubensworks
 */
@Data
public class Cluster<E extends IPathElement> implements Collection<E> {

    @Delegate
    private final Set<E> elements;
}
