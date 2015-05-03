package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.TickingCyclopsTileEntity;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.ICableConnectable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.*;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A ticking tile entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends TickingCyclopsTileEntity implements IPartContainer {

    private final Map<EnumFacing, PartStateHolder<?, ?>> partData = Maps.newHashMap();

    @NBTPersist private Map<Integer, Boolean> connected = Maps.newHashMap();
    @NBTPersist private Map<Integer, Boolean> forceDisconnected = Maps.newHashMap();

    @Getter
    @Setter
    private Network network;

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList partList = new NBTTagList();
        for(Map.Entry<EnumFacing, PartStateHolder<?, ?>> entry : partData.entrySet()) {
            NBTTagCompound partTag = new NBTTagCompound();
            IPartType part = entry.getValue().getPart();
            IPartState partState = entry.getValue().getState();
            partTag.setString("__partType", part.getType().getName());
            partTag.setString("__side", entry.getKey().getName());
            part.toNBT(partTag, partState);
            partList.appendTag(partTag);
        }
        tag.setTag("parts", partList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        NBTTagList partList = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for(int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound partTag = partList.getCompoundTagAt(i);
            EnumPartType type = EnumPartType.getInstance(partTag.getString("__partType"));
            if(type != null) {
                EnumFacing side = EnumFacing.byName(partTag.getString("__side"));
                if(side != null) {
                    IPartType part = type.getPart();
                    IPartState partState = part.fromNBT(partTag);
                    partData.put(side, PartStateHolder.of(part, partState));
                } else {
                    IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was at an invalid " +
                                    "side and removed.",
                            type, getPosition()));
                }
            } else {
                IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was unknown and removed.",
                        partTag.getString("__partType"), getPosition()));
            }
        }
        super.readFromNBT(tag);
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

    protected void onPartsChanged() {
        markDirty();
    }

    @Override
    public void setPart(EnumFacing side, IPartType part) {
        partData.put(side, PartStateHolder.of(part, part.getDefaultState()));
        if(getNetwork() != null) {
            getNetwork().addNetworkElement(part.createNetworkElement(
                    (IPartContainerFacade) getBlock(), DimPos.of(getWorld(), getPos()), side));
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
    public IPartType removePart(EnumFacing side) {
        IPartType removed = partData.remove(side).getPart();
        onPartsChanged();
        return removed;
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
    public void resetCurrentNetwork() {
        if(network == null)
        setNetwork(null);
    }

    /**
     * Reset and update the cable connections for all sides.
     */
    public void updateCableConnections() {
        World world = getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbourPos = pos.offset(side);
            Block neighbourBlock = world.getBlockState(neighbourPos).getBlock();
            boolean cableConnected = neighbourBlock instanceof ICableConnectable &&
                    ((ICableConnectable) neighbourBlock).canConnect(world, neighbourPos, (ICableConnectable) getBlock(),
                            side.getOpposite());
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
    public void onUpdateReceived() {
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    public IExtendedBlockState getConnectionState() {
        IExtendedBlockState extendedState = (IExtendedBlockState) getBlock().getDefaultState();
        if(connected.isEmpty()) {
            updateCableConnections();
        }
        for(EnumFacing side : EnumFacing.VALUES) {
            extendedState = extendedState.withProperty(BlockCable.CONNECTED[side.ordinal()],
                    !isForceDisconnected(side) && connected.get(side.ordinal()));
        }
        return extendedState;
    }

    public void forceDisconnect(EnumFacing side) {
        forceDisconnected.put(side.ordinal(), true);
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side.ordinal())) return false;
        return forceDisconnected.get(side.ordinal());
    }

    @Override
    protected void updateTileEntity() {
        if(connected.isEmpty()) {
            updateCableConnections();
        }
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
