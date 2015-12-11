package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
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
import org.cyclops.integrateddynamics.core.network.event.UnknownPartEvent;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A ticking tile entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile,
        IPartContainer, ITileCableNetwork, ITileCableFacadeable {

    private final Map<EnumFacing, PartStateHolder<?, ?>> partData = Maps.newHashMap();
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
        NBTTagList partList = new NBTTagList();
        for(Map.Entry<EnumFacing, PartStateHolder<?, ?>> entry : partData.entrySet()) {
            NBTTagCompound partTag = new NBTTagCompound();
            IPartType part = entry.getValue().getPart();
            IPartState partState = entry.getValue().getState();
            partTag.setString("__partType", part.getName());
            partTag.setString("__side", entry.getKey().getName());
            try {
                part.toNBT(partTag, partState);
                partList.appendTag(partTag);
            } catch (Exception e) {
                e.printStackTrace();
                IntegratedDynamics.clog(Level.ERROR,  String.format("The part %s at position %s was errored " +
                        "and is removed.", part.getName(), getPosition()));
            }
        }
        tag.setTag("parts", partList);
    }

    protected IPartType validatePartType(String partTypeName, IPartType partType) {
        if(partType == null) {
            IPartNetwork network = getNetwork();
            UnknownPartEvent event = new UnknownPartEvent(network, partTypeName);
            network.getEventBus().post(event);
            partType = event.getPartType();
        }
        return partType;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        partData.clear(); // We only want the new data.
        NBTTagList partList = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for(int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound partTag = partList.getCompoundTagAt(i);
            String partTypeName = partTag.getString("__partType");
            IPartType partType = validatePartType(partTypeName, PartTypes.REGISTRY.getPartType(partTypeName));
            if(partType != null) {
                EnumFacing side = EnumFacing.byName(partTag.getString("__side"));
                if(side != null) {
                    IPartState partState = partType.fromNBT(partTag);
                    partData.put(side, PartStateHolder.of(partType, partState));
                } else {
                    IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was at an invalid " +
                                    "side and removed.",
                            partType.getName(), getPosition()));
                }
            } else {
                IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was unknown and removed.",
                        partTypeName, getPosition()));
            }
        }
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
        return Maps.transformValues(partData, new Function<PartStateHolder<?, ?>, IPartType<?, ?>>() {
            @Nullable
            @Override
            public IPartType<?, ?> apply(@Nullable PartStateHolder<?, ?> input) {
                return input.getPart();
            }
        });
    }

    @Override
    public boolean hasParts() {
        return !partData.isEmpty();
    }

    protected void onPartsChanged() {
        markDirty();
        sendUpdate();
        //getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public void setPart(EnumFacing side, IPartType part, IPartState partState) {
        partData.put(side, PartStateHolder.of(part, partState));
        if(getNetwork() != null) {
            INetworkElement networkElement = part.createNetworkElement(
                    (IPartContainerFacade) getBlock(), DimPos.of(getWorld(), getPos()), side);
            if(!getNetwork().addNetworkElement(networkElement, false)) {
                // In this case, the addition failed because that part id is already present in the network,
                // therefore we have to make a new state for that part (with a new id) and retry.
                partState = part.getDefaultState();
                partData.put(side, PartStateHolder.of(part, partState));
                IntegratedDynamics.clog(Level.WARN, "A part already existed in the network, this is possibly a " +
                        "result from item duplication.");
                getNetwork().addNetworkElement(networkElement, false);
            }
        }
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
        PartStateHolder<?, ?> partStateHolder = partData.get(side); // Don't remove the state just yet! We might need it in network removal.
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
                networkElement.addDrops(itemStacks);
                for(ItemStack itemStack : itemStacks) {
                    if(player != null) {
                        ItemStackHelpers.spawnItemStackToPlayer(getWorld(), pos, itemStack, player);
                    } else {
                        Block.spawnAsEntity(getWorld(), pos, itemStack);
                    }
                }

                // Remove the element from the network.
                getNetwork().removeNetworkElementPost(networkElement);
            }
            // Finally remove the part data from this tile.
            IPartType ret = partData.remove(side).getPart();
            onPartsChanged();
            return ret;
        }
    }

    @Override
    public void setPartState(EnumFacing side, IPartState partState) {
        PartStateHolder<?, ?> partStateHolder = partData.get(side);
        if(partStateHolder == null) {
            throw new IllegalArgumentException(String.format("No part at position %s was found to update the state " +
                    "for.", getPosition()));
        }
        partData.put(side, PartStateHolder.of(partStateHolder.getPart(), partState));
        onPartsChanged();
    }

    @Override
    public IPartState getPartState(EnumFacing side) {
        PartStateHolder<?, ?> partStateHolder = partData.get(side);
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
            boolean hasPart = hasPart(side);
            extendedState = extendedState.withProperty(BlockCable.PART[side.ordinal()], hasPart);
            if(hasPart) {
                extendedState = extendedState.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], getPart(side).getRenderPosition());
            } else {
                extendedState = extendedState.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], IPartType.RenderPosition.NONE);
            }
        }
        extendedState = extendedState.withProperty(BlockCable.FACADE, hasFacade() ? Optional.of(getFacade()) : Optional.absent());
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
            for (PartStateHolder<?, ?> partStateHolder : partData.values()) {
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
        return TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IPartContainer.class);
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

    @Data
    private static class PartStateHolder<P extends IPartType<P, S>, S extends IPartState<P>> {

        private final IPartType<P, S> part;
        private final S state;

        public static PartStateHolder<?, ?> of(IPartType part, IPartState partState) {
            return new PartStateHolder(part, partState);
        }

    }

}
