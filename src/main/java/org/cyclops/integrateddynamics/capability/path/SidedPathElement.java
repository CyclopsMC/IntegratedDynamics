package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class SidedPathElement implements ISidedPathElement {

    private final IPathElement pathElement;
    private final EnumFacing side;

    public SidedPathElement(IPathElement pathElement, @Nullable EnumFacing side) {
        this.pathElement = pathElement;
        this.side = side;
    }

    @Override
    public IPathElement getPathElement() {
        return pathElement;
    }

    @Override
    @Nullable
    public EnumFacing getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ISidedPathElement && compareTo((ISidedPathElement)o) == 0;
    }

    @Override
    public int compareTo(ISidedPathElement o) {
        int pathElement = getPathElement().getPosition().compareTo(o.getPathElement().getPosition());
        if (pathElement == 0) {
            EnumFacing thisSide = getSide();
            EnumFacing thatSide = o.getSide();
            // If one of the sides is null, assume equality
            return thisSide != null && thatSide != null ? thisSide.compareTo(thatSide) : 0;
        }
        return pathElement;
    }

    @Override
    public int hashCode() {
        return getPathElement().getPosition().hashCode();
    }

    @Override
    public String toString() {
        return "[Sided PE: " + getPathElement().getPosition() + " @ " + getSide() + "]";
    }

    public static SidedPathElement of(IPathElement pathElement, @Nullable EnumFacing side) {
        return new SidedPathElement(pathElement, side);
    }
}
