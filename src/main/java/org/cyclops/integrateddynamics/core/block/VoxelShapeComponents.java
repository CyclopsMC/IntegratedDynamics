package org.cyclops.integrateddynamics.core.block;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForgeMod;
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

    protected static DiscreteVoxelShape createInnerPart(Collection<Pair<VoxelShape, IComponent>> entries) {
        return new Part(entries.stream()
                .map(pair -> pair.getLeft().shape)
                .collect(Collectors.toList()));
    }

    public static VoxelShapeComponents create(BlockState blockState, BlockGetter world, BlockPos blockPos,
                                              CollisionContext selectionContext, List<IComponent> components) {
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
    public double min(Direction.Axis axis) {
        boolean first = true;
        double startMin = 0;
        for (VoxelShape shape : this) {
            double start = shape.min(axis);
            if (first || start < startMin) {
                startMin = start;
                first = false;
            }
        }
        return startMin;
    }

    @Override
    public double max(Direction.Axis axis) {
        boolean first = true;
        double endMax = 0;
        for (VoxelShape shape : this) {
            double end = shape.max(axis);
            if (first || end > endMax) {
                endMax = end;
                first = false;
            }
        }
        return endMax;
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        DoubleArrayList values = new DoubleArrayList();
        for (VoxelShape shape : this) {
            values.addAll(shape.getCoords(axis));
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
    public VoxelShape move(double x, double y, double z) {
        List<Pair<VoxelShape, IComponent>> entries = Lists.newArrayList();
        for (Pair<VoxelShape, IComponent> entry : this.entries) {
            entries.add(Pair.of(entry.getLeft().move(x, y, z), entry.getRight()));
        }
        return new VoxelShapeComponents(entries, this.stateId);
    }

    @Override
    public void forAllEdges(Shapes.DoubleLineConsumer consumer) {
        for (VoxelShape shape : this) {
            shape.forAllEdges(consumer);
        }
    }

    @Override
    public void forAllBoxes(Shapes.DoubleLineConsumer consumer) {
        for (VoxelShape shape : this) {
            shape.forAllBoxes(consumer);
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

    @Nullable
    @Override
    public BlockRayTraceResultComponent clip(Vec3 startVec, Vec3 endVec, BlockPos pos) {
        // Find component with shape that is closest to the startVec
        double distanceMin = Double.POSITIVE_INFINITY;
        VoxelShapeComponents.IComponent componentMin = null;
        BlockHitResult resultMin = null;

        for (Pair<VoxelShape, IComponent> entry : entries) {
            VoxelShape shape = entry.getLeft();
            BlockHitResult result = shape.clip(startVec, endVec, pos);
            if (result != null) {
                double distance = result.getLocation().distanceToSqr(startVec);
                if (distance < distanceMin) {
                    // If the previous match was a part and the current one is a facade,
                    // check if the direction of that part matches with the matched face of the facade,
                    // and if so, don't match the facade,
                    // because we want the user to be able to select parts within facades.
                    if (resultMin == null || !(entry.getRight().isRaytraceLastForFace() && componentMin.getRaytraceDirection() == result.getDirection())) {
                        distanceMin = distance;
                        componentMin = entry.getRight();
                        resultMin = result;
                    }
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
        AttributeInstance reachDistanceAttribute = entity instanceof LivingEntity ? ((LivingEntity) entity).getAttribute(NeoForgeMod.BLOCK_REACH.value()) : null;
        double reachDistance = reachDistanceAttribute == null ? 5 : reachDistanceAttribute.getValue();

        double eyeHeight = entity.getCommandSenderWorld().isClientSide() ? entity.getEyeHeight() : entity.getEyeHeight(); // Client removed :  - player.getDefaultEyeHeight()
        Vec3 lookVec = entity.getLookAngle();
        Vec3 origin = new Vec3(entity.getX(), entity.getY() + eyeHeight, entity.getZ());
        Vec3 direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

        return clip(origin, direction, pos);
    }

    @Override
    public double collideX(AxisCycle rotation, AABB axisAlignedBB, double range) {
        boolean first = true;
        double valueBest = 0D;
        for (VoxelShape shape : this) {
            double value = shape.collideX(rotation, axisAlignedBB, range);
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

    public static class Part extends DiscreteVoxelShape implements Iterable<DiscreteVoxelShape> {

        private final Collection<DiscreteVoxelShape> entries;

        public Part(Collection<DiscreteVoxelShape> entries) {
            super(0, 0, 0);
            this.entries = entries;
        }

        @Override
        public Iterator<DiscreteVoxelShape> iterator() {
            return entries.iterator();
        }

        @Override
        public boolean isFullWide(int x, int y, int z) {
            for (DiscreteVoxelShape part : this) {
                if (part.isFullWide(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isFull(int x, int y, int z) {
            for (DiscreteVoxelShape part : this) {
                if (part.isFull(x, y, z)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void fill(int x, int y, int z) {
            for (DiscreteVoxelShape part : this) {
                part.fill(x, y, z);
            }
        }

        @Override
        public int firstFull(Direction.Axis axis) {
            boolean first = true;
            int startMin = 0;
            for (DiscreteVoxelShape part : this) {
                int start = part.firstFull(axis);
                if (first || start < startMin) {
                    startMin = start;
                    first = false;
                }
            }
            return startMin;
        }

        @Override
        public int lastFull(Direction.Axis axis) {
            boolean first = true;
            int endMax = 0;
            for (DiscreteVoxelShape part : this) {
                int end = part.lastFull(axis);
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
            for (DiscreteVoxelShape part : this) {
                int size = part.getSize(axis);
                if (first || size > sizeMax) {
                    sizeMax = size;
                    first = false;
                }
            }
            return sizeMax;
        }

        @Override
        public void forAllBoxes(IntLineConsumer consumer, boolean p_197831_2_) {
            for (DiscreteVoxelShape part : this) {
                part.forAllBoxes(consumer, p_197831_2_);
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
        public String getStateId(BlockState blockState, BlockGetter world, BlockPos blockPos);

        /**
         * Get the shape of this component.
         * @param blockState The block state.
         * @param world The world.
         * @param blockPos The position.
         * @param selectionContext The selection context.
         * @return The shape.
         */
        public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext);

        /**
         * Get the pick block item.
         * @param world The world
         * @param pos The position
         * @return The item.
         */
        public ItemStack getCloneItemStack(Level world, BlockPos pos);

        /**
         * Destroy this component
         * @param world The world
         * @param pos The position
         * @param player The player destroying the component.
         * @param saveState If the component state should be saved in the dropped item.
         * @return If the complete block was destroyed
         */
        public boolean destroy(Level world, BlockPos pos, Player player, boolean saveState);

        /**
         * @param world The world
         * @param pos The position
         * @return The model that will be used to render the breaking overlay.
         */
        @OnlyIn(Dist.CLIENT)
        @Nullable
        public BakedModel getBreakingBaseModel(Level world, BlockPos pos);

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
        public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos blockPos, Player player,
                                                 InteractionHand hand, BlockRayTraceResultComponent hit);

        /**
         * @return The direction this component points at.
         */
        @Nullable
        public Direction getRaytraceDirection();

        /**
         * @return If this component should only be raytraced if no other components matched for this face.
         */
        public boolean isRaytraceLastForFace();

    }

}
