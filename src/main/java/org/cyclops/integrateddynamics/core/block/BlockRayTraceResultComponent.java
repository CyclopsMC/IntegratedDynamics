package org.cyclops.integrateddynamics.core.block;

import net.minecraft.world.phys.BlockHitResult;

/**
 * A {@link BlockHitResult} that specifies the selected {@link VoxelShapeComponents.IComponent}.
 * @author rubensworks
 */
public class BlockRayTraceResultComponent extends BlockHitResult {

    private final VoxelShapeComponents.IComponent component;

    public BlockRayTraceResultComponent(BlockHitResult blockRayTraceResult, VoxelShapeComponents.IComponent component) {
        super(blockRayTraceResult.getLocation(), blockRayTraceResult.getDirection(), blockRayTraceResult.getBlockPos(), blockRayTraceResult.isInside());
        this.component = component;
    }

    public VoxelShapeComponents.IComponent getComponent() {
        return component;
    }
}
