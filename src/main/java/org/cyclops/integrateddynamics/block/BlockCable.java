package org.cyclops.integrateddynamics.block;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.cyclops.cyclopscore.block.BlockWithEntity;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.shapes.*;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.client.model.IRenderState;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A block that is built up from different parts.
 * This block refers to a ticking part entity.
 * @author rubensworks
 */
public class BlockCable extends BlockWithEntity implements SimpleWaterloggedBlock {

    public static final MapCodec<BlockCable> CODEC = simpleCodec(BlockCable::new);

    public static final float BLOCK_HARDNESS = 3.0F;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Model Properties
    public static final ModelProperty<Boolean> REALCABLE = new ModelProperty<>();
    public static final ModelProperty<Boolean>[] CONNECTED = new ModelProperty[6];
    public static final ModelProperty<PartRenderPosition>[] PART_RENDERPOSITIONS = new ModelProperty[6];
    public static final ModelProperty<Optional<BlockState>> FACADE = new ModelProperty<>();
    static {
        for(Direction side : Direction.values()) {
            CONNECTED[side.ordinal()] = new ModelProperty<>();
            PART_RENDERPOSITIONS[side.ordinal()] = new ModelProperty<>();
        }
    }
    public static final ModelProperty<IPartContainer> PARTCONTAINER = new ModelProperty<>();
    public static final ModelProperty<IRenderState> RENDERSTATE = new ModelProperty<>();

    // Collision boxes
    public final static AABB CABLE_CENTER_BOUNDINGBOX = new AABB(
            CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX);
    private final static EnumFacingMap<AABB> CABLE_SIDE_BOUNDINGBOXES = EnumFacingMap.forAllValues(
            new AABB(CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX), // DOWN
            new AABB(CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX), // UP
            new AABB(CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN), // NORTH
            new AABB(CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1), // SOUTH
            new AABB(0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX), // WEST
            new AABB(CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX) // EAST
    );

    private final VoxelShapeComponentsFactory voxelShapeComponentsFactory = new VoxelShapeComponentsFactory(
            new VoxelShapeComponentsFactoryHandlerCableCenter(),
            new VoxelShapeComponentsFactoryHandlerCableConnections(),
            new VoxelShapeComponentsFactoryHandlerParts(),
            new VoxelShapeComponentsFactoryHandlerFacade()
    );

    private boolean disableCollisionBox = false;

    public void setDisableCollisionBox(boolean disableCollisionBox) {
        this.disableCollisionBox = disableCollisionBox;
    }

    /**
     * Flag to skip expensive network initialization during bulk cable placement.
     * When true, onCableAdded calls are skipped in onPlace.
     * Network initialization should be done manually after all cables are placed.
     */
    public static boolean SKIP_NETWORK_INIT = false;

