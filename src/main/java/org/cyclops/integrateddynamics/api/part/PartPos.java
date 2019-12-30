package org.cyclops.integrateddynamics.api.part;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;

/**
 * Object holder to refer to a block side and position.
 * @author rubensworks
 */
public class PartPos implements Comparable<PartPos> {

    static {
        PacketCodec.addCodedAction(PartPos.class, new PacketCodec.ICodecAction() {

            @Override
            public void encode(Object object, PacketBuffer packetBuffer) {
                PacketCodec.write(packetBuffer, ((PartPos) object).getPos());
                PacketCodec.write(packetBuffer, ((PartPos) object).getSide());
            }

            @Override
            public Object decode(PacketBuffer packetBuffer) {
                DimPos pos = PacketCodec.read(packetBuffer, DimPos.class);
                Direction side = PacketCodec.read(packetBuffer, Direction.class);
                return PartPos.of(pos, side);
            }
        });
    }

    private final DimPos pos;
    private final Direction side;

    public static PartPos of(World world, BlockPos pos, @Nullable Direction side) {
        return of(DimPos.of(world, pos), side);
    }

    public static PartPos of(DimPos pos, @Nullable Direction side) {
        return new PartPos(pos, side);
    }

    private PartPos(DimPos pos, @Nullable Direction side) {
        this.pos = pos;
        this.side = side;
    }

    public DimPos getPos() {
        return pos;
    }

    @Nullable
    public Direction getSide() {
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
    @Nullable
    public static Pair<IPartType, IPartState> getPartData(PartPos pos) {
        IPartContainer partContainer = PartHelpers.getPartContainer(pos.getPos(), pos.getSide()).orElse(null);
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
            Direction thisSide = this.getSide();
            Direction thatSide = o.getSide();
            return thisSide == null ? (thatSide == null ? 0 : -1) : (thatSide == null ? 1 : thisSide.compareTo(thatSide));
        }
        return pos;
    }
}
