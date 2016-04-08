package org.cyclops.integrateddynamics.api.part;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;

/**
 * Object holder to refer to a block side and position.
 * @author rubensworks
 */
public class PartPos {

    private final DimPos pos;
    private final EnumFacing side;

    public static PartPos of(World world, BlockPos pos, EnumFacing side) {
        return of(DimPos.of(world, pos), side);
    }

    public static PartPos of(DimPos pos, EnumFacing side) {
        return new PartPos(pos, side);
    }

    private PartPos(DimPos pos, EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    public DimPos getPos() {
        return pos;
    }

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
        int result = pos.hashCode();
        result = 31 * result + side.hashCode();
        return result;
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
        IPartContainer partContainer = TileHelpers.getSafeTile(pos.getPos().getWorld(), pos.getPos().getBlockPos(), IPartContainer.class);
        if(partContainer != null) {
            IPartType partType = partContainer.getPart(pos.getSide());
            IPartState partState = partContainer.getPartState(pos.getSide());
            if(partType != null && partState != null) {
                return Pair.of(partType, partState);
            }
        }
        return null;
    }

    /**
     * Get the network at the given position.
     * @param pos The part position.
     * @return The network or null if not found.
     */
    public static IPartNetwork getNetwork(PartPos pos) {
        ITileCableNetwork cableNetwork = TileHelpers.getSafeTile(pos.getPos().getWorld(), pos.getPos().getBlockPos(), ITileCableNetwork.class);
        return cableNetwork.getNetwork();
    }

}
