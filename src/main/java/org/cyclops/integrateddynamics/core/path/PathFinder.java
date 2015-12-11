package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import java.util.Set;
import java.util.TreeSet;

/**
 * Algorithm to construct paths/clusters of {@link IPathElement}s.
 * @author rubensworks
 */
public final class PathFinder {

    protected static <E extends IPathElement<E>> TreeSet<E> getConnectedElements(E head, Set<DimPos> visitedPositions) {
        TreeSet<E> elements = Sets.newTreeSet();

        // Make sure to add our head
        if(!visitedPositions.contains(head.getPosition())) {
            elements.add(head);
            visitedPositions.add(head.getPosition());
        }

        // Add neighbours that haven't been checked yet.
        for(E neighbour : head.getReachableElements()) {
            if(!visitedPositions.contains(neighbour.getPosition())) {
                elements.add(neighbour);
                visitedPositions.add(neighbour.getPosition());
            }
        }

        // Loop over the added neighbours to recursively check their neighbours.
        Set<E> neighbourElements = Sets.newHashSet();
        for(E addedElement : elements) {
            neighbourElements.addAll(getConnectedElements(addedElement, visitedPositions));
        }
        elements.addAll(neighbourElements);

        return elements;
    }

    public static <E extends IPathElement<E>> Cluster<E> getConnectedCluster(E head) {
        return new Cluster<E>(getConnectedElements(head, Sets.<DimPos>newTreeSet()));
    }

}
