package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.MatrixHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.block.CollidableComponent;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.block.ICollidableParent;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.*;

/**
 * A block that is build up from different parts.
 * This block refers to a ticking tile entity.
 * Ray tracing code is partially based on BuildCraft's pipe code.
 * @author rubensworks
 */
public class BlockCable extends ConfigurableBlockContainer implements ICableConnectable<CablePathElement>,
        INetworkElementProvider, IPartContainerFacade, ICollidable<EnumFacing>, ICollidableParent {

    // Properties
    @BlockProperty
    public static final IUnlistedProperty<Boolean> REALCABLE = Properties.toUnlisted(PropertyBool.create("realcable"));
    @BlockProperty
    public static final IUnlistedProperty<Boolean>[] CONNECTED = new IUnlistedProperty[6];
    @BlockProperty
    public static final IUnlistedProperty<Boolean>[] PART = new IUnlistedProperty[6];
    static {
        for(EnumFacing side : EnumFacing.values()) {
            CONNECTED[side.ordinal()] = Properties.toUnlisted(PropertyBool.create("connect-" + side.getName()));
            PART[side.ordinal()] = Properties.toUnlisted(PropertyBool.create("part-" + side.getName()));
        }
    }

    // Collision boxes
    private static final float[][] CABLE_COLLISION_BOXES = {
            {CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX}, // DOWN
            {CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX}, // UP
            {CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN}, // NORTH
            {CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1}, // SOUTH
            {0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX}, // WEST
            {CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX}, // EAST
    };
    private static final float[][] PART_COLLISION_BOXES = {
            {0.19F, 0.81F}, {CableModel.MIN - 0.37F, CableModel.MIN}, {0.19F, 0.81F}
    };

    // Collision components
    private static final List<IComponent<EnumFacing, BlockCable>> COLLIDABLE_COMPONENTS = Lists.newLinkedList();
    private static final IComponent<EnumFacing, BlockCable> CENTER_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(new EnumFacing[]{null});
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.isRealCable(world, pos);
        }

        @Override
        public AxisAlignedBB getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.getCableBoundingBox(null);
        }
    };
    private static final IComponent<EnumFacing, BlockCable> CABLECONNECTIONS_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(EnumFacing.VALUES);
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.isConnected(world, pos, position);
        }

        @Override
        public AxisAlignedBB getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.getCableBoundingBox(position);
        }
    };
    private static final IComponent<EnumFacing, BlockCable> PARTS_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(EnumFacing.VALUES);
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.hasPart(world, pos, position);
        }

        @Override
        public AxisAlignedBB getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.getPartBoundingBox(position);
        }
    };
    static {
        COLLIDABLE_COMPONENTS.add(CENTER_COMPONENT);
        COLLIDABLE_COMPONENTS.add(CABLECONNECTIONS_COMPONENT);
        COLLIDABLE_COMPONENTS.add(PARTS_COMPONENT);
    }
    @Delegate
    private ICollidable collidableComponent = new CollidableComponent<EnumFacing, BlockCable>(this, COLLIDABLE_COMPONENTS);

    private static BlockCable _instance = null;

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
    public BlockCable(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileMultipartTicking.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
        eConfig.getMod().getIconProvider().registerIconHolderObject(this);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            return tile.getConnectionState();
        }
        return getDefaultState();
    }

    @Override
    public IExtendedBlockState updateConnections(World world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            tile.updateCableConnections();
            return tile.getConnectionState();
        }
        return null;
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(
                (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos),
                CONNECTED[side.ordinal()],
                false);
    }

    @Override
    public boolean hasPart(World world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(
               (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos),
               PART[side.ordinal()],
               false);
    }

    @Override
    public void initNetwork(World world, BlockPos pos) {
        Network.initiateNetworkSetup(this, world, pos).initialize();
    }

    @Override
    public boolean isRealCable(World world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            return tile.isRealCable();
        }
        return true;
    }

    @Override
    public void setRealCable(World world, BlockPos pos, boolean realCable) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            tile.setRealCable(realCable);
            // TODO: place/break sound
            if(realCable) {
                addToNetwork(world, pos);
            } else {
                removeFromNetwork(world, pos);
            }
        }
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            tile.forceDisconnect(side);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
            if(rayTraceResult != null) {
                EnumFacing positionHit = rayTraceResult.getPositionHit();
                if(rayTraceResult.getCollisionType() == PARTS_COMPONENT) {
                    if(!world.isRemote && WrenchHelpers.isWrench(player, pos)) {
                        // Remove part from cable
                        if(player.isSneaking()) {
                            getPartContainer(world, pos).removePart(positionHit);
                            // Remove full cable block if this was the last part and if it was already an unreal cable.
                            if(!isRealCable(world, pos) && !getPartContainer(world, pos).hasParts()) {
                                world.destroyBlock(pos, !player.capabilities.isCreativeMode);
                            }
                        }
                        return true;
                    } else {
                        // Delegate activated call to part
                        getPartContainer(world, pos).getPart(positionHit).onPartActivated(world, pos, state,
                                getPartContainer(world, pos).getPartState(positionHit), player, positionHit, hitX, hitY, hitZ);
                        return true;
                    }
                } else if (!world.isRemote
                        && (rayTraceResult.getCollisionType() == CABLECONNECTIONS_COMPONENT
                            || rayTraceResult.getCollisionType() == CENTER_COMPONENT)
                        && WrenchHelpers.isWrench(player, pos)) {
                    if(player.isSneaking()) {
                        if(!getPartContainer(world, pos).hasParts()) {
                            // Remove full cable
                            world.destroyBlock(pos, !player.capabilities.isCreativeMode);
                        } else {
                            // Mark cable as unavailable.
                            setRealCable(world, pos, false);
                        }
                    } else if(rayTraceResult.getCollisionType() == CABLECONNECTIONS_COMPONENT) {
                        // Disconnect cable side

                        // Store the disconnection in the tile entity
                        disconnect(world, pos, positionHit);

                        // Signal changes
                        tile.updateCableConnections();
                        triggerNeighbourConnections(world, pos);

                        // Reinit the networks for this block and the disconnected neighbour.
                        initNetwork(world, pos);
                        BlockPos neighbourPos = pos.offset(positionHit);
                        Block neighbourBlock = world.getBlockState(neighbourPos).getBlock();
                        if (neighbourBlock instanceof ICableConnectable) {
                            ((ICableConnectable<CablePathElement>) neighbourBlock).initNetwork(world, neighbourPos);
                        }
                    }
                    return true;
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player , side, hitX, hitY, hitZ);
    }

    protected void requestConnectionsUpdate(World world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos);
        if(tile != null) {
            tile.updateCableConnections();
        }
        world.markBlockRangeForRenderUpdate(pos, pos);
    }


    protected void triggerNeighbourConnections(World world, BlockPos blockPos) {
        for(EnumFacing side : EnumFacing.VALUES) {
            requestConnectionsUpdate(world, blockPos.offset(side));
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        addToNetwork(world, pos);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }

    @Override
    public boolean isDropBlockItem(IBlockAccess world, BlockPos pos, IBlockState blockState, int fortune) {
        return BlockHelpers.getSafeBlockStateProperty((IExtendedBlockState) getExtendedState(blockState, world, pos),
               REALCABLE, true);
    }

    protected void addToNetwork(World world, BlockPos pos) {
        triggerNeighbourConnections(world, pos);
        if(!world.isRemote) {
            initNetwork(world, pos);
        }
    }

    protected void removeFromNetwork(World world, BlockPos pos) {
        removeFromNetwork(world, pos, true);
        removeFromNetwork(world, pos, false);
    }

    protected void removeFromNetwork(World world, BlockPos pos, boolean preDestroy) {
        if(preDestroy) {
            // Remove the cable from this network if it exists
            Network network = getPartContainer(world, pos).getNetwork();
            if(network != null) {
                network.removeCable(this, createPathElement(world, pos));
            }
        } else {
            triggerNeighbourConnections(world, pos);
            // Reinit neighbouring networks.
            for(EnumFacing side : EnumFacing.VALUES) {
                if(!world.isRemote) {
                    BlockPos sidePos = pos.offset(side);
                    Block block = world.getBlockState(sidePos).getBlock();
                    if(block instanceof ICableConnectable) {
                        ((ICableConnectable<CablePathElement>) block).initNetwork(world, sidePos);
                    }
                }
            }
        }
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        // Drop all parts types as item.
        List<ItemStack> itemStacks = Lists.newLinkedList();
        for (INetworkElement networkElement : createNetworkElements(world, pos)) {
            networkElement.addDrops(itemStacks);
        }
        for(ItemStack itemStack : itemStacks) {
            spawnAsEntity(world, pos, itemStack);
        }

        removeFromNetwork(world, pos, true);

        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        removeFromNetwork(world, pos, false);
    }

    @Override
    public boolean canConnect(World world, BlockPos selfPosition, ICableConnectable connector, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, selfPosition);
        return tile == null || !tile.isForceDisconnected(side);
    }

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        Set<INetworkElement> sidedElements = Sets.newHashSet();
        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : getPartContainer(world, blockPos).getParts().entrySet()) {
            sidedElements.add(entry.getValue().createNetworkElement(this, DimPos.of(world, blockPos), entry.getKey()));
        }
        return sidedElements;
    }

    @Override
    public IPartContainer getPartContainer(World world, BlockPos pos) {
        return (IPartContainer) world.getTileEntity(pos);
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return new CablePathElement(this, DimPos.of(world, blockPos));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        if(disableCollisionBox) return null;
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public int getRenderType() {
        return 3;
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

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    private AxisAlignedBB getCableBoundingBox(EnumFacing side) {
        float min = CableModel.MIN;
        float max = CableModel.MAX;
        if (side == null) {
            return AxisAlignedBB.fromBounds(min, min, min, max, max, max).expand(0.002F, 0.002F, 0.002F);
        } else {
            float[] b = CABLE_COLLISION_BOXES[side.ordinal()];
            return AxisAlignedBB.fromBounds(b[0], b[1], b[2], b[3], b[4], b[5]).expand(0.001F, 0.001F, 0.001F);
        }
    }

    private AxisAlignedBB getPartBoundingBox(EnumFacing side) {
        // Copy bounds
        float[][] bounds = new float[PART_COLLISION_BOXES.length][PART_COLLISION_BOXES[0].length];
        for (int i = 0; i < bounds.length; i++)
            bounds[i] = Arrays.copyOf(PART_COLLISION_BOXES[i], PART_COLLISION_BOXES[i].length);

        // Transform bounds
        MatrixHelpers.transform(bounds, side);
        return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public void addCollisionBoxesToListParent(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask,
                                              List list, Entity collidingEntity) {
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxParent(World worldIn, BlockPos pos) {
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public MovingObjectPosition collisionRayTraceParent(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
        return super.collisionRayTrace(world, pos, origin, direction);
    }

}
