package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.path.CablePathElement;

import java.io.IOException;
import java.util.*;

/**
 * A part for cables.
 * @author rubensworks
 */
public class PartCable extends Multipart implements ISlottedPart, IHitEffectsPart, IOccludingPart,
        ICableNetwork<IPartNetwork, ICablePathElement>, INetworkElementProvider,
        IDynamicRedstoneBlock, IDynamicLightBlock, INBTProvider, ITileCableNetwork {

    @Delegate
    private final INBTProvider nbtProvider = new NBTProviderComponent(this);
    private final PartCableNetworkComponent cableNetworkComponent = new PartCableNetworkComponent(this);
    private final NetworkElementProviderComponent<IPartNetwork> networkElementProviderComponent = new NetworkElementProviderComponent<>(this);

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();
    @NBTPersist
    private Map<Integer, Boolean> forceDisconnected = Maps.newHashMap();
    @NBTPersist
    private int lightLevel = 0;
    @NBTPersist
    private int redstoneLevel = 0;
    @NBTPersist
    private boolean allowsRedstone = false;
    private IPartNetwork network = null;

    protected ItemStack getItemStack() {
        return new ItemStack(BlockCable.getInstance());
    }
    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
        return getItemStack();
    }

    @Override
    public List<ItemStack> getDrops() {
        return Lists.newArrayList(getItemStack());
    }

    @Override
    public float getHardness(PartMOP hit) {
        return BlockCable.BLOCK_HARDNESS;
    }

    @Override
    public Material getMaterial() {
        return BlockCable.BLOCK_MATERIAL;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {
        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        for(EnumFacing side : EnumFacing.VALUES) {
            extendedState = extendedState.withProperty(BlockCable.CONNECTED[side.ordinal()], isConnected(side));
        }
        return extendedState;
    }

    @Override
    public BlockState createBlockState() {
        return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0], BlockCable.CONNECTED);
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
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(PartMOP partMOP, AdvancedEffectRenderer advancedEffectRenderer) {
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
            }
        }
    }

    protected void addCollisionBoxConditional(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, EnumFacing side) {
        AxisAlignedBB box = BlockCable.getInstance().getCableBoundingBox(side);
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
        }
        return null;
    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        addCollisionBoxConditional(mask, list, collidingEntity, null);
        for(EnumFacing side : EnumFacing.VALUES) {
            if(isConnected(side)) {
                addCollisionBoxConditional(mask, list, collidingEntity, side);
            }
        }
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == EnumWorldBlockLayer.TRANSLUCENT;
    }

    @Override
    public String getModelPath() {
        return IntegratedDynamics._instance.getModId() + ":" + BlockCableConfig._instance.getNamedId();
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.CENTER);
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        buf.writeNBTTagCompoundToBuffer(tag);
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {
        super.readUpdatePacket(buf);
        try {
            readFromNBT(buf.readNBTTagCompoundFromBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        nbtProvider.readGeneratedFieldsFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        nbtProvider.writeGeneratedFieldsToNBT(tag);
    }

    @Override
    public void onAdded() {
        super.onAdded();
        cableNetworkComponent.addToNetwork(getWorld(), getPos());
    }

    @Override
    public void onLoaded() {
        super.onLoaded();
    }

    @Override
    public void onRemoved() {
        World world = getWorld();
        BlockPos pos = getPos();
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        super.onRemoved();
        cableNetworkComponent.onPostBlockDestroyed(world, pos);
    }

    @Override
    public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit) {
        EnumFacing cableConnectionHit = getSubHitSide(hit.subHit);
        return BlockCable.onCableActivated(getWorld(), getPos(), BlockCable.getInstance().getDefaultState(), player, hit.sideHit, cableConnectionHit);
    }

    @Override
    public void onNeighborBlockChange(Block neighborBlock) {
        super.onNeighborBlockChange(neighborBlock);
        World world = getWorld();
        BlockPos pos = getPos();
        networkElementProviderComponent.onBlockNeighborChange(getNetwork(world, pos), world, pos, neighborBlock);
    }

    protected boolean hasPart(EnumFacing side) {
        return false; // TODO
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side.ordinal())) return false;
        return forceDisconnected.get(side.ordinal());
    }

    public void setConnected(EnumFacing side, boolean connected) {
        this.connected.put(side.ordinal(), connected);
    }

    protected void sendUpdate() {
        sendUpdatePacket();
    }

    /* --------------- Start element Providers--------------- */

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        return Collections.emptyList(); // TODO: detect parts
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
        cableNetworkComponent.remove(world, pos, player);
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
        return !isForceDisconnected(side) && getContainer().getPartInSlot(PartSlot.getFaceSlot(side)) == null;
    }

    @Override
    public void updateConnections() {
        World world = getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            boolean cableConnected = CableNetworkComponent.canSideConnect(world, getPos(), side, this);
            setConnected(side, cableConnected);

            // Remove any already existing force-disconnects for this side.
            if(!cableConnected) {
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
        return !isForceDisconnected(side) && connected.get(side.ordinal());
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
}
