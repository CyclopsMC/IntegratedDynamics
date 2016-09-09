package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.block.property.ExtendedBlockStateBuilder;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableFacadeable;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.capability.PartContainerConfig;
import org.cyclops.integrateddynamics.capability.TileMultipartTickingPartContainer;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A ticking tile entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile,
        ITileCableNetwork, ITileCableFacadeable, PartHelpers.IPartStateHolderCallback {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist private boolean realCable = true;
    @NBTPersist private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    @NBTPersist private EnumFacingMap<Boolean> forceDisconnected = EnumFacingMap.newMap();
    @NBTPersist private EnumFacingMap<Integer> redstoneLevels = EnumFacingMap.newMap();
    @NBTPersist private EnumFacingMap<Boolean> redstoneInputs = EnumFacingMap.newMap();
    @NBTPersist private EnumFacingMap<Integer> lightLevels = EnumFacingMap.newMap();
    private EnumFacingMap<Integer> previousLightLevels;
    @NBTPersist private String facadeBlockName = null;
    @NBTPersist private int facadeMeta = 0;

    @Getter
    @Setter
    private IPartNetwork network;

    @Getter
    private final TileMultipartTickingPartContainer partContainer;

    public TileMultipartTicking() {
        partContainer = new TileMultipartTickingPartContainer(this);
        addCapabilityInternal(PartContainerConfig.CAPABILITY, partContainer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {this.markDirty();
        tag = super.writeToNBT(tag);
        tag.setTag("partContainer", partContainer.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        EnumFacingMap<Boolean> lastConnected = connected;
        String lastFacadeBlockName = facadeBlockName;
        int lastFacadeMeta = facadeMeta;
        boolean lastRealCable = realCable;
        if (tag.hasKey("parts", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())
                && !tag.hasKey("partContainer", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            // Backwards compatibility with old part saving.
            // TODO: remove in next major MC update.
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, partContainer.getPartData(), getWorld());
        } else {
            partContainer.deserializeNBT(tag.getCompoundTag("partContainer"));
        }

        super.readFromNBT(tag);
        if (getWorld() != null && (lastConnected == null || connected == null || !lastConnected.equals(connected)
                || !Objects.equals(lastFacadeBlockName, facadeBlockName) || lastFacadeMeta != facadeMeta
                || lastRealCable != realCable)) {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
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
        if(!lightLevels.equals(previousLightLevels)) {
            previousLightLevels = lightLevels;
            getWorld().checkLight(getPos());
        }

    }

    public IExtendedBlockState getConnectionState() {
        ExtendedBlockStateBuilder builder = ExtendedBlockStateBuilder.builder((IExtendedBlockState) getBlock().getDefaultState());
        if (partContainer.getPartData() != null) { // Can be null in rare cases where rendering happens before data sync
            builder.withProperty(BlockCable.REALCABLE, isRealCable());
            if (connected.isEmpty()) {
                updateConnections();
            }
            for (EnumFacing side : EnumFacing.VALUES) {
                builder.withProperty(BlockCable.CONNECTED[side.ordinal()],
                        !isForceDisconnected(side) && connected.get(side));
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()],
                        partContainer.hasPart(side) ? partContainer.getPart(side).getPartRenderPosition() : PartRenderPosition.NONE);
            }
            builder.withProperty(BlockCable.FACADE, hasFacade() ? Optional.of(getFacade()) : Optional.absent());
            builder.withProperty(BlockCable.PARTCONTAINER, partContainer);
        }
        return builder.build();
    }

    public boolean isForceDisconnected(EnumFacing side) {
        if(!isRealCable() || partContainer.hasPart(side)) return true;
        if(!forceDisconnected.containsKey(side)) return false;
        return forceDisconnected.get(side);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        // If the connection data were reset, update the cable connections
        if(connected.isEmpty()) {
            updateConnections();
        }

        partContainer.update();
    }

    protected void updateRedstoneInfo(EnumFacing side) {
        sendUpdate();
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlock());
        getWorld().notifyNeighborsOfStateChange(pos.offset(side.getOpposite()), getBlock());
    }

    public void setRedstoneLevel(EnumFacing side, int level) {
        if(!getWorld().isRemote) {
            boolean sendUpdate = false;
            if(redstoneLevels.containsKey(side)) {
                if(redstoneLevels.get(side) != level) {
                    sendUpdate = true;
                    redstoneLevels.put(side, level);
                }
            } else {
                sendUpdate = true;
                redstoneLevels.put(side, level);
            }
            if(sendUpdate) {
                updateRedstoneInfo(side);
            }
        }
    }

    public int getRedstoneLevel(EnumFacing side) {
        if(redstoneLevels.containsKey(side)) {
            return redstoneLevels.get(side);
        }
        return -1;
    }

    public void setAllowRedstoneInput(EnumFacing side, boolean allow) {
        redstoneInputs.put(side, allow);
    }

    public boolean isAllowRedstoneInput(EnumFacing side) {
        if(redstoneInputs.containsKey(side)) {
            return redstoneInputs.get(side);
        }
        return false;
    }

    public void disableRedstoneLevel(EnumFacing side) {
        if(!getWorld().isRemote) {
            redstoneLevels.remove(side);
            updateRedstoneInfo(side);
        }
    }

    protected void updateLightInfo(EnumFacing side) {
        sendUpdate();
    }

    public void setLightLevel(EnumFacing side, int level) {
        if(!getWorld().isRemote) {
            boolean sendUpdate = false;
            if(lightLevels.containsKey(side)) {
                if(lightLevels.get(side) != level) {
                    sendUpdate = true;
                    lightLevels.put(side, level);
                }
            } else {
                sendUpdate = true;
                lightLevels.put(side, level);
            }
            if(sendUpdate) {
                updateLightInfo(side);
            }
        }
    }

    public int getLightLevel(EnumFacing side) {
        if(lightLevels.containsKey(side)) {
            return lightLevels.get(side);
        }
        return 0;
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
            connected.put(side, cableConnected);

            // Remove any already existing force-disconnects for this side.
            if(!cableConnected) {
                forceDisconnected.put(side, false);
            }
        }
        markDirty();
        sendUpdate();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return connected.containsKey(side) && connected.get(side);
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
    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {

    }

    /**
     * @return The raw force disconnection data.
     */
    public EnumFacingMap<Boolean> getForceDisconnected() {
        return this.forceDisconnected;
    }

    public void setForceDisconnected(EnumFacingMap<Boolean> forceDisconnected) {
        this.forceDisconnected.clear();
        this.forceDisconnected.putAll(forceDisconnected);
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return partContainer.hasCapability(capability, facing)
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T t = partContainer.getCapability(capability, facing);
        if (t != null) {
            return t;
        }
        return super.getCapability(capability, facing);
    }
}
