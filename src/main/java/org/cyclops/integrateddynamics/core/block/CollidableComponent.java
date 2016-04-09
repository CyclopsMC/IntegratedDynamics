package org.cyclops.integrateddynamics.core.block;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.BlockHelpers;

import java.util.Arrays;
import java.util.List;

/**
 * Component for blocks that require complex collision detection.
 * @author rubensworks
 * @param <P> The type of positions this component type can provide.
 * @param <B> The type of block this component is part of.
 */
@Data
public class CollidableComponent<P, B extends Block & ICollidableParent> implements ICollidable {

    private final B block;
    private final List<IComponent<P, B>> components;
    private final int totalComponents;

    public CollidableComponent(B block, List<IComponent<P, B>> components) {
        this.block = block;
        this.components = components;
        int count = 0;
        for(IComponent component : components) {
            for(Object position : component.getPossiblePositions()) {
                count += component.getBoundsCount(position);
            }
        }
        this.totalComponents = count;
    }

    private void addComponentCollisionBoxesToList(IComponent<EnumFacing, B> component, IBlockState state, World world, BlockPos pos,
                         AxisAlignedBB axisalignedbb, List<AxisAlignedBB> list, Entity collidingEntity) {
        for(EnumFacing position : component.getPossiblePositions()) {
            if(component.isActive(getBlock(), world, pos, position)) {
                for(AxisAlignedBB bb : component.getBounds(getBlock(), world, pos, position)) {
                    BlockHelpers.addCollisionBoxToList(pos, axisalignedbb, list, bb);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB axisalignedbb,
                                        List list, Entity collidingEntity) {
        // Add bounding boxes for all active components.
        for(IComponent component : components) {
            addComponentCollisionBoxesToList(component, state, world, pos, axisalignedbb, list, collidingEntity);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState blockState, World world, BlockPos pos) {
        RayTraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);
        if (rayTraceResult != null && rayTraceResult.getBoundingBox() != null) {
            AxisAlignedBB box = rayTraceResult.getBoundingBox();
            return box.offset(pos.getX(), pos.getY(), pos.getZ());
        }
        // Happens when client hovers away from a block.
        return getBlock().getSelectedBoundingBoxParent(blockState, world, pos).expand(-0.625F, -0.625F, -0.625F);
    }

    @Override
    public net.minecraft.util.math.RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        RayTraceResult raytraceResult = doRayTrace(world, pos, origin, direction);
        if (raytraceResult == null) {
            return null;
        } else {
            return raytraceResult.getMovingObjectPosition();
        }
    }

    /**
     * Do a ray trace for the current look direction of the player.
     * @param world The world.
     * @param pos The block position to perform a ray trace for.
     * @param player The player.
     * @return A holder object with information on the ray tracing.
     */
    public RayTraceResult doRayTrace(World world, BlockPos pos, EntityPlayer player) {
        if(player == null) {
            return null;
        }
        double reachDistance;
        if (player instanceof EntityPlayerMP) {
            reachDistance = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
        } else {
            reachDistance = 5;
        }

        double eyeHeight = world.isRemote ? player.getEyeHeight(): player.getEyeHeight(); // Client removed :  - player.getDefaultEyeHeight()
        Vec3d lookVec = player.getLookVec();
        Vec3d origin = new Vec3d(player.posX, player.posY + eyeHeight, player.posZ);
        Vec3d direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

        return doRayTrace(world, pos, origin, direction);
    }

    private int doRayTraceComponent(IComponent<P, B> component, int countStart,
                                        World world, BlockPos pos, Vec3d origin, Vec3d direction,
                                        net.minecraft.util.math.RayTraceResult[] hits, AxisAlignedBB[] boxes, P[] sideHit,
                                        IComponent<P, B>[] components) {
        int i = countStart;
        for(P position : component.getPossiblePositions()) {
            if(component.isActive(getBlock(), world, pos, position)) {
                int offset = 0;
                for(AxisAlignedBB bb : component.getBounds(getBlock(), world, pos, position)) {
                    boxes[i + offset] = bb;
                    hits[i + offset] = getBlock().rayTraceParent(pos, origin, direction, bb);
                    sideHit[i + offset] = position;
                    components[i + offset] = component;
                    offset++;
                }
            }
            i += component.getBoundsCount(position);
        }
        return i;
    }

    private RayTraceResult doRayTrace(World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        // Perform a ray trace for all six sides.
        net.minecraft.util.math.RayTraceResult[] hits = new net.minecraft.util.math.RayTraceResult[totalComponents];
        AxisAlignedBB[] boxes = new AxisAlignedBB[totalComponents];
        @SuppressWarnings("unchecked")
        P[] sideHit = (P[]) new Object[totalComponents];
        @SuppressWarnings("unchecked")
        IComponent<P, B>[] componentsOutput = new IComponent[totalComponents];
        Arrays.fill(sideHit, null);

        // Ray trace for all active components.
        int count = 0;
        for(IComponent<P, B> component : components) {
            count = doRayTraceComponent(component, count, world, pos, origin, direction, hits, boxes, sideHit, componentsOutput);
        }

        // Find the closest hit
        double minDistance = Double.POSITIVE_INFINITY;
        int minIndex = -1;
        for (int i = 0; i < hits.length; i++) {
            if (hits[i] != null) {
                double d = hits[i].hitVec.squareDistanceTo(origin);
                if (d < minDistance) {
                    minDistance = d;
                    minIndex = i;
                }
            }
        }

        if (minIndex != -1) {
            return new RayTraceResult<P>(hits[minIndex], boxes[minIndex], sideHit[minIndex], componentsOutput[minIndex]);
        }
        return null;
    }

}
