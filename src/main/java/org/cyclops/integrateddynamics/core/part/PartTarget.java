package org.cyclops.integrateddynamics.core.part;

import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;

/**
 * Object holder to refer to another block side and its origin.
 * @author rubensworks
 */
@Data(staticConstructor = "of")
public class PartTarget {

    private final PartPos center;
    private final PartPos target;

    /**
     * Get the target from a center block that is targeted at another block.
     * @param pos The central position that is referring to the target.
     * @param side The side on the central position that points to the target.
     * @return The target referral.
     */
    public static PartTarget fromCenter(DimPos pos, EnumFacing side) {
        return PartTarget.of(
                PartPos.of(pos, side),
                PartPos.of(DimPos.of(pos.getWorld(), pos.getBlockPos().offset(side)), side.getOpposite())
        );
    }

    /**
     * Get the target from a center block that is targeted at another block.
     * @param pos The central position that is referring to the target.
     * @return The target referral.
     */
    public static PartTarget fromCenter(PartPos pos) {
        return fromCenter(pos.getPos(), pos.getSide());
    }

    /**
     * Get the target from a center block that is targeted at another block.
     * @param world The world.
     * @param pos The central position that is referring to the target.
     * @param side The side on the central position that points to the target.
     * @return The target referral.
     */
    public static PartTarget fromCenter(World world, BlockPos pos, EnumFacing side) {
        return PartTarget.fromCenter(DimPos.of(world, pos), side);
    }

}
