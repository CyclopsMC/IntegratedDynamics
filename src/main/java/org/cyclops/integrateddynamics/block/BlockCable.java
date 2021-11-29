package org.cyclops.integrateddynamics.block;

import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.ModelProperty;
import org.cyclops.cyclopscore.block.BlockTile;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.client.model.IDynamicModelElement;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
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
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;


/**
 * A block that is built up from different parts.
 * This block refers to a ticking part entity.
 * @author rubensworks
 */
public class BlockCable extends BlockTile implements IDynamicModelElement, IWaterLoggable {

    public static final float BLOCK_HARDNESS = 3.0F;
    public static final Material BLOCK_MATERIAL = Material.GLASS;

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
    public final static AxisAlignedBB CABLE_CENTER_BOUNDINGBOX = new AxisAlignedBB(
            CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX);
    private final static EnumFacingMap<AxisAlignedBB> CABLE_SIDE_BOUNDINGBOXES = EnumFacingMap.forAllValues(
            new AxisAlignedBB(CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX), // DOWN
            new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX), // UP
            new AxisAlignedBB(CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN), // NORTH
            new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1), // SOUTH
            new AxisAlignedBB(0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX), // WEST
            new AxisAlignedBB(CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX) // EAST
    );

    private final VoxelShapeComponentsFactory voxelShapeComponentsFactory = new VoxelShapeComponentsFactory(
            new VoxelShapeComponentsFactoryHandlerCableCenter(),
            new VoxelShapeComponentsFactoryHandlerCableConnections(),
            new VoxelShapeComponentsFactoryHandlerFacade(),
            new VoxelShapeComponentsFactoryHandlerParts()
    );

    @OnlyIn(Dist.CLIENT)
    @Icon(location = "block/cable")
    public TextureAtlasSprite texture;
    @Setter
    private boolean disableCollisionBox = false;

    public BlockCable(Properties properties) {
        super(properties, TileMultipartTicking::new);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, false));
        if (MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getIconProvider().registerIconHolderObject(this);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        NetworkHelpers.onElementProviderBlockNeighborChange((World) worldIn, currentPos, facingState.getBlock(), facing, facingPos);
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        FluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState().with(WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return !state.get(BlockStateProperties.WATERLOGGED) && fluidIn == Fluids.WATER
                && !CableHelpers.hasFacade(worldIn, pos);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos, connectedCables);
        CableHelpers.setRemovingCable(false);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, pos, ISelectionContext.forEntity(player))
                .rayTrace(pos, player);
        if (rayTraceResult != null && rayTraceResult.getComponent().destroy(world, pos, player, false)) {
            return false;
        }
        return rayTraceResult != null && super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            Collection<Direction> connectedCables = null;
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoving(world, blockPos, false, false);
                connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
            }
            super.onReplaced(state, world, blockPos, newState, isMoving);
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            }
        } else {
            super.onReplaced(state, world, blockPos, newState, isMoving);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class).orElse(null);
        if(tile != null) {
            BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, pos, ISelectionContext.forEntity(player))
                    .rayTrace(pos, player);
            if(rayTraceResult != null) {
                ActionResultType actionResultType = rayTraceResult.getComponent().onBlockActivated(state, world, pos, player, hand, rayTraceResult);
                if (actionResultType.isSuccessOrConsume()) {
                    return actionResultType;
                }
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);
        if (!world.isRemote() && !state.hasTileEntity()) {
            CableHelpers.onCableAdded(world, pos);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isRemote()) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, net.minecraft.util.math.RayTraceResult target, IBlockReader world,
                                  BlockPos blockPos, PlayerEntity player) {
        BlockRayTraceResultComponent rayTraceResult = getSelectedShape(state, world, blockPos, ISelectionContext.forEntity(player))
                .rayTrace(blockPos, player);
        if(rayTraceResult != null) {
            return rayTraceResult.getComponent().getPickBlock((World) world, blockPos);
        }
        return getItem(world, blockPos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock, null, fromPos);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof World) {
            NetworkHelpers.onElementProviderBlockNeighborChange((World) world, pos, world.getBlockState(neighbor).getBlock(), null, neighbor);
        }
    }

    @Override
    public void observedNeighborChange(BlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, observerPos, changedBlock, null, changedBlockPos);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        super.tick(state, world, pos, rand);
        TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class)
                .ifPresent(tile -> {
                    for (Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : tile
                            .getPartContainer().getPartData().entrySet()) {
                        updateTickPart(entry.getValue().getPart(), world, pos, entry.getValue().getState(), rand);
                    }
                });
    }

    protected void updateTickPart(IPartType partType, World world, BlockPos pos, IPartState partState, Random random) {
        partType.updateTick(world, pos, partState, random);
    }

    /* --------------- Start shapes and rendering --------------- */

    public AxisAlignedBB getCableBoundingBox(Direction side) {
        if (side == null) {
            return CABLE_CENTER_BOUNDINGBOX;
        } else {
            return CABLE_SIDE_BOUNDINGBOXES.get(side);
        }
    }

    public VoxelShapeComponents getSelectedShape(BlockState blockState, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return voxelShapeComponentsFactory.createShape(blockState, world, pos, selectionContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        VoxelShapeComponents selectedShape = getSelectedShape(state, world, pos, selectionContext);
        BlockRayTraceResultComponent rayTraceResult = selectedShape.rayTrace(pos, selectionContext.getEntity());
        if (rayTraceResult != null) {
            return rayTraceResult.getComponent().getShape(state, world, pos, selectionContext);
        }
        return getSelectedShape(state, world, pos, selectionContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_, ISelectionContext p_220071_4_) {
        if(disableCollisionBox) {
            return VoxelShapes.empty();
        }

        // Combine all VoxelShapes using IBooleanFunction.OR,
        // because for some reason our VoxelShapeComponents aggregator does not handle collisions properly.
        // This can probably be fixed, but I spent too much time on this already, and the current solution works just fine.
        VoxelShapeComponents voxelShapeComponents = (VoxelShapeComponents) super.getCollisionShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
        Iterator<VoxelShape> it = voxelShapeComponents.iterator();
        if (!it.hasNext()) {
            return VoxelShapes.empty();
        }
        VoxelShape shape = it.next();
        while (it.hasNext()) {
            shape = VoxelShapes.combine(shape, it.next(), IBooleanFunction.OR);
        }
        return shape.simplify();
    }

    @Override
    public int getOpacity(BlockState blockState, IBlockReader world, BlockPos pos) {
        return CableHelpers.hasFacade(world, pos) && !CableHelpers.isLightTransparent(world, pos, null) ? 255 : 0;
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState) {
        return BlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState blockState, World world, RayTraceResult target, ParticleManager particleManager) {
        BlockPos blockPos = ((BlockRayTraceResult) target).getPos();
        if(CableHelpers.hasFacade(world, blockPos)) {
            CableHelpers.getFacade(world, blockPos)
                    .ifPresent(facadeState -> RenderHelpers.addBlockHitEffects(particleManager, (ClientWorld) world, facadeState, blockPos, ((BlockRayTraceResult) target).getFace()));
            return true;
        } else {
            return super.addHitEffects(blockState, world, target, particleManager);
        }
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
        return CableHelpers.getFacade(world, pos).isPresent();
    }

    /* --------------- Start IDynamicRedstone --------------- */

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(BlockState blockState) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState blockState, IBlockReader world, BlockPos pos, Direction side) {
        if(side == null) {
            for(Direction dummySide : Direction.values()) {
                IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, dummySide, DynamicRedstoneConfig.CAPABILITY).orElse(null);
                if(dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput())) {
                    return true;
                }
            }
            return false;
        }
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side) {
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null && dynamicRedstone.isStrong() ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side) {
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
        return dynamicRedstone != null ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    /* --------------- Start IDynamicLight --------------- */

    @Override
    public int getLightValue(BlockState blockState, IBlockReader world, BlockPos pos) {
        int light = 0;
        for(Direction side : Direction.values()) {
            IDynamicLight dynamicLight = TileHelpers.getCapability(world, pos, side, DynamicLightConfig.CAPABILITY).orElse(null);
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
    public IBakedModel createDynamicModel(ModelBakeEvent event) {
        CableModel model = new CableModel();
        event.getModelRegistry().put(new ModelResourceLocation(getRegistryName(), "waterlogged=false"), model);
        event.getModelRegistry().put(new ModelResourceLocation(getRegistryName(), "waterlogged=true"), model);
        event.getModelRegistry().put(new ModelResourceLocation(getRegistryName(), "inventory"), model);
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockColor implements IBlockColor {
        @Override
        public int getColor(BlockState blockState, @Nullable IBlockDisplayReader world, @Nullable BlockPos blockPos, int color) {
            // Only modify color if we have a facade
            return world == null || blockPos == null ?
                -1 : CableHelpers.getFacade(world, blockPos)
                    .map(facadeState -> Minecraft.getInstance().getBlockColors().getColor(facadeState, world, blockPos, color))
                    .orElse(-1);
        }
    }

}
