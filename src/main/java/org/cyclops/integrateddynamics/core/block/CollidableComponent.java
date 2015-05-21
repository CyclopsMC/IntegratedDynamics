package org.cyclops.integrateddynamics.core.block;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Component for blocks that require complex collision detection.
 * @author rubensworks
 */
@Data
public class CollidableComponent<B extends Block & ICollidableParent> implements ICollidable {

    private final B block;
    private final List<IComponent<EnumFacing, B>> components;
    private final int totalComponents;

    public CollidableComponent(B block, List<IComponent<EnumFacing, B>> components) {
        this.block = block;
        this.components = components;
        int count = 0;
        for(IComponent component : components) {
            count += component.getPossiblePositions().size();
        }
        this.totalComponents = count;
    }

    private void addComponentCollisionBoxesToList(IComponent<EnumFacing, B> component, World world, BlockPos pos, IBlockState state,
                         AxisAlignedBB axisalignedbb, List list, Entity collidingEntity) {
        for(EnumFacing position : component.getPossiblePositions()) {
            if(component.isActive(getBlock(), world, pos, position)) {
                setBlockBounds(component.getBounds(getBlock(), world, pos, position));
                getBlock().addCollisionBoxesToListParent(world, pos, state, axisalignedbb, list, collidingEntity);
            }
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb,
                                        List list, Entity collidingEntity) {
        // Add bounding boxes for all active components.
        for(IComponent component : components) {
            addComponentCollisionBoxesToList(component, world, pos, state, axisalignedbb, list, collidingEntity);
        }

        // Reset the bounding box to prevent any entity glitches.
        getBlock().setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        RayTraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);
        if (rayTraceResult != null && rayTraceResult.getBoundingBox() != null) {
            AxisAlignedBB box = rayTraceResult.getBoundingBox();
            return box.offset(pos.getX(), pos.getY(), pos.getZ());
        }
        // Happens when client hovers away from a block.
        return getBlock().getSelectedBoundingBoxParent(world, pos).expand(-0.625F, -0.625F, -0.625F);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
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
        double reachDistance;
        if (player instanceof EntityPlayerMP) {
            reachDistance = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
        } else {
            reachDistance = 5;
        }

        double eyeHeight = world.isRemote ? player.getEyeHeight(): player.getEyeHeight(); // Client removed :  - player.getDefaultEyeHeight()
        Vec3 lookVec = player.getLookVec();
        Vec3 origin = new Vec3(player.posX, player.posY + eyeHeight, player.posZ);
        Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

        return doRayTrace(world, pos, origin, direction);
    }

    private int doRayTraceComponent(IComponent<EnumFacing, B> component, int countStart,
                                        World world, BlockPos pos, Vec3 origin, Vec3 direction,
                                        MovingObjectPosition[] hits, AxisAlignedBB[] boxes, EnumFacing[] sideHit,
                                        IComponent<EnumFacing, B>[] components) {
        int i = countStart;
        for(EnumFacing position : component.getPossiblePositions()) {
            if(component.isActive(getBlock(), world, pos, position)) {
                AxisAlignedBB bb = component.getBounds(getBlock(), world, pos, position);
                setBlockBounds(bb);
                boxes[i]      = bb;
                hits[i]       = getBlock().collisionRayTraceParent(world, pos, origin, direction);
                sideHit[i]    = position;
                components[i] = component;
            }
            i++;
        }
        return i;
    }

    private RayTraceResult doRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
        // Perform a ray trace for all six sides.
        MovingObjectPosition[] hits = new MovingObjectPosition[totalComponents];
        AxisAlignedBB[] boxes = new AxisAlignedBB[totalComponents];
        EnumFacing[] sideHit = new EnumFacing[totalComponents];
        IComponent<EnumFacing, B>[] componentsOutput = new IComponent[totalComponents];
        Arrays.fill(sideHit, null);

        // Ray trace for all active components.
        int count = 0;
        for(IComponent<EnumFacing, B> component : components) {
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

        // Reset bounds
        getBlock().setBlockBounds(0, 0, 0, 1, 1, 1);

        if (minIndex != -1) {
            return new RayTraceResult(hits[minIndex], boxes[minIndex], sideHit[minIndex], componentsOutput[minIndex]);
        }
        return null;
    }

    private void setBlockBounds(AxisAlignedBB bounds) {
        getBlock().setBlockBounds((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ,
                                  (float) bounds.maxX, (float) bounds.maxY, (float) bounds.maxZ);
    }

}
