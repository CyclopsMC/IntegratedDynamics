package org.cyclops.integrateddynamics.core.block;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Interface for blocks that have a collidable component.
 * Delegate calls to {@link org.cyclops.integrateddynamics.core.block.CollidableComponent}.
 * @param <P> The type of positions this component type can provide.
 * @author rubensworks
 */
public interface ICollidable<P> {

    /**
     * @return The colliding block instance
     */
    public Block getBlock();

    /**
     * Add the current block bounding box to the given list.
     * @param world The world
     * @param pos The position
     * @param state The block state
     * @param mask The bounding boxes mask
     * @param list The list to add to
     * @param collidingEntity The entity that is colliding
     */
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask,
                                        List list, Entity collidingEntity);

    /**
     * The the selected bounding box.
     * @param worldIn The world
     * @param pos The position
     * @return The selected bounding box
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos);

    /**
     * Do a ray trace for the current look direction of the player.
     * @param world The world.
     * @param pos The block position to perform a ray trace for.
     * @param player The player.
     * @return A holder object with information on the ray tracing.
     */
    public RayTraceResult<P> doRayTrace(World world, BlockPos pos, EntityPlayer player);

    /**
     * Ray trace the given direction.
     * @param world The world
     * @param pos The position
     * @param origin The origin vector
     * @param direction The direction vector
     * @return The position object holder
     */
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction);

    /**
     * Result from ray tracing
     * @param <P> The type of position that can be hit.
     */
    @Data
    public static class RayTraceResult<P> {
        private final MovingObjectPosition movingObjectPosition;
        private final AxisAlignedBB boundingBox;
        private final P positionHit;
        private final IComponent<P, ?> collisionType;

        @Override
        public String toString() {
            return String.format("RayTraceResult: %s %s", boundingBox, collisionType);
        }
    }

    /**
     * A component that can be part of the collision detection for a block.
     * @param <P> The type of positions this component type can provide.
     * @param <B> The type of block this component is part of.
     */
    public static interface IComponent<P, B> {
        public Collection<P> getPossiblePositions();
        public int getBoundsCount(P position);
        public boolean isActive(B block, World world, BlockPos pos, P position);
        public List<AxisAlignedBB> getBounds(B block, World world, BlockPos pos, P position);
        public ItemStack getPickBlock(World world, BlockPos pos, P position);

        /**
         * Destroy this component
         * @param world The world
         * @param pos The position
         * @param position The component position
         * @param player The player destroying the component.
         * @return If the complete block was destroyed
         */
        public boolean destroy(World world, BlockPos pos, P position, EntityPlayer player);

        /**
         * @param world The world
         * @param pos The position
         * @param position The component position
         * @return The model that will be used to render the breaking overlay.
         */
        @SideOnly(Side.CLIENT)
        public @Nullable IBakedModel getBreakingBaseModel(World world, BlockPos pos, P position);
    }

}
