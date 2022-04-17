package org.cyclops.integrateddynamics.core.block;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link VoxelShape} that contains one or more {@link VoxelShapeComponents.IComponent}.
 *
 * These components are used to handle ray tracing on seperate components.
 * Consequently, ray trace results on this object may be safely cast to {@link BlockRayTraceResultComponent}
 * so that the targeted component may be retrieved.
 *
 * @author rubensworks
 */
public class VoxelShapeComponents extends VoxelShape implements Iterable<VoxelShape> {

    private final Collection<Pair<VoxelShape, IComponent>> entries;
    private final String stateId;

    protected VoxelShapeComponents(Collection<Pair<VoxelShape, IComponent>> entries, String stateId) {
        super(createInnerPart(entries));
        this.entries = entries;
        this.stateId = stateId;
    }

    protected static VoxelShapePart createInnerPart(Collection<Pair<VoxelShape, IComponent>> entries) {
        return new Part(entries.stream()
                .map(pair -> pair.getLeft().part)
                .collect(Collectors.toList()));
    }

    public static VoxelShapeComponents create(BlockState blockState, IBlockReader world, BlockPos blockPos,
                                              ISelectionContext selectionContext, List<IComponent> components) {
        List<Pair<VoxelShape, IComponent>> entries = Lists.newArrayList();
        for (VoxelShapeComponents.IComponent component : components) {
            VoxelShape shape = component.getShape(blockState, world, blockPos, selectionContext);
            entries.add(Pair.of(shape, component));
        }

        StringBuilder stateIdBuilder = new StringBuilder();
        for (IComponent component : components) {
            stateIdBuilder.append(component.getStateId(blockState, world, blockPos));
            stateIdBuilder.append(";");
        }

        return new VoxelShapeComponents(entries, stateIdBuilder.toString());
    }

    public String getStateId() {
        return this.stateId;
    }

    @Override
    public Iterator<VoxelShape> iterator() {
        return entries.stream().map(Pair::getLeft).iterator();
    }

    @Override
    public double getStart(Direction.Axis axis) {
        boolean first = true;
        double startMin = 0;
        for (VoxelShape shape : this) {
            double start = shape.getStart(axis);
            if (first || start < startMin) {
                startMin = start;
                first = false;
            }
        }
        return startMin;
    }

    @Override
    public double getEnd(Direction.Axis axis) {
        boolean first = true;
        double endMax = 0;
        for (VoxelShape shape : this) {
            double end = shape.getEnd(axis);
            if (first || end > endMax) {
                endMax = end;
                first = false;
            }
        }
        return endMax;
    }

    @Override
    public DoubleList getValues(Direction.Axis axis) {
        DoubleArrayList values = new DoubleArrayList();
        for (VoxelShape shape : this) {
            values.addAll(shape.getValues(axis));
        }
        return values;
    }

