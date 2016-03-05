package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableFacadeable;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A ticking tile entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile,
        IPartContainer, ITileCableNetwork, ITileCableFacadeable, PartHelpers.IPartStateHolderCallback {

    private final Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData = Maps.newHashMap();
    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist private boolean realCable = true;
    @NBTPersist private Map<Integer, Boolean> connected = Maps.newHashMap();
    @NBTPersist private Map<Integer, Boolean> forceDisconnected = Maps.newHashMap();
    @NBTPersist private Map<Integer, Integer> redstoneLevels = Maps.newHashMap();
    @NBTPersist private Map<Integer, Boolean> redstoneInputs = Maps.newHashMap();
    @NBTPersist private Map<Integer, Integer> lightLevels = Maps.newHashMap();
    private Map<Integer, Integer> previousLightLevels;
    @NBTPersist private String facadeBlockName = null;
    @NBTPersist private int facadeMeta = 0;

    @Getter
    @Setter
    private IPartNetwork network;

    @Override
    public void writeToNBT(NBTTagCompound tag) {this.markDirty();
        super.writeToNBT(tag);
        PartHelpers.writePartsToNBT(getPos(), tag, this.partData);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, this.partData);
        super.readFromNBT(tag);
    }

    /**
     * Indicate that this cable is not a real cable if false and should not allow any connections.
     * Parts can be added to it though.
     * @param realCable If this cable is real and should accept connections.
     */
    public void setRealCable(boolean realCable) {
        this.realCable = realCable;
        sendUpdate();
    }

    /**
     * @return If this cable is real.
     */
    public boolean isRealCable() {
        return this.realCable;
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(getWorld(), getPos());
    }

    @Override
    public Map<EnumFacing, IPartType<?, ?>> getParts() {
        return Maps.transformValues(partData, new Function<PartHelpers.PartStateHolder<?, ?>, IPartType<?, ?>>() {
            @Nullable
            @Override
            public IPartType<?, ?> apply(@Nullable PartHelpers.PartStateHolder<?, ?> input) {
                return input.getPart();
            }
        });
    }

    @Override
    public boolean hasParts() {
        return !partData.isEmpty();
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean canAddPart(EnumFacing side, IPartType<P, S> part) {
        return !hasPart(side);
    }

    protected void onPartsChanged() {
        markDirty();
        sendUpdate();
        //getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public void setPart(final EnumFacing side, final IPartType part, final IPartState partState) {
        PartHelpers.setPart(getNetwork(), getWorld(), getPos(), side, part, partState, new PartHelpers.IPartStateHolderCallback() {
            @Override
            public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                partData.put(side, PartHelpers.PartStateHolder.of(part, partState));
            }
        });
        onPartsChanged();
    }

    @Override
    public IPartType getPart(EnumFacing side) {
        if(!partData.containsKey(side)) return null;
        return partData.get(side).getPart();
    }

    @Override
    public boolean hasPart(EnumFacing side) {
        return partData.containsKey(side);
    }

    @Override
    public IPartType removePart(EnumFacing side, EntityPlayer player) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side); // Don't remove the state just yet! We might need it in network removal.
        if(partStateHolder == null) {
            IntegratedDynamics.clog(Level.WARN, "Attempted to remove a part at a side where no part was.");
            return null;
        } else {
            IPartType removed = partStateHolder.getPart();
            if (getNetwork() != null) {
                INetworkElement networkElement = removed.createNetworkElement(
                        (IPartContainerFacade) getBlock(), DimPos.of(getWorld(), getPos()), side);
                if(!getNetwork().removeNetworkElementPre(networkElement)) {
                    return null;
                }

                // Drop all parts types as item.
                List<ItemStack> itemStacks = Lists.newLinkedList();
                networkElement.addDrops(itemStacks, true);
                for(ItemStack itemStack : itemStacks) {
                    if(player != null) {
                        ItemStackHelpers.spawnItemStackToPlayer(getWorld(), pos, itemStack, player);
                    } else {
                        Block.spawnAsEntity(getWorld(), pos, itemStack);
                    }
                }

                // Remove the element from the network.
                getNetwork().removeNetworkElementPost(networkElement);
            } else {
                ItemStackHelpers.spawnItemStackToPlayer(getWorld(), pos, new ItemStack(removed.getItem()), player);
            }
            // Finally remove the part data from this tile.
            IPartType ret = partData.remove(side).getPart();
            onPartsChanged();
            return ret;
        }
    }

    @Override
    public void setPartState(EnumFacing side, IPartState partState) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to update the state " +
                    "for.", getPosition()));
        }
        partData.put(side, PartHelpers.PartStateHolder.of(partStateHolder.getPart(), partState));
        onPartsChanged();
    }

    @Override
    public IPartState getPartState(EnumFacing side) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to get the state from.",
                    getPosition()));
        }
        return partStateHolder.getState();
    }

    @Override
    public boolean hasFacade() {
        return facadeBlockName != null && !facadeBlockName.isEmpty();
    }

    @Override
    public IBlockState getFacade() {
        if(!hasFacade()) {
            return null;
        }
        return BlockHelpers.deserializeBlockState(Pair.of(this.facadeBlockName, this.facadeMeta));
    }

    @Override
    public void setFacade(@Nullable IBlockState blockState) {
        if(blockState == null) {
            this.facadeMeta = 0;
            this.facadeBlockName = null;
        } else {
            Pair<String, Integer> serializedBlockState = BlockHelpers.serializeBlockState(blockState);
            this.facadeMeta = serializedBlockState.getRight();
            this.facadeBlockName = serializedBlockState.getLeft();
        }
        sendUpdate();
    }

    @Override
    public void onUpdateReceived() {
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
        if(!lightLevels.equals(previousLightLevels)) {
            previousLightLevels = lightLevels;
            getWorld().checkLight(getPos());
        }

    }

    public IExtendedBlockState getConnectionState() {
        IExtendedBlockState extendedState = (IExtendedBlockState) getBlock().getDefaultState();
        extendedState = extendedState.withProperty(BlockCable.REALCABLE, isRealCable());
        if(connected.isEmpty()) {
            updateConnections();
        }
        for(EnumFacing side : EnumFacing.VALUES) {
            extendedState = extendedState.withProperty(BlockCable.CONNECTED[side.ordinal()],
                    !isForceDisconnected(side) && connected.get(side.ordinal()));
            extendedState = extendedState.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()],
                    hasPart(side) ? getPart(side).getRenderPosition() : IPartType.RenderPosition.NONE);
        }
        extendedState = extendedState.withProperty(BlockCable.FACADE, hasFacade() ? Optional.of(getFacade()) : Optional.absent());
        extendedState = extendedState.withProperty(BlockCable.PARTCONTAINER, this);
        return extendedState;
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(!isRealCable() || hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side.ordinal())) return false;
        return forceDisconnected.get(side.ordinal());
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        // If the connection data were reset, update the cable connections
        if(connected.isEmpty()) {
            updateConnections();
        }

        if(!MinecraftHelpers.isClientSide()) {
            // Loop over all part states to check their dirtiness
            for (PartHelpers.PartStateHolder<?, ?> partStateHolder : partData.values()) {
                if (partStateHolder.getState().isDirtyAndReset()) {
                    markDirty();
                }
                if (partStateHolder.getState().isUpdateAndReset()) {
                    sendUpdate();
                }
            }
        }
    }

    protected void updateRedstoneInfo(EnumFacing side) {
        sendUpdate();
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlock());
        getWorld().notifyNeighborsOfStateChange(pos.offset(side.getOpposite()), getBlock());
    }

    public void setRedstoneLevel(EnumFacing side, int level) {
        if(!getWorld().isRemote) {
            boolean sendUpdate = false;
            if(redstoneLevels.containsKey(side.ordinal())) {
                if(redstoneLevels.get(side.ordinal()) != level) {
                    sendUpdate = true;
                    redstoneLevels.put(side.ordinal(), level);
                }
            } else {
                sendUpdate = true;
                redstoneLevels.put(side.ordinal(), level);
            }
            if(sendUpdate) {
                updateRedstoneInfo(side);
            }
        }
    }

    public int getRedstoneLevel(EnumFacing side) {
        if(redstoneLevels.containsKey(side.ordinal())) {
            return redstoneLevels.get(side.ordinal());
        }
        return -1;
    }

    public void setAllowRedstoneInput(EnumFacing side, boolean allow) {
        redstoneInputs.put(side.ordinal(), allow);
    }

    public boolean isAllowRedstoneInput(EnumFacing side) {
        if(redstoneInputs.containsKey(side.ordinal())) {
            return redstoneInputs.get(side.ordinal());
        }
        return false;
    }

    public void disableRedstoneLevel(EnumFacing side) {
        if(!getWorld().isRemote) {
            redstoneLevels.remove(side.ordinal());
            updateRedstoneInfo(side);
        }
    }

    protected void updateLightInfo(EnumFacing side) {
        sendUpdate();
    }

    public void setLightLevel(EnumFacing side, int level) {
        if(!getWorld().isRemote) {
            boolean sendUpdate = false;
            if(lightLevels.containsKey(side.ordinal())) {
                if(lightLevels.get(side.ordinal()) != level) {
                    sendUpdate = true;
                    lightLevels.put(side.ordinal(), level);
                }
            } else {
                sendUpdate = true;
                lightLevels.put(side.ordinal(), level);
            }
            if(sendUpdate) {
                updateLightInfo(side);
            }
        }
    }

    public int getLightLevel(EnumFacing side) {
        if(lightLevels.containsKey(side.ordinal())) {
            return lightLevels.get(side.ordinal());
        }
        return 0;
    }

    /**
     * Get the part container at the given position.
     * @param pos The position.
     * @return The container or null.
     */
    public static IPartContainer get(DimPos pos) {
        IPartContainerFacade partContainerFacade = CableHelpers.getInterface(pos, IPartContainerFacade.class);
        return partContainerFacade.getPartContainer(pos.getWorld(), pos.getBlockPos());
    }

    @Override
    public void resetCurrentNetwork() {
        if(network != null) setNetwork(null);
    }

    @Override
    public boolean canConnect(ICable connector, EnumFacing side) {
        return !isForceDisconnected(side);
    }

    @Override
    public void updateConnections() {
        World world = getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            boolean cableConnected = CableNetworkComponent.canSideConnect(world, pos, side, (ICable) getBlock());
            connected.put(side.ordinal(), cableConnected);

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
        return connected.containsKey(side.ordinal()) && connected.get(side.ordinal());
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
    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {

    }

    /**
     * @return The raw part data.
     */
    public Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return this.partData;
    }

    /**
     * Override the part data.
     * @param partData The raw part data.
     */
    public void setPartData(Map<EnumFacing, PartHelpers.PartStateHolder<?, ?>> partData) {
        this.partData.clear();
        this.partData.putAll(partData);
    }

    /**
     * @return The raw force disconnection data.
     */
    public Map<Integer, Boolean> getForceDisconnected() {
        return this.forceDisconnected;
    }

    public void setForceDisconnected(Map<Integer, Boolean> forceDisconnected) {
        this.forceDisconnected.clear();
        this.forceDisconnected.putAll(forceDisconnected);
    }

    /**
     * Reset the part data without signaling any neighbours or the network.
     * Is used in block conversion.
     */
    public void silentResetPartData() {
        this.partData.clear();
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return true;
    }
}
