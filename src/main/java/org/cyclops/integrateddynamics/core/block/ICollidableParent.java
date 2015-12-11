package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Interface used to access the parent methods from a {@link ICollidable}.
 * @author rubensworks
 */
public interface ICollidableParent {

    /**
     * Simply forward this call to the super.
     * @param worldIn The world
     * @param pos The position
     * @param state The block state
     * @param mask The bounding boxes mask
     * @param list The list to add to
     * @param collidingEntity The entity that is colliding
     */
    public void addCollisionBoxesToListParent(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask,
                                               List list, Entity collidingEntity);

    /**
     * Simply forward this call to the super.
     * @param worldIn The world
     * @param pos The position
     * @return The selected bounding box
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxParent(World worldIn, BlockPos pos);

    /**
     * Simply forward this call to the super.
     * @param world The world
     * @param pos The position
     * @param origin The origin vector
     * @param direction The direction vector
     * @return The position object holder
     */
    public MovingObjectPosition collisionRayTraceParent(World world, BlockPos pos, Vec3 origin, Vec3 direction);

}
