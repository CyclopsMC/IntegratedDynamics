package org.cyclops.integrateddynamics.block;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.cyclopscore.block.BlockWithEntity;
import org.cyclops.cyclopscore.client.model.IDynamicModelElement;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.shapes.VoxelShapeComponentsFactoryHandlerCableCenter;
import org.cyclops.integrateddynamics.block.shapes.VoxelShapeComponentsFactoryHandlerCableConnections;
import org.cyclops.integrateddynamics.block.shapes.VoxelShapeComponentsFactoryHandlerFacade;
import org.cyclops.integrateddynamics.block.shapes.VoxelShapeComponentsFactoryHandlerParts;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.client.model.IRenderState;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponentsFactory;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A block that is built up from different parts.
 * This block refers to a ticking part entity.
 * @author rubensworks
 */
public class BlockCable extends BlockWithEntity implements IDynamicModelElement, SimpleWaterloggedBlock {

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

    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite texture;
    @Setter
    private boolean disableCollisionBox = false;

    public BlockCable(Properties properties) {
        super(properties, BlockEntityMultipartTicking::new);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
        if (MinecraftHelpers.isClientSide()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postTextureStitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void postTextureStitch(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            texture = event.getAtlas().getSprite(new ResourceLocation(Reference.MOD_ID, "block/cable"));
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState p_60576_) {
        return true;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_MULTIPART_TICKING, new BlockEntityMultipartTicking.Ticker<>());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        NetworkHelpers.onElementProviderBlockNeighborChange((Level) worldIn, currentPos, facingState.getBlock(), facing, facingPos);
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
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
    public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return !state.getValue(BlockStateProperties.WATERLOGGED) && fluidIn == Fluids.WATER
                && !CableHelpers.hasFacade(worldIn, pos);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos, connectedCables);
        CableHelpers.setRemovingCable(false);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, pos, CollisionContext.of(player))
                .rayTrace(pos, player);
        if (rayTraceResult != null && rayTraceResult.getComponent().destroy(world, pos, player, false)) {
            return false;
        }
        return rayTraceResult != null && super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            Collection<Direction> connectedCables = null;
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoving(world, blockPos, false, false);
                connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
            }
            super.onRemove(state, world, blockPos, newState, isMoving);
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            }
        } else {
            super.onRemove(state, world, blockPos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        BlockEntityMultipartTicking tile = BlockEntityHelpers.get(world, pos, BlockEntityMultipartTicking.class).orElse(null);
        if(tile != null) {
            BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, pos, CollisionContext.of(player))
                    .rayTrace(pos, player);
            if(rayTraceResult != null) {
                InteractionResult actionResultType = rayTraceResult.getComponent().onBlockActivated(state, world, pos, player, hand, rayTraceResult);
                if (actionResultType.consumesAction()) {
                    return actionResultType;
                }
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide() && !state.hasBlockEntity()) {
            CableHelpers.onCableAdded(world, pos);
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
    public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target, BlockGetter world,
                                  BlockPos blockPos, Player player) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, blockPos, CollisionContext.of(player))
                .rayTrace(blockPos, player);
        if(rayTraceResult != null) {
            return rayTraceResult.getComponent().getCloneItemStack((Level) world, blockPos);
        }
        return getCloneItemStack(world, blockPos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock, null, fromPos);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof Level) {
            NetworkHelpers.onElementProviderBlockNeighborChange((Level) world, pos, world.getBlockState(neighbor).getBlock(), null, neighbor);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        super.tick(state, world, pos, rand);
        BlockEntityHelpers.get(world, pos, BlockEntityMultipartTicking.class)
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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        VoxelShapeComponents selectedShape = getSelectedShape(state, world, pos, selectionContext);
        BlockRayTraceResultComponent rayTraceResult = selectedShape.rayTrace(pos, selectionContext instanceof EntityCollisionContext ? ((EntityCollisionContext) selectionContext).getEntity() : null);
        if (rayTraceResult != null) {
            return rayTraceResult.getComponent().getShape(state, world, pos, selectionContext);
        }
        return selectedShape;
    }

    private final Cache<String, VoxelShape> CACHE_COLLISION_SHAPES = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    @SneakyThrows
    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        if(disableCollisionBox) {
            return Shapes.empty();
        }

        VoxelShapeComponents voxelShapeComponents = (VoxelShapeComponents) super.getCollisionShape(blockState, world, pos, selectionContext);
        String cableState = voxelShapeComponents.getStateId();

        // Cache the operations below, as they are too expensive to execute each render tick
        return CACHE_COLLISION_SHAPES.get(cableState, () -> {
            // Combine all VoxelShapes using IBooleanFunction.OR,
            // because for some reason our VoxelShapeComponents aggregator does not handle collisions properly.
            // This can probably be fixed, but I spent too much time on this already, and the current solution works just fine.
            Iterator<VoxelShape> it = voxelShapeComponents.iterator();
            if (!it.hasNext()) {
                return Shapes.empty();
            }
            VoxelShape shape = it.next();
            while (it.hasNext()) {
                shape = Shapes.join(shape, it.next(), BooleanOp.OR);
            }
            return shape.optimize();
        });
    }

    @Override
    public boolean hasDynamicShape() {
        return BlockCableConfig.dynamicShape;
    }

    @Override
    public int getLightBlock(BlockState blockState, BlockGetter world, BlockPos pos) {
        if (CableHelpers.isLightTransparent(world, pos, null)) {
            return 0;
        }
        return CableHelpers.getFacade(world, pos)
                .map(facade -> facade.getLightBlock(world, pos))
                .orElse(0);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState blockState, Level world, HitResult target, ParticleEngine particleManager) {
                BlockPos blockPos = ((BlockHitResult) target).getBlockPos();
                if(CableHelpers.hasFacade(world, blockPos)) {
                    CableHelpers.getFacade(world, blockPos)
                            .ifPresent(facadeState -> RenderHelpers.addBlockHitEffects(particleManager, (ClientLevel) world, facadeState, blockPos, ((BlockHitResult) target).getDirection()));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return CableHelpers.getFacade(world, pos).isPresent();
    }

    /* --------------- Start IDynamicRedstone --------------- */

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if(side == null) {
            for(Direction dummySide : Direction.values()) {
                IDynamicRedstone dynamicRedstone = BlockEntityHelpers.getCapability(world, pos, dummySide, DynamicRedstoneConfig.CAPABILITY).orElse(null);
                if(dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput())) {
                    return true;
                }
            }
            return false;
        }
        IDynamicRedstone dynamicRedstone = BlockEntityHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        IDynamicRedstone dynamicRedstone = BlockEntityHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null && dynamicRedstone.isDirect() ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        IDynamicRedstone dynamicRedstone = BlockEntityHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    /* --------------- Start IDynamicLight --------------- */

    @Override
    public int getLightEmission(BlockState blockState, BlockGetter world, BlockPos pos) {
        int light = 0;
        for(Direction side : Direction.values()) {
            IDynamicLight dynamicLight = BlockEntityHelpers.getCapability(world, pos, side, DynamicLightConfig.CAPABILITY).orElse(null);
            if (dynamicLight != null) {
                light = Math.max(light, dynamicLight.getLightLevel());
            }
        }
        return light;
    }

    /* --------------- Start Dynamic model --------------- */

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BakedModel createDynamicModel(ModelEvent.ModifyBakingResult event) {
        CableModel model = new CableModel();
        ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(this);
        event.getModels().put(new ModelResourceLocation(registryName, "waterlogged=false"), model);
        event.getModels().put(new ModelResourceLocation(registryName, "waterlogged=true"), model);
        event.getModels().put(new ModelResourceLocation(registryName, "inventory"), model);
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockColor implements net.minecraft.client.color.block.BlockColor {
        @Override
        public int getColor(BlockState blockState, @Nullable BlockAndTintGetter world, @Nullable BlockPos blockPos, int color) {
            // Only modify color if we have a facade
            return world == null || blockPos == null ?
                -1 : CableHelpers.getFacade(world, blockPos)
                    .map(facadeState -> Minecraft.getInstance().getBlockColors().getColor(facadeState, world, blockPos, color))
                    .orElse(-1);
        }
    }

}
