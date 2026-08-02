package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public interface ICableRayTraceHandler {

    public boolean canHandle(BlockPos pos, @Nullable Entity entity);

    /**
     * A custom ray trace handler.
     * @param pos The block position to perform a ray trace for.
     * @param entity The entity.
     * @param parentRayTracer The parent ray trace logic.
     * @return A holder object with information on the ray tracing.
     */
    public BlockRayTraceResultComponent rayTrace(BlockPos pos, @Nullable Entity entity, TriFunction<BlockPos, Entity, Vec3, BlockRayTraceResultComponent> parentRayTracer);
}
