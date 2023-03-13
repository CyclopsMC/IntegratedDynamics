package org.cyclops.integrateddynamics.api.part;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;

import javax.annotation.Nullable;

/**
 * Object holder to refer to another block side and its origin.
 * @author rubensworks
 */
public class PartTarget {

    private final PartPos center;
    private final PartPos target;

    /**
     * Get the target from a center block that is targeted at another block.
     * @param pos The central position that is referring to the target.
     * @param side The side on the central position that points to the target.
     * @return The target referral.
     */
    public static PartTarget fromCenter(DimPos pos, @Nullable Direction side) {
        return PartTarget.of(
                PartPos.of(pos, side),
                PartPos.of(DimPos.of(pos.getLevelKey(), side == null ? pos.getBlockPos() : pos.getBlockPos().relative(side)), side == null ? null : side.getOpposite())
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
    public static PartTarget fromCenter(Level world, BlockPos pos, Direction side) {
        return PartTarget.fromCenter(DimPos.of(world, pos), side);
    }

    /**
     * Create a new instance.
     * @param center The center position.
     * @param target The target position.
     * @return The target.
     */
    public static PartTarget of(PartPos center, PartPos target) {
        return new PartTarget(center, target);
    }

    public PartPos getCenter() {
        return center;
    }

    public PartPos getTarget() {
        return target;
    }

    private PartTarget(PartPos center, PartPos target) {
        this.center = center;
        this.target = target;
    }

    /**
     * Create a new instance with the given target side.
     * @param targetSide The side of the target.
     * @return A new {@link PartTarget} instance.
     */
    public PartTarget forTargetSide(Direction targetSide) {
        return new PartTarget(center, PartPos.of(target.getPos(), targetSide));
    }

    /**
     * Create a new instance with the given target offset.
     * @param offset The offset of the target.
     * @return A new {@link PartTarget} instance.
     */
    public PartTarget forOffset(Vec3i offset) {
        return new PartTarget(center, PartPos.of(target.getPos().withPosition(target.getPos().getBlockPos().offset(offset)), target.getSide()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartTarget)) return false;

        PartTarget that = (PartTarget) o;

        if (!center.equals(that.center)) return false;
        return target.equals(that.target);

    }

    @Override
    public int hashCode() {
        int result = center.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PartTarget{" +
                "center=" + center +
                ", target=" + target +
                '}';
    }
}