    public BlockCable(Properties properties) {
        super(properties, BlockEntityMultipartTicking::new);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState p_60576_) {
        return true;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_MULTIPART_TICKING.get(), new BlockEntityMultipartTicking.Ticker<>());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            if (state.getValue(WATERLOGGED)) {
                serverLevel.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            NetworkHelpers.onElementProviderBlockNeighborChange(serverLevel, pos, direction);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(@org.jetbrains.annotations.Nullable LivingEntity entity, BlockGetter worldIn, BlockPos pos, BlockState blockState, Fluid fluidIn) {
        return !blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidIn == Fluids.WATER
                && !(worldIn instanceof ILevelExtension levelExtension && CableHelpers.hasFacade(levelExtension, pos, blockState));
    }

    @Override
    public void onBlockExploded(BlockState state, ServerLevel world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false, state, world.getBlockEntity(blockPos));
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos);
        CableHelpers.setRemovingCable(false);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, pos, CollisionContext.of(player))
                .rayTrace(pos, player);
        if (rayTraceResult != null && rayTraceResult.getComponent().destroy(world, pos, player, false)) {
            return false;
        }
        return rayTraceResult != null && super.onDestroyedByPlayer(state, world, pos, player, toolStack, willHarvest, fluid);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        if (!CableHelpers.isRemovingCable() && !SKIP_NETWORK_INIT) {
            CableHelpers.onCableRemoved(level, pos);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        BlockEntityMultipartTicking tile = IModHelpers.get().getBlockEntityHelpers().get(pLevel, pPos, BlockEntityMultipartTicking.class).orElse(null);
        if(tile != null) {
            BlockRayTraceResultComponent rayTraceResult = getSelectedShape(pState, pLevel, pPos, CollisionContext.of(pPlayer))
                    .rayTrace(pPos, pPlayer);
            if(rayTraceResult != null) {
                InteractionResult actionResultType = rayTraceResult.getComponent().onBlockActivated(pState, pLevel, pPos, pPlayer, pHand, rayTraceResult);
                if (actionResultType.consumesAction()) {
                    return actionResultType;
                }
            }
        }
        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide() && !SKIP_NETWORK_INIT) {
            ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, pos, null).orElse(null);
            if (cableFakeable != null && cableFakeable.isRealCable()) {
                CableHelpers.onCableAdded(world, pos);
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide()) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, level, pos, CollisionContext.of(player))
                .rayTrace(pos, player);
        if(rayTraceResult != null && level instanceof Level levelReal) {
            return rayTraceResult.getComponent().getCloneItemStack(levelReal, pos);
        }
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @org.jetbrains.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        NetworkHelpers.onElementProviderBlockNeighborChange(level, pos, orientation != null ? orientation.getFront().getOpposite() : null);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof Level level) {
            NetworkHelpers.onElementProviderBlockNeighborChange(level, pos, null);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        super.tick(state, world, pos, rand);
        IModHelpers.get().getBlockEntityHelpers().get(world, pos, BlockEntityMultipartTicking.class)
                .ifPresent(tile -> {
                    for (Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : tile
                            .getPartContainer().getPartData().entrySet()) {
                        updateTickPart(entry.getValue().getPart(), world, pos, entry.getValue().getState(), rand);
                    }
                });
    }

    protected void updateTickPart(IPartType partType, Level world, BlockPos pos, IPartState partState, RandomSource random) {
        partType.updateTick(world, pos, partState, random);
    }

    /* --------------- Start shapes and rendering --------------- */

    public AABB getCableBoundingBox(Direction side) {
        if (side == null) {
            return CABLE_CENTER_BOUNDINGBOX;
        } else {
            return CABLE_SIDE_BOUNDINGBOXES.get(side);
        }
    }

    public VoxelShapeComponents getSelectedShape(BlockState blockState, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return voxelShapeComponentsFactory.createShape(blockState, world, pos, selectionContext);
    }

    private final Cache<String, VoxelShape> CACHE_COLLISION_SHAPES = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        VoxelShapeComponents selectedShape = getSelectedShape(state, world, pos, selectionContext);
        BlockRayTraceResultComponent rayTraceResult = selectedShape.rayTrace(pos, selectionContext instanceof EntityCollisionContext ? ((EntityCollisionContext) selectionContext).getEntity() : null);
        if (rayTraceResult != null) {
            return rayTraceResult.getComponent().getShape(state, world, pos, selectionContext);
        }

        String cableState = selectedShape.getStateId();

        // Cache the operations below, as they are too expensive to execute each render tick
        try {
            return CACHE_COLLISION_SHAPES.get(cableState, () -> {
            // Combine all VoxelShapes using IBooleanFunction.OR,
            // because for some reason our VoxelShapeComponents aggregator does not handle collisions properly.
            // This can probably be fixed, but I spent too much time on this already, and the current solution works just fine.
            Iterator<VoxelShape> it = selectedShape.iterator();
            if (!it.hasNext()) {
                return Shapes.empty();
            }
            VoxelShape shape = it.next();
            while (it.hasNext()) {
                shape = Shapes.join(shape, it.next(), BooleanOp.OR);
            }
            return shape.optimize();
            });
        } catch (Exception e) {
            return Helpers.sneakyThrow(e);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return (disableCollisionBox || GeneralConfig.disableCableCollision) ? Shapes.empty() : super.getCollisionShape(blockState, world, pos, selectionContext);
    }

    @Override
    public boolean hasDynamicShape() {
        return BlockCableConfig.dynamicShape;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return this.getShape(pState, pLevel, pPos, new CollisionContextBlockSupport());
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
        return level instanceof RenderSectionRegion renderSectionRegion && CableHelpers.getFacade(renderSectionRegion.level, pos, state).isPresent();
    }

    /* --------------- Start IDynamicRedstone --------------- */

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if (world instanceof ILevelExtension levelExtension) {
            if (side == null) {
                for (Direction dummySide : Direction.values()) {
                    IDynamicRedstone dynamicRedstone = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(levelExtension, pos, dummySide, Capabilities.DynamicRedstone.BLOCK).orElse(null);
                    if (dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput())) {
                        return true;
                    }
                }
                return false;
            }
            IDynamicRedstone dynamicRedstone = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(levelExtension, pos, side.getOpposite(), Capabilities.DynamicRedstone.BLOCK).orElse(null);
            return dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput());
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if (world instanceof ILevelExtension levelExtension) {
            IDynamicRedstone dynamicRedstone = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(levelExtension, pos, side.getOpposite(), Capabilities.DynamicRedstone.BLOCK).orElse(null);
            return dynamicRedstone != null && dynamicRedstone.isDirect() ? dynamicRedstone.getRedstoneLevel() : 0;
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if (world instanceof ILevelExtension levelExtension) {
            IDynamicRedstone dynamicRedstone = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(levelExtension, pos, side.getOpposite(), Capabilities.DynamicRedstone.BLOCK).orElse(null);
            return dynamicRedstone != null ? dynamicRedstone.getRedstoneLevel() : 0;
        }
        return 0;
    }

    /* --------------- Start IDynamicLight --------------- */

    @Override
    public int getLightEmission(BlockState blockState, BlockGetter world, BlockPos pos) {
        int light = 0;
        if (world instanceof ILevelExtension levelExtension) {
            for (Direction side : Direction.values()) {
                IDynamicLight dynamicLight = levelExtension.getCapability(Capabilities.DynamicLight.BLOCK, pos, blockState, null, side);
                if (dynamicLight != null) {
                    light = Math.max(light, dynamicLight.getLightLevel());
                }
            }
        }
        return light;
    }

}
