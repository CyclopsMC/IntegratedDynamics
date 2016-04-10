package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.client.multipart.AdvancedEffectRenderer;
import mcmultipart.multipart.*;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.path.CablePathElement;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A part for cables.
 * @author rubensworks
 */
public class PartCable extends MultipartBase implements ICableNetwork<IPartNetwork, ICablePathElement>,
        INetworkElementProvider, IDynamicRedstoneBlock, IDynamicLightBlock, ITileCableNetwork, IPartContainerFacade, ITickable {

    private final PartCableNetworkComponent cableNetworkComponent = new PartCableNetworkComponent(this);
    private final NetworkElementProviderComponent<IPartNetwork> networkElementProviderComponent = new NetworkElementProviderComponent<>(this);

    private final Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData;
    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();
    @NBTPersist
    private Map<Integer, Boolean> forceDisconnected;
    @NBTPersist
    private int lightLevel = 0;
    @NBTPersist
    private int redstoneLevel = 0;
    @NBTPersist
    private boolean allowsRedstone = false;
    private IPartNetwork network = null;
    private IPartContainer partContainer = null;
    private boolean addSilent = false;
    private boolean sendFurtherUpdates = true;

    public PartCable() {
        this(Maps.<EnumFacing, PartHelpers.PartStateHolder<?, ?>>newHashMap(), Maps.<Integer, Boolean>newHashMap());
    }

    public PartCable(Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData, Map<Integer, Boolean> forceDisconnected) {
        this.partData = partData;
        this.forceDisconnected = forceDisconnected;
    }

    /**
     * @return The raw part data.
     */
    public Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return this.partData;
    }

    /**
     * @return The raw force disconnection data.
     */
    public Map<Integer, Boolean> getForceDisconnected() {
        return this.forceDisconnected;
    }

    @Override
    protected ItemStack getItemStack() {
        return new ItemStack(BlockCable.getInstance());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {
        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        for(EnumFacing side : EnumFacing.VALUES) {
            extendedState = extendedState.withProperty(BlockCable.CONNECTED[side.ordinal()], isConnected(side));
            boolean hasPart = hasPart(side);
            if(hasPart) {
                extendedState = extendedState.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], getPart(side).getRenderPosition());
            } else {
                extendedState = extendedState.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], IPartType.RenderPosition.NONE);
            }
        }
        return extendedState;
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0],
                ArrayUtils.addAll(BlockCable.PART_RENDERPOSITIONS, BlockCable.CONNECTED));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return BlockCable.getInstance().getCableBoundingBox(null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(AdvancedEffectRenderer advancedEffectRenderer) {
        advancedEffectRenderer.addBlockDestroyEffects(getPos(), BlockCable.getInstance().texture);
        return true;
    }

    @Override
    public void addOcclusionBoxes(List<AxisAlignedBB> list) {
        list.add(BlockCable.getInstance().getCableBoundingBox(null));
    }

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        list.add(BlockCable.getInstance().getCableBoundingBox(null));
        for(EnumFacing side : EnumFacing.VALUES) {
            if(isConnected(side)) {
                list.add(BlockCable.getInstance().getCableBoundingBox(side));
            } else if(hasPart(side)) {
                list.add(getPart(side).getRenderPosition().getSidedCableBoundingBox(side));
            }
        }
    }

    protected void addCollisionBoxConditional(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, EnumFacing side) {
        AxisAlignedBB box = BlockCable.getInstance().getCableBoundingBox(side);
        if(box.intersectsWith(mask)) {
            list.add(box);
        }
    }

    protected void addCollisionBoxWithPartConditional(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, EnumFacing side) {
        AxisAlignedBB box = getPart(side).getRenderPosition().getSidedCableBoundingBox(side);
        if(box.intersectsWith(mask)) {
            list.add(box);
        }
    }

    protected EnumFacing getSubHitSide(int subHit) {
        if(subHit == -1) return null;
        int i = 0;
        for(EnumFacing side : EnumFacing.VALUES) {
            if(isConnected(side)) {
                if(i == subHit) {
                    return side;
                }
                i++;
            }
            if(hasPart(side)) {
                if(i == subHit) {
                    return side;
                }
                i++;
            }
        }
        return null;
    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        addCollisionBoxConditional(mask, list, collidingEntity, null);
        for(EnumFacing side : EnumFacing.VALUES) {
            if(isConnected(side)) {
                addCollisionBoxConditional(mask, list, collidingEntity, side);
            } else if(hasPart(side)) {
                addCollisionBoxWithPartConditional(mask, list, collidingEntity, side);
            }
        }
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public ResourceLocation getModelPath() {
        return new ResourceLocation(IntegratedDynamics._instance.getModId(), BlockCableConfig._instance.getNamedId());
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.CENTER);
    }

    public void detectPresentParts() {
        IMultipartContainer container = getContainer();
        for(IMultipart multipart : container.getParts()) {
            if(multipart instanceof PartPartType) {
                final PartPartType partPart = (PartPartType) multipart;
                PartHelpers.setPart(getNetwork(), getWorld(), getPos(), partPart.getFacing(), partPart.getPartType(), partPart.getPartType().getDefaultState(), new PartHelpers.IPartStateHolderCallback() {
                    @Override
                    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                        partData.put(partPart.getFacing(), partStateHolder);
                        sendUpdate();
                    }
                });
            }
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if(!isAddSilent()) {
            cableNetworkComponent.addToNetwork(getWorld(), getPos());
            detectPresentParts();
        }
    }

    @Override
    public void harvest(EntityPlayer player, PartMOP hit) {
        World world = getWorld();
        BlockPos pos = getPos();
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos, false);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        super.harvest(player, hit);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        cableNetworkComponent.onPostBlockDestroyed(getWorld(), getPos());
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
        EnumFacing cableConnectionHit = getSubHitSide(hit.subHit);
        return !getWorld().isRemote ?
                BlockCable.onCableActivated(getWorld(), getPos(), BlockCable.getInstance().getDefaultState(), player, hand, player.getHeldItem(hand), hit.sideHit, cableConnectionHit)
                : super.onActivated(player, hand, stack, hit);
    }

    @Override
    public void onNeighborBlockChange(Block neighborBlock) {
        super.onNeighborBlockChange(neighborBlock);
        World world = getWorld();
        BlockPos pos = getPos();
        cableNetworkComponent.updateConnections(world, pos);
        networkElementProviderComponent.onBlockNeighborChange(getNetwork(world, pos), world, pos, neighborBlock);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        PartHelpers.writePartsToNBT(getPos(), tag, this.partData);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        synchronized (this.partData) {
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, this.partData);
        }
        super.readFromNBT(tag);
    }

    protected boolean hasPart(EnumFacing side) {
        return getPartContainer().hasPart(side);
    }

    protected IPartType getPart(EnumFacing side) {
        return getPartContainer().getPart(side);
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side.ordinal())) return false;
        return forceDisconnected.get(side.ordinal());
    }

    public void setConnected(EnumFacing side, boolean connected) {
        this.connected.put(side.ordinal(), connected);
    }

    @Override
    public void onPartChanged(IMultipart part) {
        super.onPartChanged(part);
        if(sendFurtherUpdates) {
            updateConnections();
        }
    }

    /* --------------- Start element Providers--------------- */

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        Set<INetworkElement> sidedElements = Sets.newHashSet();
        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : getPartContainer().getParts().entrySet()) {
            if(getPartContainer().getPartState(entry.getKey()) != null) {
                sidedElements.add(entry.getValue().createNetworkElement(this, DimPos.of(world, blockPos), entry.getKey()));
            }
        }
        return sidedElements;
    }

    @Override
    public ICablePathElement createPathElement(World world, BlockPos blockPos) {
        return new CablePathElement(this, DimPos.of(world, blockPos));
    }

    /* --------------- Delegate to ICableNetwork<CablePathElement> --------------- */

    @Override
    public void initNetwork(World world, BlockPos pos) {
        cableNetworkComponent.initNetwork(world, pos);
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
        return cableNetworkComponent.isConnected(world, pos, side);
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
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos, false);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        cableNetworkComponent.remove(world, pos, player);
        cableNetworkComponent.onPostBlockDestroyed(world, pos);
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

    /* --------------- Start IDynamicLightBlock --------------- */

    @Override
    public void setLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level) {
        this.lightLevel = level;
        sendUpdate();
    }

    @Override
    public int getLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return this.lightLevel;
    }

    /* --------------- Start IDynamicRedstoneBlock --------------- */

    @Override
    public void disableRedstoneAt(IBlockAccess world, BlockPos pos, EnumFacing side) {
        setRedstoneLevel(world, pos, side, 0);
    }

    @Override
    public void setRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level) {
        this.redstoneLevel = level;
        sendUpdate();
    }

    @Override
    public int getRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return this.redstoneLevel;
    }

    @Override
    public void setAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side, boolean allow) {
        this.allowsRedstone = allow;
        sendUpdate();
    }

    @Override
    public boolean isAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return this.allowsRedstone;
    }

    /* --------------- Start ITileCableNetwork --------------- */

    @Override
    public boolean canConnect(ICable connector, EnumFacing side) {
        return !isForceDisconnected(side)
                && getContainer().getPartInSlot(PartSlot.getFaceSlot(side)) == null
                && OcclusionHelper.occlusionTest(getContainer().getParts(), new Predicate<IMultipart>() {
            @Override
            public boolean apply(@Nullable IMultipart input) {
                return input == PartCable.this;
            }
        }, BlockCable.getInstance().getCableBoundingBox(side));
    }

    @Override
    public void updateConnections() {
        World world = getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            boolean canConnectThis = this.canConnect(this, side);
            boolean cableConnected = canConnectThis && CableNetworkComponent.canSideConnect(world, getPos(), side, this);
            setConnected(side, cableConnected);

            // Remove any already existing force-disconnects for this side.
            if (!cableConnected && canConnectThis) {
                forceDisconnected.put(side.ordinal(), false);
            }
        }
        markDirty();
        sendUpdate();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        if(getPos() == null) {
            return false;
        }
        if(connected.isEmpty()) {
            updateConnections();
        }
        return !isForceDisconnected(side) && connected != null && connected.get(side.ordinal());
    }

    @Override
    public void disconnect(EnumFacing side) {
        forceDisconnected.put(side.ordinal(), true);
    }

    @Override
    public void reconnect(EnumFacing side) {
        forceDisconnected.remove(side.ordinal());
    }

    @Override
    public void resetCurrentNetwork() {
        this.network = null;
    }

    @Override
    public void setNetwork(IPartNetwork network) {
        this.network = network;
    }

    @Override
    public IPartNetwork getNetwork() {
        return this.network;
    }

    protected IPartContainer getPartContainer() {
        if(this.partContainer == null) {
            this.partContainer = new VirtualPartContainer();
        }
        return this.partContainer;
    }

    @Override
    public IPartContainer getPartContainer(IBlockAccess world, BlockPos pos) {
        return getPartContainer();
    }

    @Nullable
    @Override
    public EnumFacing getWatchingSide(World world, BlockPos pos, EntityPlayer player) {
        Vec3d start = RayTraceUtils.getStart(player);
        Vec3d end = RayTraceUtils.getEnd(player);
        RayTraceUtils.AdvancedRayTraceResultPart result = ((TileMultipartContainer) world.getTileEntity(pos)).getPartContainer().collisionRayTrace(start, end);
        if(result == null || result.hit == null) return null;
        IMultipart multipart = result.hit.partHit;
        if(!(multipart instanceof PartPartType)) return null;
        PartPartType partPartType = (PartPartType) result.hit.partHit;
        return partPartType != null ? partPartType.getFacing() : null;
    }

    @Override
    public void update() {
        if(!MinecraftHelpers.isClientSide()) {
            for(PartHelpers.PartStateHolder<?, ?> partStateHolder : this.partData.values()) {
                IPartState partState = partStateHolder.getState();
                if (partState.isDirtyAndReset()) {
                    markDirty();
                }
                if (partState.isUpdateAndReset()) {
                    sendUpdate();
                }
            }
        }
    }

    public void setAddSilent(boolean addSilent) {
        this.addSilent = addSilent;
    }

    public boolean isAddSilent() {
        return this.addSilent;
    }

    public void setSendFurtherUpdates(boolean sendFurtherUpdates) {
        this.sendFurtherUpdates = sendFurtherUpdates;
    }

    public class VirtualPartContainer implements IPartContainer {

        protected PartPartType getPartPart(EnumFacing side) {
            IMultipartContainer container = getContainer();
            if(container != null) {
                IMultipart multipart = container.getPartInSlot(PartSlot.getFaceSlot(side));
                if (multipart instanceof PartPartType) {
                    return (PartPartType) multipart;
                }
            }
            return null;
        }

        @Override
        public DimPos getPosition() {
            return DimPos.of(getWorld(), getPos());
        }

        @Override
        public Map<EnumFacing, IPartType<?, ?>> getParts() {
            Map<EnumFacing, IPartType<?, ?>> parts = Maps.newHashMap();
            for(EnumFacing side : EnumFacing.VALUES) {
                IPartType partType = getPart(side);
                if(partType != null) {
                    parts.put(side, partType);
                }
            }
            return parts;
        }

        @Override
        public boolean hasParts() {
            for(IMultipart multipart : getContainer().getParts()) {
                if(multipart instanceof PartPartType) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <P extends IPartType<P, S>, S extends IPartState<P>> void setPart(final EnumFacing side, IPartType<P, S> part, IPartState<P> partState) {
            final PartPartType partPart = new PartPartType(side, part);
            PartHelpers.setPart(getNetwork(), getWorld(), getPos(), side, part, partState, new PartHelpers.IPartStateHolderCallback() {
                @Override
                public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                    partData.put(side, partStateHolder);
                    sendUpdate();
                }
            });
            MultipartHelper.addPart(getWorld(), getPos(), partPart);
        }

        @Override
        public <P extends IPartType<P, S>, S extends IPartState<P>> boolean canAddPart(EnumFacing side, IPartType<P, S> part) {
            return !hasPart(side) && MultipartHelper.canAddPart(getWorld(), getPos(), new PartPartType(side, part));
        }

        @Override
        public IPartType getPart(EnumFacing side) {
            PartPartType partPartType = getPartPart(side);
            if(partPartType != null) {
                return partPartType.getPartType();
            }
            return null;
        }

        @Override
        public boolean hasPart(EnumFacing side) {
            return getPart(side) != null;
        }

        @Override
        public IPartType removePart(EnumFacing side, EntityPlayer player) {
            PartPartType partPartType = getPartPart(side);
            if(partPartType != null) {
                IPartType removed = partPartType.getPartType();
                for(ItemStack itemStack : partPartType.getDrops()) {
                    ItemStackHelpers.spawnItemStackToPlayer(getWorld(), getPos(), itemStack, player);
                }
                getContainer().removePart(partPartType);
                partData.remove(side);
                sendUpdate();
                return removed;
            }
            return null;
        }

        @Override
        public void setPartState(EnumFacing side, IPartState partState) {
            partData.put(side, PartHelpers.PartStateHolder.of(getPart(side), partState));
            sendUpdate();
        }

        @Override
        public IPartState getPartState(EnumFacing side) {
            synchronized (partData) {
                PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
                if(partStateHolder != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    partStateHolder.getState().writeToNBT(tag);
                    return partStateHolder.getState();
                }
            }
            return null;
        }
    }

}
