package org.cyclops.integrateddynamics.api.part;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;

/**
 * Object holder to refer to a block side and position.
 * @author rubensworks
 */
public class PartPos implements Comparable<PartPos> {

    private final DimPos pos;
    private final EnumFacing side;

    public static PartPos of(World world, BlockPos pos, @Nullable EnumFacing side) {
        return of(DimPos.of(world, pos), side);
    }

    public static PartPos of(DimPos pos, @Nullable EnumFacing side) {
        return new PartPos(pos, side);
    }

    private PartPos(DimPos pos, @Nullable EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    public DimPos getPos() {
        return pos;
    }

    @Nullable
    public EnumFacing getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PartPos)) return false;

        PartPos partPos = (PartPos) o;

        if (!pos.equals(partPos.pos)) return false;
        return side == partPos.side;

    }

    @Override
    public int hashCode() {
        return 31 * pos.hashCode() + (side != null ? side.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "PartPos{" +
                "pos=" + pos +
                ", side=" + side +
                '}';
    }

    /**
     * Get part data from the given position.
     * @param pos The part position.
     * @return A pair of part type and part state or null if not found.
     */
    public static Pair<IPartType, IPartState> getPartData(PartPos pos) {
        IPartContainer partContainer = PartHelpers.getPartContainer(pos.getPos(), pos.getSide());
        if (partContainer != null) {
            IPartType partType = partContainer.getPart(pos.getSide());
            IPartState partState = partContainer.getPartState(pos.getSide());
            if (partType != null && partState != null) {
                return Pair.of(partType, partState);
            }
        }
        return null;
    }

    @Override
    public int compareTo(PartPos o) {
        int pos = this.getPos().compareTo(o.getPos());
        if (pos == 0) {
            EnumFacing thisSide = this.getSide();
            EnumFacing thatSide = o.getSide();
            return thisSide == null ? (thatSide == null ? 0 : -1) : (thatSide == null ? 1 : thisSide.compareTo(thatSide));
        }
        return pos;
    }
}
