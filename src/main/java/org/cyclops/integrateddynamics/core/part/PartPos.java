package org.cyclops.integrateddynamics.core.part;

import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;

/**
 * Object holder to refer to a block side and position.
 * @author rubensworks
 */
@Data(staticConstructor = "of")
public class PartPos {

    private final DimPos pos;
    private final EnumFacing side;

    public static PartPos of(World world, BlockPos pos, EnumFacing side) {
        return of(DimPos.of(world, pos), side);
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
    public static Network getNetwork(PartPos pos) {
        ITileCableNetwork cableNetwork = TileHelpers.getSafeTile(pos.getPos().getWorld(), pos.getPos().getBlockPos(), ITileCableNetwork.class);
        return cableNetwork.getNetwork();
    }

}