    @Override
    public boolean isEmpty() {
        for (VoxelShape shape : this) {
            if (!shape.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public VoxelShape withOffset(double x, double y, double z) {
        List<Pair<VoxelShape, IComponent>> entries = Lists.newArrayList();
        for (Pair<VoxelShape, IComponent> entry : this.entries) {
            entries.add(Pair.of(entry.getLeft().withOffset(x, y, z), entry.getRight()));
        }
        return new VoxelShapeComponents(entries, this.stateId);
    }

    @Override
    public void forEachEdge(VoxelShapes.ILineConsumer consumer) {
        for (VoxelShape shape : this) {
            shape.forEachEdge(consumer);
        }
    }

    @Override
    public void forEachBox(VoxelShapes.ILineConsumer consumer) {
        for (VoxelShape shape : this) {
            shape.forEachBox(consumer);
        }
    }

    @Override
    public double max(Direction.Axis axis, double a, double b) {
        boolean first = true;
        double valueMax = 0D;
        for (VoxelShape shape : this) {
            double value = shape.max(axis, a, b);
            if (first || value > valueMax) {
                valueMax = value;
                first = false;
            }
        }
        return valueMax;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        for (VoxelShape shape : this) {
            if (shape.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockRayTraceResultComponent rayTrace(Vector3d startVec, Vector3d endVec, BlockPos pos) {
        // Find component with shape that is closest to the startVec
        double distanceMin = Double.POSITIVE_INFINITY;
        VoxelShapeComponents.IComponent componentMin = null;
        BlockRayTraceResult resultMin = null;

        for (Pair<VoxelShape, IComponent> entry : entries) {
            VoxelShape shape = entry.getLeft();
            BlockRayTraceResult result = shape.rayTrace(startVec, endVec, pos);
            if (result != null) {
                double distance = result.getHitVec().squareDistanceTo(startVec);
                if (distance < distanceMin) {
                    distanceMin = distance;
                    componentMin = entry.getRight();
                    resultMin = result;
                }
            }
        }

        // Store the component in the ray trace result when we found one.
        if (resultMin != null) {
            return new BlockRayTraceResultComponent(resultMin, componentMin);
        }

        return null;
    }

    /**
     * Do a ray trace for the current look direction of the player.
     * @param pos The block position to perform a ray trace for.
     * @param entity The entity.
     * @return A holder object with information on the ray tracing.
     */
    @Nullable
    public BlockRayTraceResultComponent rayTrace(BlockPos pos, @Nullable Entity entity) {
        if(entity == null) {
            return null;
        }
        ModifiableAttributeInstance reachDistanceAttribute = entity instanceof LivingEntity ? ((LivingEntity) entity).getAttribute(ForgeMod.REACH_DISTANCE.get()) : null;
        double reachDistance = reachDistanceAttribute == null ? 5 : reachDistanceAttribute.getValue();

        double eyeHeight = entity.getEntityWorld().isRemote() ? entity.getEyeHeight() : entity.getEyeHeight(); // Client removed :  - player.getDefaultEyeHeight()
        Vector3d lookVec = entity.getLookVec();
        Vector3d origin = new Vector3d(entity.getPosX(), entity.getPosY() + eyeHeight, entity.getPosZ());
        Vector3d direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

        return rayTrace(origin, direction, pos);
    }

    @Override
    public double getAllowedOffset(AxisRotation rotation, AxisAlignedBB axisAlignedBB, double range) {
        boolean first = true;
        double valueBest = 0D;
        for (VoxelShape shape : this) {
            double value = shape.getAllowedOffset(rotation, axisAlignedBB, range);
            if (range > 0) {
                if (first || value < valueBest) {
                    valueBest = value;
                    first = false;
                }
            } else {
                if (first || value > valueBest) {
                    valueBest = value;
                    first = false;
                }
            }
        }
        return valueBest;
    }

    public static class Part extends VoxelShapePart implements Iterable<VoxelShapePart> {

        private final Collection<VoxelShapePart> entries;

        public Part(Collection<VoxelShapePart> entries) {
            super(0, 0, 0);
            this.entries = entries;
        }

        @Override
        public Iterator<VoxelShapePart> iterator() {
            return entries.iterator();
        }

        @Override
        public boolean contains(int x, int y, int z) {
            for (VoxelShapePart part : this) {
                if (part.contains(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isFilled(int x, int y, int z) {
            for (VoxelShapePart part : this) {
                if (part.isFilled(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void setFilled(int x, int y, int z, boolean b, boolean b1) {
            for (VoxelShapePart part : this) {
                part.setFilled(x, y, z, b, b1);
            }
        }

        @Override
        public int getStart(Direction.Axis axis) {
            boolean first = true;
            int startMin = 0;
            for (VoxelShapePart part : this) {
                int start = part.getStart(axis);
                if (first || start < startMin) {
                    startMin = start;
                    first = false;
                }
            }
            return startMin;
        }

        @Override
        public int getEnd(Direction.Axis axis) {
            boolean first = true;
            int endMax = 0;
            for (VoxelShapePart part : this) {
                int end = part.getEnd(axis);
                if (first || end > endMax) {
                    endMax = end;
                    first = false;
                }
            }
            return endMax;
        }

        @Override
        public int getSize(Direction.Axis axis) {
            boolean first = true;
            int sizeMax = 0;
            for (VoxelShapePart part : this) {
                int size = part.getSize(axis);
                if (first || size > sizeMax) {
                    sizeMax = size;
                    first = false;
                }
            }
            return sizeMax;
        }

        @Override
        public void forEachBox(ILineConsumer consumer, boolean p_197831_2_) {
            for (VoxelShapePart part : this) {
                part.forEachBox(consumer, p_197831_2_);
            }
        }
    }

    public static interface IComponent {

        /**
         * @param blockState The block state.
         * @param world The world.
         * @param blockPos The position.
         * @return Unique identifier for the component's state.
         */
        public String getStateId(BlockState blockState, IBlockReader world, BlockPos blockPos);

        /**
         * Get the shape of this component.
         * @param blockState The block state.
         * @param world The world.
         * @param blockPos The position.
         * @param selectionContext The selection context.
         * @return The shape.
         */
        public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext);

        /**
         * Get the pick block item.
         * @param world The world
         * @param pos The position
         * @return The item.
         */
        public ItemStack getPickBlock(World world, BlockPos pos);

        /**
         * Destroy this component
         * @param world The world
         * @param pos The position
         * @param player The player destroying the component.
         * @param saveState If the component state should be saved in the dropped item.
         * @return If the complete block was destroyed
         */
        public boolean destroy(World world, BlockPos pos, PlayerEntity player, boolean saveState);

        /**
         * @param world The world
         * @param pos The position
         * @return The model that will be used to render the breaking overlay.
         */
        @OnlyIn(Dist.CLIENT)
        @Nullable
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos);

        /**
         * When this component has been activated.
         * @param state The block state.
         * @param world The world.
         * @param blockPos The position.
         * @param player The player.
         * @param hand The hand.
         * @param hit The ray trace result.
         * @return Action result.
         */
        public ActionResultType onBlockActivated(BlockState state, World world, BlockPos blockPos, PlayerEntity player,
                                                 Hand hand, BlockRayTraceResultComponent hit);

    }

}
