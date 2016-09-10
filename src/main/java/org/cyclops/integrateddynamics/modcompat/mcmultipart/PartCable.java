package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.base.Predicate;
import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.OcclusionHelper;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.block.property.ExtendedBlockStateBuilder;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.capability.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.NetworkElementProviderPartContainer;
import org.cyclops.integrateddynamics.capability.PartContainerConfig;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.path.CablePathElement;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * A part for cables.
 * @author rubensworks
 */
public class PartCable extends MultipartBase implements ICableNetwork<IPartNetwork, ICablePathElement>,
        IDynamicRedstoneBlock, IDynamicLightBlock, ITileCableNetwork, ITickable {

    private final PartCableNetworkComponent cableNetworkComponent = new PartCableNetworkComponent(this);
    private final NetworkElementProviderComponent<IPartNetwork> networkElementProviderComponent = new NetworkElementProviderComponent<>();

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    @NBTPersist
    private EnumFacingMap<Boolean> forceDisconnected;
    @NBTPersist
    private int lightLevel = 0;
    @NBTPersist
    private int redstoneLevel = 0;
    @NBTPersist
    private boolean allowsRedstone = false;
    private IPartNetwork network = null;
    private final PartContainerPartCable partContainer;
    private final INetworkElementProvider<IPartNetwork> networkElementProvider;
    private boolean addSilent = false;
    private boolean sendFurtherUpdates = true;

    public PartCable() {
        this(EnumFacingMap.<PartHelpers.PartStateHolder<?, ?>>newMap(), EnumFacingMap.<Boolean>newMap());
    }

    public PartCable(EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData, EnumFacingMap<Boolean> forceDisconnected) {
        partContainer = new PartContainerPartCable(this);
        partContainer.setPartData(partData);
        networkElementProvider = new NetworkElementProviderPartContainer(partContainer);
        this.forceDisconnected = forceDisconnected;
    }

    /**
     * @return The raw part data.
     */
    public EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return partContainer.getPartData();
    }

    /**
     * @return The raw force disconnection data.
     */
    public EnumFacingMap<Boolean> getForceDisconnected() {
        return this.forceDisconnected;
    }

    @Override
    protected ItemStack getItemStack() {
        return new ItemStack(BlockCable.getInstance());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ExtendedBlockStateBuilder builder = ExtendedBlockStateBuilder.builder((IExtendedBlockState) state);
        for(EnumFacing side : EnumFacing.VALUES) {
            builder.withProperty(BlockCable.CONNECTED[side.ordinal()], isConnected(side));
            boolean hasPart = hasPart(side);
            if(hasPart) {
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], getPart(side).getPartRenderPosition());
            } else {
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], PartRenderPosition.NONE);
            }
        }
        return builder.build();
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
    public boolean addDestroyEffects(AdvancedParticleManager advancedParticleManager) {
        advancedParticleManager.addBlockDestroyEffects(getPos(), BlockCable.getInstance().texture);
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
                list.add(getPart(side).getPartRenderPosition().getSidedCableBoundingBox(side));
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
        AxisAlignedBB box = getPart(side).getPartRenderPosition().getSidedCableBoundingBox(side);
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
                        partContainer.getPartData().put(partPart.getFacing(), partStateHolder);
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("partContainer", partContainer.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("parts", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())
                && !tag.hasKey("partContainer", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            // Backwards compatibility with old part saving.
            // TODO: remove in next major MC update.
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, partContainer.getPartData(), getWorld());
        } else {
            partContainer.deserializeNBT(tag.getCompoundTag("partContainer"));
        }
        super.readFromNBT(tag);
    }

    protected boolean hasPart(EnumFacing side) {
        return partContainer.hasPart(side);
    }

    protected IPartType getPart(EnumFacing side) {
        return partContainer.getPart(side);
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side)) return false;
        return forceDisconnected.get(side);
    }

    public void setConnected(EnumFacing side, boolean connected) {
        this.connected.put(side, connected);
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
                && OcclusionHelper.occlusionTest(OcclusionHelper.boxes(BlockCable.getInstance().getCableBoundingBox(side)), new Predicate<IMultipart>() {
            @Override
            public boolean apply(@Nullable IMultipart input) {
                return input == PartCable.this;
            }
        }, getContainer().getParts());
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
                forceDisconnected.put(side, false);
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
        return !isForceDisconnected(side) && connected != null && connected.get(side);
    }

    @Override
    public void disconnect(EnumFacing side) {
        forceDisconnected.put(side, true);
    }

    @Override
    public void reconnect(EnumFacing side) {
        forceDisconnected.remove(side);
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

    @Override
    public void update() {
        partContainer.update();
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

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == PartContainerConfig.CAPABILITY
                || capability == NetworkElementProviderConfig.CAPABILITY
                || partContainer.hasCapability(capability, facing)
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == PartContainerConfig.CAPABILITY) {
            return (T) partContainer;
        }
        if (capability == NetworkElementProviderConfig.CAPABILITY) {
            return (T) networkElementProvider;
        }
        T t = partContainer.getCapability(capability, facing);
        if (t != null) {
            return t;
        }
        return super.getCapability(capability, facing);
    }

}
