package org.cyclops.integrateddynamics.block;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.UnlistedProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.collidable.CollidableComponentCableCenter;
import org.cyclops.integrateddynamics.block.collidable.CollidableComponentCableConnections;
import org.cyclops.integrateddynamics.block.collidable.CollidableComponentFacade;
import org.cyclops.integrateddynamics.block.collidable.CollidableComponentParts;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.client.model.IRenderState;
import org.cyclops.integrateddynamics.core.block.CollidableComponent;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.block.ICollidableParent;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A block that is buildReader up from different parts.
 * This block refers to a ticking part entity.
 * @author rubensworks
 */
public class BlockCable extends ConfigurableBlockContainer implements ICollidable<EnumFacing>, ICollidableParent {

    public static final float BLOCK_HARDNESS = 3.0F;
    public static final Material BLOCK_MATERIAL = Material.GLASS;

    // Properties
    @BlockProperty
    public static final IUnlistedProperty<Boolean> REALCABLE = Properties.toUnlisted(PropertyBool.create("realcable"));
    @BlockProperty
    public static final IUnlistedProperty<Boolean>[] CONNECTED = new IUnlistedProperty[6];
    @BlockProperty
    public static final IUnlistedProperty<PartRenderPosition>[] PART_RENDERPOSITIONS = new IUnlistedProperty[6];
    @BlockProperty
    public static final IUnlistedProperty<Optional> FACADE = new UnlistedProperty<>("facade", Optional.class);
    static {
        for(EnumFacing side : EnumFacing.values()) {
            CONNECTED[side.ordinal()] = Properties.toUnlisted(PropertyBool.create("connect-" + side.getName()));
            PART_RENDERPOSITIONS[side.ordinal()] = new UnlistedProperty<>("partRenderPosition-" + side.getName(), PartRenderPosition.class);
        }
    }
    @BlockProperty
    public static final IUnlistedProperty<IPartContainer> PARTCONTAINER = new UnlistedProperty<>("partcontainer", IPartContainer.class);
    @BlockProperty
    public static final IUnlistedProperty<IRenderState> RENDERSTATE = new UnlistedProperty<>("renderState", IRenderState.class);

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

    private static final List<IComponent<EnumFacing, BlockCable>> COLLIDABLE_COMPONENTS = Lists.newArrayList();
    private static final IComponent<EnumFacing, BlockCable> FACADE_COMPONENT = new CollidableComponentFacade();
    private static final IComponent<EnumFacing, BlockCable> CABLECENTER_COMPONENT = new CollidableComponentCableCenter();
    private static final IComponent<EnumFacing, BlockCable> CABLECONNECTIONS_COMPONENT = new CollidableComponentCableConnections();
    private static final IComponent<EnumFacing, BlockCable> PARTS_COMPONENT = new CollidableComponentParts();
    static {
        COLLIDABLE_COMPONENTS.add(FACADE_COMPONENT);
        COLLIDABLE_COMPONENTS.add(CABLECENTER_COMPONENT);
        COLLIDABLE_COMPONENTS.add(CABLECONNECTIONS_COMPONENT);
        COLLIDABLE_COMPONENTS.add(PARTS_COMPONENT);
    }
    @SuppressWarnings("deprecation")
    @Delegate
    private ICollidable<EnumFacing> collidableComponent = new CollidableComponent<>(this, COLLIDABLE_COMPONENTS);

    private static BlockCable _instance = null;

    public static boolean IS_MCMP_CONVERTING = false;

    @SideOnly(Side.CLIENT)
    @Icon(location = "blocks/cable")
    public TextureAtlasSprite texture;
    @Setter
    private boolean disableCollisionBox = false;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockCable getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     */
    public BlockCable(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, BLOCK_MATERIAL, TileMultipartTicking.class);

