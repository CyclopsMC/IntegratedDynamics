package org.cyclops.integrateddynamics.core.block;

import net.minecraft.util.math.BlockRayTraceResult;

/**
 * A {@link BlockRayTraceResult} that specifies the selected {@link VoxelShapeComponents.IComponent}.
 * @author rubensworks
 */
public class BlockRayTraceResultComponent extends BlockRayTraceResult {

    private final VoxelShapeComponents.IComponent component;

    public BlockRayTraceResultComponent(BlockRayTraceResult blockRayTraceResult, VoxelShapeComponents.IComponent component) {
        super(blockRayTraceResult.getLocation(), blockRayTraceResult.getDirection(), blockRayTraceResult.getBlockPos(), blockRayTraceResult.isInside());
        this.component = component;
    }

    public VoxelShapeComponents.IComponent getComponent() {
        return component;
    }
}
