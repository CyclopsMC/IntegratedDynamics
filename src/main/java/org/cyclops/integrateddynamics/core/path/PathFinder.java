package org.cyclops.integrateddynamics.core.path;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;


/**
 * Algorithm to construct paths/clusters of {@link IPathElement}s.
 * @author rubensworks
 */
public final class PathFinder {

    protected static Set<ISidedPathElement> getConnectedElements(ISidedPathElement head) {
        Set<ISidedPathElement> connectedElements = new HashSet<>();
        Set<DimPos> visitedPositions = new HashSet<>();

        // Use a queue for BFS traversal.
        Queue<ISidedPathElement> queue = new ArrayDeque<>();
        DimPos headPos = head.getPathElement().getPosition();
        visitedPositions.add(headPos);
        queue.offer(head);

        while (!queue.isEmpty()) {
            ISidedPathElement current = queue.poll();
            connectedElements.add(current);
            IPathElement currentElement = current.getPathElement();

            for (ISidedPathElement neighbour : currentElement.getReachableElements()) {
                DimPos neighbourPos = neighbour.getPathElement().getPosition();
                if (visitedPositions.add(neighbourPos)) { // Adds and returns true if not already present.
                    queue.offer(neighbour);
                }
            }
        }

        return connectedElements;
    }

    public static Cluster getConnectedCluster(ISidedPathElement head) {
        return new Cluster(new TreeSet<>(getConnectedElements(head)));
    }
}
