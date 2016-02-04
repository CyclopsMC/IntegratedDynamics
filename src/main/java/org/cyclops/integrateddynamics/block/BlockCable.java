package org.cyclops.integrateddynamics.block;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
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
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.UnlistedProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
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
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
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
 * A block that is build up from different parts.
 * This block refers to a ticking tile entity.
 * Ray tracing code is partially based on BuildCraft's pipe code.
 * @author rubensworks
 */
public class BlockCable extends ConfigurableBlockContainer implements ICableNetwork<IPartNetwork, ICablePathElement>,
        ICableFakeable<ICablePathElement>, ICableFacadeable<ICablePathElement>, INetworkElementProvider,
        IPartContainerFacade, ICollidable<EnumFacing>, ICollidableParent, IDynamicRedstoneBlock, IDynamicLightBlock {

    public static final float BLOCK_HARDNESS = 3.0F;
    public static final Material BLOCK_MATERIAL = Material.glass;

    // Properties
    @BlockProperty
    public static final IUnlistedProperty<Boolean> REALCABLE = Properties.toUnlisted(PropertyBool.create("realcable"));
    @BlockProperty
    public static final IUnlistedProperty<Boolean>[] CONNECTED = new IUnlistedProperty[6];
    @BlockProperty
    public static final IUnlistedProperty<IPartType.RenderPosition>[] PART_RENDERPOSITIONS = new IUnlistedProperty[6];
    @BlockProperty
    public static final IUnlistedProperty<Optional> FACADE = new UnlistedProperty<>("facade", Optional.class);
    static {
        for(EnumFacing side : EnumFacing.values()) {
            CONNECTED[side.ordinal()] = Properties.toUnlisted(PropertyBool.create("connect-" + side.getName()));
            PART_RENDERPOSITIONS[side.ordinal()] = new UnlistedProperty<>("partRenderPosition-" + side.getName(), IPartType.RenderPosition.class);
        }
    }
    @BlockProperty
    public static final IUnlistedProperty<IPartContainer> PARTCONTAINER = new UnlistedProperty<>("partcontainer", IPartContainer.class);

    // Collision boxes
    public static final float[][] CABLE_COLLISION_BOXES = {
            {CableModel.MIN, 0, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX}, // DOWN
            {CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1, CableModel.MAX}, // UP
            {CableModel.MIN, CableModel.MIN, 0, CableModel.MAX, CableModel.MAX, CableModel.MIN}, // NORTH
            {CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1}, // SOUTH
            {0, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX}, // WEST
            {CableModel.MAX, CableModel.MIN, CableModel.MIN, 1, CableModel.MAX, CableModel.MAX}, // EAST
    };

    // Collision components
    private static final List<IComponent<EnumFacing, BlockCable>> COLLIDABLE_COMPONENTS = Lists.newLinkedList();
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
                    ItemStackHelpers.spawnItemStackToPlayer(world, pos, new ItemStack(BlockCable.getInstance()), player);
                } else {
                    cable.remove(world, pos, player);
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
                PartHelpers.removePart(world, pos, position, player, true);
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
                ItemStackHelpers.spawnItemStackToPlayer(world, pos, itemStack, player);
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
        setStepSound(soundTypeMetal);
        if(MinecraftHelpers.isClientSide()) {
            eConfig.getMod().getIconProvider().registerIconHolderObject(this);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            return tile.getConnectionState();
        }
        return getDefaultState();
    }

    protected boolean hasPart(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(
               (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos),
               PART_RENDERPOSITIONS[side.ordinal()],
                IPartType.RenderPosition.NONE) != IPartType.RenderPosition.NONE;
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
            IS_MCMP_CONVERTING = false;
            cableNetworkComponent.onPostBlockDestroyed(world, pos);
        }
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if(rayTraceResult != null && rayTraceResult.getCollisionType() != null) {
            return rayTraceResult.getCollisionType().destroy(world, pos, rayTraceResult.getPositionHit(), player);
        }
        return super.removedByPlayer(world, pos, player, willHarvest);
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
        TileMultipartTicking tile = TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
        if(tile != null) {
            RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
            if(rayTraceResult != null) {
                EnumFacing positionHit = rayTraceResult.getPositionHit();
                if(rayTraceResult.getCollisionType() == FACADE_COMPONENT) {
                    if(!world.isRemote && WrenchHelpers.isWrench(player, pos) && player.isSneaking()) {
                        FACADE_COMPONENT.destroy(world, pos, side, player);
                        world.notifyNeighborsOfStateChange(pos, this);
                        return true;
                    }
                    return false;
                } else if(rayTraceResult.getCollisionType() == PARTS_COMPONENT) {
                    if(!world.isRemote && WrenchHelpers.isWrench(player, pos)) {
                        // Remove part from cable
                        if(player.isSneaking()) {
                            PARTS_COMPONENT.destroy(world, pos, rayTraceResult.getPositionHit(), player);
                            ItemBlockCable.playBreakSound(world, pos, BlockCable.getInstance().getDefaultState());
                        }
                        return true;
                    } else if(isRealCable(world, pos)) {
                        // Delegate activated call to part
                        return getPartContainer(world, pos).getPart(positionHit).onPartActivated(world, pos,
                                getPartContainer(world, pos).getPartState(positionHit), player, positionHit, hitX, hitY, hitZ);
                    }
                } else if (!world.isRemote
                        && (rayTraceResult.getCollisionType() == CABLECONNECTIONS_COMPONENT
                            || rayTraceResult.getCollisionType() == CENTER_COMPONENT)) {
                    if(onCableActivated(world, pos, state, player, side,
                            rayTraceResult.getCollisionType() == CENTER_COMPONENT ? null : rayTraceResult.getPositionHit())) {
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player , side, hitX, hitY, hitZ);
    }

    public static boolean onCableActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, EnumFacing cableConnectionHit) {
        ICableNetwork<?, ?> cable = CableHelpers.getInterface(world, pos, ICableNetwork.class);
        if(WrenchHelpers.isWrench(player, pos)) {
            if (player.isSneaking()) {
                if (!(cable instanceof IPartContainerFacade) || !((IPartContainerFacade) cable).getPartContainer(world, pos).hasParts() || !(cable instanceof ICableFakeable)) {
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

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {
        RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if(rayTraceResult != null) {
            EnumFacing positionHit = rayTraceResult.getPositionHit();
            return rayTraceResult.getCollisionType().getPickBlock(world, pos, positionHit);
        }
        return new ItemStack(getItem(world, pos), 1, getDamageValue(world, pos));
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
    public IPartContainer getPartContainer(IBlockAccess world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, IPartContainer.class);
    }

    @Nullable
    @Override
    public EnumFacing getWatchingSide(World world, BlockPos pos, EntityPlayer player) {
        ICollidable.RayTraceResult<EnumFacing> rayTraceResult = doRayTrace(world, pos, player);
        if(rayTraceResult != null) {
            return rayTraceResult.getPositionHit();
        }
        return null;
    }

    /* --------------- Start ICollidable and rendering --------------- */

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        if(disableCollisionBox) return null;
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos) {
        return hasFacade(world, pos) ? 255 : 0;
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
    public boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return super.doesSideBlockRendering(world, pos, face) || hasFacade(world, pos);
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
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
        BlockPos blockPos = target.getBlockPos();
        if(hasFacade(world, blockPos)) {
            IBlockState blockState = getFacade(world, blockPos);
            RenderHelpers.addBlockHitEffects(effectRenderer, world, blockState, blockPos, target.sideHit);
            return true;
        } else {
            return super.addHitEffects(world, target, effectRenderer);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        if(hasFacade(world, pos)) {
            return true;
        }
        if(hasPart(world, pos, side)) {
            IPartContainer partContainer = getPartContainer(world, pos);
            IPartType partType = partContainer.getPart(side);
            return partType.isSolid(partContainer.getPartState(side));
        }
        return super.isSideSolid(world, pos, side);
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return true;
    }

    public AxisAlignedBB getCableBoundingBox(EnumFacing side) {
        float min = CableModel.MIN;
        float max = CableModel.MAX;
        if (side == null) {
            return AxisAlignedBB.fromBounds(min, min, min, max, max, max);
        } else {
            float[] b = CABLE_COLLISION_BOXES[side.ordinal()];
            return AxisAlignedBB.fromBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
        }
    }

    protected IPartType.RenderPosition getPartRenderPosition(World world, BlockPos pos, EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty((IExtendedBlockState)
                getExtendedState(world.getBlockState(pos), world, pos), PART_RENDERPOSITIONS[side.ordinal()],
                IPartType.RenderPosition.NONE);
    }

    private AxisAlignedBB getCableBoundingBoxWithPart(World world, BlockPos pos, EnumFacing side) {
        float min = CableModel.MIN;
        float max = CableModel.MAX;
        if (side == null) {
            return AxisAlignedBB.fromBounds(min, min, min, max, max, max);
        } else {
            return getPartRenderPosition(world, pos, side).getSidedCableBoundingBox(side);
        }
    }

    private AxisAlignedBB getPartBoundingBox(World world, BlockPos pos, EnumFacing side) {
        return getPartRenderPosition(world, pos, side).getBoundingBox(side);
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

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
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

    @Override
    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
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
    public int getLightValue(IBlockAccess world, BlockPos pos) {
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

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(world, pos, state, neighborBlock);
        cableNetworkComponent.updateConnections(world, pos);
        networkElementProviderComponent.onBlockNeighborChange(getNetwork(world, pos), world, pos, neighborBlock);
    }
}
