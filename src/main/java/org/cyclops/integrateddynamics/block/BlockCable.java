package org.cyclops.integrateddynamics.block;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.UnlistedProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFacadeable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.capability.PartContainerConfig;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.core.block.CollidableComponent;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.block.ICollidableParent;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkFacadeableComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.item.ItemBlockCable;
import org.cyclops.integrateddynamics.item.ItemFacade;

import javax.annotation.Nullable;
import java.util.*;


/**
 * A block that is buildReader up from different parts.
 * This block refers to a ticking tile entity.
 * Ray tracing code is partially based on BuildCraft's pipe code.
 * @author rubensworks
 */
public class BlockCable extends ConfigurableBlockContainer implements ICableNetwork<IPartNetwork, ICablePathElement>,
        ICableFakeable<ICablePathElement>, ICableFacadeable<ICablePathElement>, INetworkElementProvider,
        ICollidable<EnumFacing>, ICollidableParent, IDynamicRedstoneBlock, IDynamicLightBlock {

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

    // Collision boxes
    private final static AxisAlignedBB CABLE_CENTER_BOUNDINGBOX = new AxisAlignedBB(
            CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX);
    private final static EnumFacingMap<AxisAlignedBB> CABLE_SIDE_BOUNDINGBOXES = EnumFacingMap.forAllValues(
            new AxisAlignedBB(CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX), // DOWN
            new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX), // UP
            new AxisAlignedBB(CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN), // NORTH
            new AxisAlignedBB(CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1), // SOUTH
            new AxisAlignedBB(0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX), // WEST
            new AxisAlignedBB(CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX) // EAST
    );

    // Collision components
    private static final List<IComponent<EnumFacing, BlockCable>> COLLIDABLE_COMPONENTS = Lists.newArrayList();
    private static final IComponent<EnumFacing, BlockCable> CENTER_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(new EnumFacing[]{null});
        }

        @Override
        public int getBoundsCount(EnumFacing position) {
            return 1;
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.isRealCable(world, pos);
        }

        @Override
        public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return Collections.singletonList(block.getCableBoundingBox(null));
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
            return new ItemStack(BlockCable.getInstance());
        }

        @Override
        public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
            if(!world.isRemote) {
                BlockCable cable = BlockCable.getInstance();
                if (cable.getPartContainer(world, pos).hasParts()) {
                    cable.setRealCable(world, pos, false);
                    if (!player.capabilities.isCreativeMode) {
                        ItemStackHelpers.spawnItemStackToPlayer(world, pos, new ItemStack(BlockCable.getInstance()), player);
                    }
                    return false;
                } else {
                    cable.remove(world, pos, player);
                    return true;
                }
            }
            return false;
        }

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
            return RenderHelpers.getDynamicBakedModel(world, pos);
        }
    };
    private static final IComponent<EnumFacing, BlockCable> CABLECONNECTIONS_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(EnumFacing.VALUES);
        }

        @Override
        public int getBoundsCount(EnumFacing position) {
            return 1;
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return CENTER_COMPONENT.isActive(block, world, pos, position)
                    && (block.isConnected(world, pos, position) || block.hasPart(world, pos, position));
        }

        @Override
        public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return Collections.singletonList(block.isConnected(world, pos, position) ? block.getCableBoundingBox(position) : block.getCableBoundingBoxWithPart(world, pos, position));
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
            return new ItemStack(BlockCable.getInstance());
        }

        @Override
        public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
            return CENTER_COMPONENT.destroy(world, pos, position, player);
        }

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
            return CENTER_COMPONENT.getBreakingBaseModel(world, pos, position);
        }
    };
    private static final IComponent<EnumFacing, BlockCable> PARTS_COMPONENT = new IComponent<EnumFacing, BlockCable>() {
        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(EnumFacing.VALUES);
        }

        @Override
        public int getBoundsCount(EnumFacing position) {
            return 1;
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.hasPart(world, pos, position);
        }

        @Override
        public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return Collections.singletonList(block.getPartBoundingBox(world, pos, position));
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
            IPartContainer partContainer = BlockCable.getInstance().getPartContainer(world, pos);
            return partContainer.getPart(position).getPickBlock(world, pos, partContainer.getPartState(position));
        }

        @Override
        public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
            if(!world.isRemote) {
                return PartHelpers.removePart(world, pos, position, player, true);
            }
            return false;
        }

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
            IBlockState blockState = world.getBlockState(pos);
            IExtendedBlockState state = (IExtendedBlockState) blockState.getBlock().getExtendedState(blockState, world, pos);
            IPartContainer partContainer = BlockHelpers.getSafeBlockStateProperty(state, BlockCable.PARTCONTAINER, null);
            IBlockState cableState = partContainer != null ? partContainer.getPart(position).getBlockState(partContainer, position) : null;
            return RenderHelpers.getBakedModel(cableState);
        }
    };
    private static final IComponent<EnumFacing, BlockCable> FACADE_COMPONENT = new IComponent<EnumFacing, BlockCable>() {

        private final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);

        @Override
        public Collection<EnumFacing> getPossiblePositions() {
            return Arrays.asList(new EnumFacing[]{null});
        }

        @Override
        public int getBoundsCount(EnumFacing position) {
            return 1;
        }

        @Override
        public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return block.hasFacade(world, pos);
        }

        @Override
        public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
            return Collections.singletonList(BOUNDS);
        }

        @Override
        public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
            ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
            ItemFacade.getInstance().writeFacadeBlock(itemStack, BlockCable.getInstance().getFacade(world, pos));
            return itemStack;
        }

        @Override
        public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
            if(!world.isRemote) {
                IBlockState blockState = BlockCable.getInstance().getFacade(world, pos);
                ItemStack itemStack = new ItemStack(ItemFacade.getInstance());
                ItemFacade.getInstance().writeFacadeBlock(itemStack, blockState);
                BlockCable.getInstance().setFacade(world, pos, null);
                if (!player.capabilities.isCreativeMode) {
                    ItemStackHelpers.spawnItemStackToPlayer(world, pos, itemStack, player);
                }
            }
            return false;
        }

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
            IBlockState blockState = world.getBlockState(pos);
            IExtendedBlockState state = (IExtendedBlockState) blockState.getBlock().getExtendedState(blockState, world, pos);
            Optional<IBlockState> blockStateOptional = BlockHelpers.getSafeBlockStateProperty(state, BlockCable.FACADE, Optional.absent());
            if(!blockStateOptional.isPresent()) return null;
            return RenderHelpers.getBakedModel(blockStateOptional.get());
        }
    };
    static {
        COLLIDABLE_COMPONENTS.add(FACADE_COMPONENT);
        COLLIDABLE_COMPONENTS.add(CENTER_COMPONENT);
        COLLIDABLE_COMPONENTS.add(CABLECONNECTIONS_COMPONENT);
        COLLIDABLE_COMPONENTS.add(PARTS_COMPONENT);
    }
    @SuppressWarnings("deprecation")
    @Delegate
    private ICollidable collidableComponent = new CollidableComponent<EnumFacing, BlockCable>(this, COLLIDABLE_COMPONENTS);
    //@Delegate// <- Lombok can't handle delegations with generics, so we'll have to do it manually...
    private CableNetworkFacadeableComponent<BlockCable> cableNetworkComponent = new CableNetworkFacadeableComponent<>(this);
    private NetworkElementProviderComponent<IPartNetwork> networkElementProviderComponent = new NetworkElementProviderComponent<>(this);

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
    public BlockCable(ExtendedConfig eConfig) {
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

    protected boolean hasPart(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(
               (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos),
               PART_RENDERPOSITIONS[side.ordinal()],
                PartRenderPosition.NONE) != PartRenderPosition.NONE;
    }

    @Override
    public boolean isRealCable(World world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            return tile.isRealCable();
        }
        return true;
    }

    @Override
    public void setRealCable(World world, BlockPos pos, boolean realCable) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            tile.setRealCable(realCable);
            if (realCable) {
                cableNetworkComponent.addToNetwork(world, pos);
            } else {
                networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos, false);
                if (!cableNetworkComponent.removeCableFromNetwork(world, pos)) {
                    tile.setRealCable(!realCable);
                    IntegratedDynamics.clog(Level.WARN, "Tried to set a fake cable, but the original network element was not present");
                }
            }
        }
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos, EntityPlayer player) {
        if(isRealCable(world, pos)) {
            networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos, true);
            cableNetworkComponent.onPreBlockDestroyed(world, pos);
        }
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        if(!IS_MCMP_CONVERTING) { // Yes, this is a hack, we don't want this to be called after a MCMP block conversion
            cableNetworkComponent.onPostBlockDestroyed(world, pos);
        }
        IS_MCMP_CONVERTING = false;
    }

    @Override
    public boolean removedByPlayer(IBlockState blockState, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if(rayTraceResult != null && rayTraceResult.getCollisionType() != null) {
            return rayTraceResult.getCollisionType().destroy(world, pos, rayTraceResult.getPositionHit(), player);
        }
        return super.removedByPlayer(blockState, world, pos, player, willHarvest);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, ItemStack heldItem, EnumFacing side,
                                    float hitX, float hitY, float hitZ) {
        /*
            Wrench: sneak + right-click anywhere on cable to remove cable
                    right-click on a cable side to disconnect on that side
                    sneak + right-click on part to remove that part
            No wrench: right-click to open GUI
         */
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
            if(rayTraceResult != null) {
                EnumFacing positionHit = rayTraceResult.getPositionHit();
                if(rayTraceResult.getCollisionType() == FACADE_COMPONENT) {
                    if(!world.isRemote && WrenchHelpers.isWrench(player, heldItem, world, pos, side) && player.isSneaking()) {
                        FACADE_COMPONENT.destroy(world, pos, side, player);
                        world.notifyNeighborsOfStateChange(pos, this);
                        return true;
                    }
                    return false;
                } else if(rayTraceResult.getCollisionType() == PARTS_COMPONENT) {
                    if(!world.isRemote && WrenchHelpers.isWrench(player, heldItem, world, pos, side) && player.isSneaking()) {
                        // Remove part from cable
                        PARTS_COMPONENT.destroy(world, pos, rayTraceResult.getPositionHit(), player);
                        ItemBlockCable.playBreakSound(world, pos, BlockCable.getInstance().getDefaultState());
                        return true;
                    } else if(isRealCable(world, pos)) {
                        // Delegate activated call to part
                        return getPartContainer(world, pos).getPart(positionHit).onPartActivated(world, pos,
                                getPartContainer(world, pos).getPartState(positionHit), player, hand, heldItem, positionHit, hitX, hitY, hitZ);
                    }
                } else if (!world.isRemote
                        && (rayTraceResult.getCollisionType() == CABLECONNECTIONS_COMPONENT
                            || rayTraceResult.getCollisionType() == CENTER_COMPONENT)) {
                    if(onCableActivated(world, pos, state, player, hand, heldItem, side,
                            rayTraceResult.getCollisionType() == CENTER_COMPONENT ? null : rayTraceResult.getPositionHit())) {
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    public static boolean onCableActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                           ItemStack heldItem, EnumFacing side, EnumFacing cableConnectionHit) {
        ICableNetwork<?, ?> cable = CableHelpers.getInterface(world, pos, ICableNetwork.class);
        IPartContainer partContainer = PartContainerConfig.get(world, pos);
        if(WrenchHelpers.isWrench(player, heldItem, world, pos, side)) {
            if (player.isSneaking()) {
                if (partContainer == null || !partContainer.hasParts() || !(cable instanceof ICableFakeable)) {
                    // Remove full cable
                    cable.remove(world, pos, player);
                    ItemBlockCable.playBreakSound(world, pos, state);
                } else {
                    // Mark cable as unavailable.
                    ((ICableFakeable) cable).setRealCable(world, pos, false);
                    ItemBlockCable.playBreakSound(world, pos, state);
                    ItemStackHelpers.spawnItemStackToPlayer(world, pos, new ItemStack(BlockCable.getInstance()), player);
                }
            } else if (cableConnectionHit != null) {
                // Disconnect cable side

                // Store the disconnection in the tile entity
                cable.disconnect(world, pos, cableConnectionHit);

                // Signal changes
                cable.updateConnections(world, pos);
                cable.triggerUpdateNeighbourConnections(world, pos);

                // Reinit the networks for this block and the disconnected neighbour.
                cable.initNetwork(world, pos);
                BlockPos neighbourPos = pos.offset(cableConnectionHit);
                ICableNetwork neighbourCable = CableHelpers.getInterface(world, neighbourPos, ICableNetwork.class);
                if (neighbourCable != null) {
                    neighbourCable.initNetwork(world, neighbourPos);
                }
                return true;
            } else if (cableConnectionHit == null) {
                // Reconnect cable side
                BlockPos neighbourPos = pos.offset(side);
                ICable neighbourCable = CableHelpers.getInterface(world, neighbourPos, ICable.class);
                if(neighbourCable != null && !cable.isConnected(world, pos, side) &&
                        (cable.canConnect(world, pos, neighbourCable, side) || neighbourCable.canConnect(world, neighbourPos, cable, side.getOpposite()))
                        ) {
                    // Notify the reconnection in the tile entity of this and the neighbour block,
                    // since we don't know in which one the disconnection was made.
                    cable.reconnect(world, pos, side);
                    neighbourCable.reconnect(world, neighbourPos, side.getOpposite());

                    // Signal changes
                    cable.updateConnections(world, pos);
                    cable.triggerUpdateNeighbourConnections(world, pos);

                    // Reinit the networks for this block and the connected neighbour.
                    cable.initNetwork(world, pos);
                    if (neighbourCable instanceof ICableNetwork) {
                        ((ICableNetwork<IPartNetwork, ICablePathElement>) neighbourCable).initNetwork(world, neighbourPos);
                    }
                }
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        cableNetworkComponent.addToNetwork(world, pos);
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

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        Set<INetworkElement> sidedElements = Sets.newHashSet();
        IPartContainer partContainer = getPartContainer(world, blockPos);
        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : partContainer.getParts().entrySet()) {
            sidedElements.add(entry.getValue().createNetworkElement(partContainer, DimPos.of(world, blockPos), entry.getKey()));
        }
        return sidedElements;
    }

    protected IPartContainer getPartContainer(IBlockAccess world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class).getPartContainer();
    }

    /* --------------- Start ICollidable and rendering --------------- */

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        if(disableCollisionBox) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        return super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public int getLightOpacity(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        return hasFacade(world, pos) ? 255 : 0;
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
        return super.doesSideBlockRendering(state, world, pos, face) || hasFacade(world, pos);
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
        if(hasFacade(world, blockPos)) {
            IBlockState facadeState = getFacade(world, blockPos);
            RenderHelpers.addBlockHitEffects(particleManager, world, facadeState, blockPos, target.sideHit);
            return true;
        } else {
            return super.addHitEffects(blockState, world, target, particleManager);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isSideSolid(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if(hasFacade(world, pos)) {
            return true;
        }
        if(hasPart(world, pos, side)) {
            IPartContainer partContainer = getPartContainer(world, pos);
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

    public AxisAlignedBB getCableBoundingBox(EnumFacing side) {
        if (side == null) {
            return CABLE_CENTER_BOUNDINGBOX;
        } else {
            return CABLE_SIDE_BOUNDINGBOXES.get(side);
        }
    }

    protected PartRenderPosition getPartRenderPosition(World world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty((IExtendedBlockState)
                getExtendedState(world.getBlockState(pos), world, pos), PART_RENDERPOSITIONS[side.ordinal()],
                PartRenderPosition.NONE);
    }

    private AxisAlignedBB getCableBoundingBoxWithPart(World world, BlockPos pos, EnumFacing side) {
        if (side == null) {
            return CABLE_CENTER_BOUNDINGBOX;
        } else {
            return getPartRenderPosition(world, pos, side).getSidedCableBoundingBox(side);
        }
    }

    private AxisAlignedBB getPartBoundingBox(World world, BlockPos pos, EnumFacing side) {
        return getPartRenderPosition(world, pos, side).getBoundingBox(side);
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

    /* --------------- Start IDynamicRedstoneBlock --------------- */

    @Override
    public void disableRedstoneAt(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            tile.disableRedstoneLevel(side);
        }
    }

    @Override
    public void setRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            tile.setRedstoneLevel(side, level);
        }
    }

    @Override
    public int getRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            return tile.getRedstoneLevel(side);
        }
        return -1;
    }

    @Override
    public void setAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side, boolean allow) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            tile.setAllowRedstoneInput(side, allow);
        }
    }

    @Override
    public boolean isAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            return tile.isAllowRedstoneInput(side);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(IBlockState blockState) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if(side == null) {
            for(EnumFacing dummySide : EnumFacing.VALUES) {
                if(getRedstoneLevel(world, pos, dummySide) >= 0 || isAllowRedstoneInput(world, pos, dummySide)) {
                    return true;
                }
            }
            return false;
        }
        return getRedstoneLevel(world, pos, side.getOpposite()) >= 0 || isAllowRedstoneInput(world, pos, side.getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return getRedstoneLevel(world, pos, side.getOpposite());
    }

    /* --------------- Start IDynamicLightBlock --------------- */

    @Override
    public void setLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            tile.setLightLevel(side, level);
        }
    }

    @Override
    public int getLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            return tile.getLightLevel(side);
        }
        return 0;
    }

    @Override
    public int getLightValue(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        int light = 0;
        for(EnumFacing side : EnumFacing.values()) {
            light = Math.max(light, getLightLevel(world, pos, side));
        }
        return light;
    }

    /* --------------- Delegate to ICableNetwork<CablePathElement> --------------- */

    @Override
    public void initNetwork(World world, BlockPos pos) {
        if(isRealCable(world, pos)) {
            cableNetworkComponent.initNetwork(world, pos);
        }
    }

    @Override
    public boolean canConnect(World world, BlockPos selfPosition, ICable connector, EnumFacing side) {
        return cableNetworkComponent.canConnect(world, selfPosition, connector, side);
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        cableNetworkComponent.updateConnections(world, pos);
    }

    @Override
    public void triggerUpdateNeighbourConnections(World world, BlockPos pos) {
        cableNetworkComponent.triggerUpdateNeighbourConnections(world, pos);
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        // Note delegated to component, but instead use the cached information in the extended blockstate
        return BlockHelpers.getSafeBlockStateProperty(
                (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos),
                CONNECTED[side.ordinal()],
                false);
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.disconnect(world, pos, side);
    }

    @Override
    public void reconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.reconnect(world, pos, side);
    }

    @Override
    public void remove(World world, BlockPos pos, EntityPlayer player) {
        // PRE
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos, true);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        // POST
        cableNetworkComponent.remove(world, pos, player);
    }

    @Override
    public boolean hasFacade(IBlockAccess world, BlockPos pos) {
        return cableNetworkComponent.hasFacade(world, pos);
    }

    @Override
    public IBlockState getFacade(World world, BlockPos pos) {
        return cableNetworkComponent.getFacade(world, pos);
    }

    @Override
    public void setFacade(World world, BlockPos pos, @Nullable IBlockState blockState) {
        cableNetworkComponent.setFacade(world, pos, blockState);
    }

    @Override
    public void resetCurrentNetwork(World world, BlockPos pos) {
        cableNetworkComponent.resetCurrentNetwork(world, pos);
    }

    @Override
    public void setNetwork(IPartNetwork network, World world, BlockPos pos) {
        cableNetworkComponent.setNetwork(network, world, pos);
    }

    @Override
    public IPartNetwork getNetwork(World world, BlockPos pos) {
        return cableNetworkComponent.getNetwork(world, pos);
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return cableNetworkComponent.createPathElement(world, blockPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock) {
        super.neighborChanged(state, world, pos, neighborBlock);
        cableNetworkComponent.updateConnections(world, pos);
        networkElementProviderComponent.onBlockNeighborChange(getNetwork(world, pos), world, pos, neighborBlock);
    }
}
