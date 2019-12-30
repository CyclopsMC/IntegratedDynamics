package org.cyclops.integrateddynamics.core.block;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;

import java.util.Collection;
import java.util.List;

/**
 * Allows {@link VoxelShapeComponents} instances to be created
 * based on a set of registered handlers that can create components
 * under given circumstances.
 * @author rubensworks
 */
public class VoxelShapeComponentsFactory {

    private final List<IHandler> handlers = Lists.newArrayList();

    public VoxelShapeComponentsFactory(IHandler... handlers) {
        for (IHandler handler : handlers) {
            addHandler(handler);
        }
    }

    public void addHandler(IHandler handler) {
        this.handlers.add(handler);
    }

    public VoxelShapeComponents createShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        List<VoxelShapeComponents.IComponent> components = Lists.newArrayList();
        for (IHandler handler : handlers) {
            components.addAll(handler.createComponents(blockState, world, blockPos));
        }
        return VoxelShapeComponents.create(blockState, world, blockPos, selectionContext, components);
    }

    public static interface IHandler {
        public Collection<VoxelShapeComponents.IComponent> createComponents(BlockState blockState, IBlockReader world, BlockPos blockPos);
    }

}
