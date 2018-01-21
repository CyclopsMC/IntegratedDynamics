package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import java.util.Set;
import java.util.TreeSet;

/**
 * Algorithm to construct paths/clusters of {@link IPathElement}s.
 * @author rubensworks
 */
public final class PathFinder {

    protected static TreeSet<ISidedPathElement> getConnectedElements(ISidedPathElement head, Set<DimPos> visitedPositions) {
        TreeSet<ISidedPathElement> elements = Sets.newTreeSet();

        // Make sure to add our head
        if(!visitedPositions.contains(head.getPathElement().getPosition())) {
            elements.add(head);
            visitedPositions.add(head.getPathElement().getPosition());
        }

        // Add neighbours that haven't been checked yet.
        for(ISidedPathElement neighbour : head.getPathElement().getReachableElements()) {
            if(!visitedPositions.contains(neighbour.getPathElement().getPosition())) {
                elements.add(neighbour);
                visitedPositions.add(neighbour.getPathElement().getPosition());
            }
        }

        // Loop over the added neighbours to recursively check their neighbours.
        Set<ISidedPathElement> neighbourElements = Sets.newHashSet();
        for(ISidedPathElement addedElement : elements) {
            neighbourElements.addAll(getConnectedElements(addedElement, visitedPositions));
        }
        elements.addAll(neighbourElements);

        return elements;
    }

    public static Cluster getConnectedCluster(ISidedPathElement head) {
        return new Cluster(getConnectedElements(head, Sets.<DimPos>newTreeSet()));
    }

}