        setHardness(BLOCK_HARDNESS);
        setSoundType(SoundType.METAL);
        if(MinecraftHelpers.isClientSide()) {
            eConfig.getMod().getIconProvider().registerIconHolderObject(this);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null && state.getBlock() == this) {
            return tile.getConnectionState();
        }
        return getDefaultState();
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos, EntityPlayer player) {
        CableHelpers.onCableRemoving(world, pos, true, false);
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        CableHelpers.onCableRemoving(world, pos, false, false);
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        if(!IS_MCMP_CONVERTING) { // Yes, this is a hack, we don't want this to be called after a MCMP block conversion
            CableHelpers.onCableRemoved(world, pos, CableHelpers.getExternallyConnectedCables(world, pos));
        }
        IS_MCMP_CONVERTING = false;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState blockState, int fortune) {
        // do nothing - leave drops empty
    }

    @Override
    public boolean removedByPlayer(IBlockState blockState, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if (rayTraceResult != null && rayTraceResult.getCollisionType() != null
                && rayTraceResult.getCollisionType().destroy(world, pos, rayTraceResult.getPositionHit(), player, false)) {
            return true;
        }
        return rayTraceResult != null && super.removedByPlayer(blockState, world, pos, player, willHarvest);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side,
                                    float hitX, float hitY, float hitZ) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        ItemStack heldItem = player.getHeldItem(hand);
        if(tile != null) {
            RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
            if(rayTraceResult != null) {
                EnumFacing positionHit = rayTraceResult.getPositionHit();
                if(rayTraceResult.getCollisionType() == FACADE_COMPONENT) {
                    if(WrenchHelpers.isWrench(player, heldItem, world, pos, side) && player.isSneaking()) {
                        if (!world.isRemote) {
                            FACADE_COMPONENT.destroy(world, pos, side, player, true);
                            world.notifyNeighborsOfStateChange(pos, this, true);
                        }
                        return true;
                    }
                    return false;
                } else if(rayTraceResult.getCollisionType() == PARTS_COMPONENT) {
                    if(WrenchHelpers.isWrench(player, heldItem, world, pos, side) && player.isSneaking()) {
                        // Remove part from cable
                        if (!world.isRemote) {
                            PARTS_COMPONENT.destroy(world, pos, rayTraceResult.getPositionHit(), player, true);
                            ItemBlockCable.playBreakSound(world, pos, BlockCable.getInstance().getDefaultState());
                        }
                        return true;
                    } else if(CableHelpers.isNoFakeCable(world, pos, side)) {
                        // Delegate activated call to part
                        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos, side);
                        return partContainer.getPart(positionHit).onPartActivated(world, pos,
                                partContainer.getPartState(positionHit), player, hand, heldItem, positionHit, hitX, hitY, hitZ);
                    }
                } else if ((rayTraceResult.getCollisionType() == CABLECONNECTIONS_COMPONENT
                            || rayTraceResult.getCollisionType() == CABLECENTER_COMPONENT)) {
                    if(CableHelpers.onCableActivated(world, pos, state, player, heldItem, side,
                            rayTraceResult.getCollisionType() == CABLECENTER_COMPONENT ? null : rayTraceResult.getPositionHit())) {
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            CableHelpers.onCableAdded(world, pos);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isRemote) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }

    @Override
    public boolean isDropBlockItem(IBlockAccess world, BlockPos pos, IBlockState blockState, int fortune) {
        return CableHelpers.isNoFakeCable(world, pos, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getPickBlock(IBlockState blockState, net.minecraft.util.math.RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if(rayTraceResult != null) {
            EnumFacing positionHit = rayTraceResult.getPositionHit();
            return rayTraceResult.getCollisionType().getPickBlock(world, pos, positionHit);
        }
        return getItem(world, pos, blockState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock, null);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
        if (world instanceof World) {
            NetworkHelpers.onElementProviderBlockNeighborChange((World) world, pos, world.getBlockState(neighbor).getBlock(), null);
        }
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, observerPos, changedBlock, null);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if (tile != null) {
            for (Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : tile
                    .getPartContainer().getPartData().entrySet()) {
                updateTickPart(entry.getValue().getPart(), world, pos, entry.getValue().getState(), rand);
            }
        }
    }

    protected void updateTickPart(IPartType partType, World world, BlockPos pos, IPartState partState, Random random) {
        partType.updateTick(world, pos, partState, random);
    }

    /* --------------- Start ICollidable and rendering --------------- */

    public AxisAlignedBB getCableBoundingBox(EnumFacing side) {
        if (side == null) {
            return CABLE_CENTER_BOUNDINGBOX;
        } else {
            return CABLE_SIDE_BOUNDINGBOXES.get(side);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if(disableCollisionBox) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        return super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public int getLightOpacity(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        return CableHelpers.hasFacade(world, pos) && !CableHelpers.isLightTransparent(world, pos, null) ? 255 : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState blockState) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBakedModel createDynamicModel() {
        return new CableModel();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return super.doesSideBlockRendering(state, world, pos, face) || (CableHelpers.hasFacade(world, pos) && CableHelpers.getFacade(world, pos).isOpaqueCube());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState blockState, World world, net.minecraft.util.math.RayTraceResult target, ParticleManager particleManager) {
        BlockPos blockPos = target.getBlockPos();
        if(CableHelpers.hasFacade(world, blockPos)) {
            IBlockState facadeState = CableHelpers.getFacade(world, blockPos);
            RenderHelpers.addBlockHitEffects(particleManager, world, facadeState, blockPos, target.sideHit);
            return true;
        } else {
            return super.addHitEffects(blockState, world, target, particleManager);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isSideSolid(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if(CableHelpers.hasFacade(world, pos)) {
            return true;
        }
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos, side);
        if(partContainer != null && partContainer.hasPart(side)) {
            IPartType partType = partContainer.getPart(side);
            return partType.isSolid(partContainer.getPartState(side));
        }
        return super.isSideSolid(blockState, world, pos, side);
    }

    @Override
    public boolean canRenderInLayer(IBlockState blockState, BlockRenderLayer layer) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getSelectedBoundingBoxParent(IBlockState blockState, World worldIn, BlockPos pos) {
        return super.getSelectedBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public net.minecraft.util.math.RayTraceResult rayTraceParent(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox) {
        return super.rayTrace(pos, start, end, boundingBox);
    }

    /* --------------- Start IDynamicRedstone --------------- */

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(IBlockState blockState) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if(side == null) {
            for(EnumFacing dummySide : EnumFacing.VALUES) {
                IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, dummySide, DynamicRedstoneConfig.CAPABILITY);
                if(dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput())) {
                    return true;
                }
            }
            return false;
        }
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
        return dynamicRedstone != null && (dynamicRedstone.getRedstoneLevel() >= 0 || dynamicRedstone.isAllowRedstoneInput());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
        return dynamicRedstone != null && dynamicRedstone.isStrong() ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IDynamicRedstone dynamicRedstone = TileHelpers.getCapability(world, pos, side.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
        return dynamicRedstone != null ? dynamicRedstone.getRedstoneLevel() : 0;
    }

    /* --------------- Start IDynamicLight --------------- */

    @Override
    public int getLightValue(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        int light = 0;
        for(EnumFacing side : EnumFacing.values()) {
            IDynamicLight dynamicLight = TileHelpers.getCapability(world, pos, side, DynamicLightConfig.CAPABILITY);
            if (dynamicLight != null) {
                light = Math.max(light, dynamicLight.getLightLevel());
            }
        }
        return light;
    }
}
