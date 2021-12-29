package org.cyclops.integrateddynamics.api.path;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

/**
 * A path element wrapped together with the side it was found on.
 * Multiple instances for the same 'element' can be created, so the comparator implementation must
 * make sure that these instances are considered equal.
 * @author rubensworks
 */
public interface ISidedPathElement extends Comparable<ISidedPathElement> {

    /**
     * @return The path element.
     */
    public IPathElement getPathElement();

    /**
     * @return The side the path element was found on.
     */
    @Nullable
    public Direction getSide();

}
