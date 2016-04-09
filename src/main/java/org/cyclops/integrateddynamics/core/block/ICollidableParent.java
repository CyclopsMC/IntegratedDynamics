package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface used to access the parent methods from a {@link ICollidable}.
 * @author rubensworks
 */
public interface ICollidableParent {

    /**
     * Simply forward this call to the super.
     * @param blockState The block state
     * @param worldIn The world
     * @param pos The position
     * @return The selected bounding box
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxParent(IBlockState blockState, World worldIn, BlockPos pos);

    /**
     * Simply forward this call to the super.
     * @param pos The position
     * @param start The start vector
     * @param end The end vector
     * @param boundingBox The bounding box to ray trace with.
     * @return The position object holder
     */
    public RayTraceResult rayTraceParent(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox);

}
