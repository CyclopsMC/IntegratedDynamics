package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.cyclops.cyclopscore.block.property.ExtendedBlockStateBuilder;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableFakeableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableFakeableMultipartTicking;
import org.cyclops.integrateddynamics.capability.cable.CableTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderPartContainer;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerConfig;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTileMultipartTicking;
import org.cyclops.integrateddynamics.client.model.CableRenderState;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Map;
import java.util.Objects;

/**
 * A ticking part entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class TileMultipartTicking extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile,
        PartHelpers.IPartStateHolderCallback {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @Getter
    @NBTPersist private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    @NBTPersist private EnumFacingMap<Boolean> forceDisconnected = EnumFacingMap.newMap();
    @Getter
    @NBTPersist private EnumFacingMap<Integer> redstoneLevels = EnumFacingMap.newMap();
    @Getter
    @NBTPersist private EnumFacingMap<Boolean> redstoneInputs = EnumFacingMap.newMap();
    @Getter
    @NBTPersist private EnumFacingMap<Boolean> redstoneStrong = EnumFacingMap.newMap();
    @Getter
    @NBTPersist private EnumFacingMap<Integer> lightLevels = EnumFacingMap.newMap();
    private EnumFacingMap<Integer> previousLightLevels;
    @Getter
    @Setter
    @NBTPersist private String facadeBlockName = null;
    @Getter
    @Setter
    @NBTPersist private int facadeMeta = 0;

    @Getter
    private final PartContainerTileMultipartTicking partContainer;
    @Getter
    private final CableTileMultipartTicking cable;
    @Getter
    private final INetworkCarrier networkCarrier;
    @Getter
    private final ICableFakeable cableFakeable;

    private IExtendedBlockState cachedState = null;

    public TileMultipartTicking() {
        partContainer = new PartContainerTileMultipartTicking(this);
        addCapabilityInternal(PartContainerConfig.CAPABILITY, partContainer);
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderPartContainer(partContainer));
        addCapabilityInternal(FacadeableConfig.CAPABILITY, new FacadeableTileMultipartTicking(this));
        cable = new CableTileMultipartTicking(this);
        addCapabilityInternal(CableConfig.CAPABILITY, cable);
        networkCarrier = new NetworkCarrierDefault();
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, networkCarrier);
        cableFakeable = new CableFakeableMultipartTicking(this);
        addCapabilityInternal(CableFakeableConfig.CAPABILITY, cableFakeable);
        addCapabilityInternal(PathElementConfig.CAPABILITY, new PathElementTileMultipartTicking(this, cable));
        for (EnumFacing facing : EnumFacing.VALUES) {
            addCapabilitySided(DynamicLightConfig.CAPABILITY, facing, new DynamicLightTileMultipartTicking(this, facing));
            addCapabilitySided(DynamicRedstoneConfig.CAPABILITY, facing, new DynamicRedstoneTileMultipartTicking(this, facing));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("partContainer", partContainer.serializeNBT());
        tag.setBoolean("realCable", cableFakeable.isRealCable());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        EnumFacingMap<Boolean> lastConnected = EnumFacingMap.newMap(connected);
        String lastFacadeBlockName = facadeBlockName;
        int lastFacadeMeta = facadeMeta;
        boolean lastRealCable = cableFakeable.isRealCable();
        if (tag.hasKey("parts", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())
                && !tag.hasKey("partContainer", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            // Backwards compatibility with old part saving.
            // TODO: remove in next major MC update.
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, partContainer.getPartData(), getWorld());
        } else {
            partContainer.deserializeNBT(tag.getCompoundTag("partContainer"));
        }
        boolean wasLightTransparent = getWorld() != null && CableHelpers.isLightTransparent(getWorld(), getPos());

        super.readFromNBT(tag);
        cableFakeable.setRealCable(tag.getBoolean("realCable"));
        boolean isLightTransparent = getWorld() != null && CableHelpers.isLightTransparent(getWorld(), getPos());
        if (getWorld() != null && (lastConnected == null || connected == null || !lastConnected.equals(connected)
                || !Objects.equals(lastFacadeBlockName, facadeBlockName) || lastFacadeMeta != facadeMeta
                || lastRealCable != cableFakeable.isRealCable() || wasLightTransparent != isLightTransparent)) {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            getWorld().checkLight(getPos());
        }
    }

    @Override
    public void onUpdateReceived() {
        if(!lightLevels.equals(previousLightLevels)) {
            previousLightLevels = lightLevels;
            getWorld().checkLight(getPos());
        }
        cachedState = null;
    }

    public IExtendedBlockState getConnectionState() {
        if (cachedState != null) {
            return cachedState;
        }
        ExtendedBlockStateBuilder builder = ExtendedBlockStateBuilder.builder((IExtendedBlockState) getBlock().getDefaultState());
        if (partContainer.getPartData() != null) { // Can be null in rare cases where rendering happens before data sync
            builder.withProperty(BlockCable.REALCABLE, cableFakeable.isRealCable());
            if (connected.isEmpty()) {
                getCable().updateConnections();
            }
            for (EnumFacing side : EnumFacing.VALUES) {
                builder.withProperty(BlockCable.CONNECTED[side.ordinal()],
                        !cable.isForceDisconnected(side) && connected.get(side));
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()],
                        partContainer.hasPart(side) ? partContainer.getPart(side).getPartRenderPosition() : PartRenderPosition.NONE);
            }
            IFacadeable facadeable = getCapability(FacadeableConfig.CAPABILITY, null);
            builder.withProperty(BlockCable.FACADE, facadeable.hasFacade() ? Optional.of(facadeable.getFacade()) : Optional.absent());
            builder.withProperty(BlockCable.PARTCONTAINER, partContainer);
            builder.withProperty(BlockCable.RENDERSTATE, new CableRenderState(
                    this.cableFakeable.isRealCable(),
                    EnumFacingMap.newMap(this.connected),
                    EnumFacingMap.newMap(this.partContainer.getPartData()),
                    facadeBlockName,
                    facadeMeta
                    ));
        }
        return cachedState = builder.build();
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
        partContainer.update();
    }

    public void updateRedstoneInfo(EnumFacing side) {
        if (getWorld().isBlockLoaded(getPos().offset(side))) {
            getWorld().neighborChanged(getPos().offset(side), getBlockType(), getPos());
        }
    }

    public void updateLightInfo() {
        sendUpdate();
    }

    public INetwork getNetwork() {
        return networkCarrier.getNetwork();
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
        return super.hasCapability(capability, facing) || partContainer.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T value = super.getCapability(capability, facing);
        if (value != null) {
            return value;
        }
        return partContainer.getCapability(capability, facing);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        invalidateParts();
    }

    protected void invalidateParts() {
        if (getWorld() != null && !getWorld().isRemote) {
            INetwork network = getNetwork();
            if (network != null) {
                for (Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partContainer.getPartData().entrySet()) {
                    INetworkElement element = entry.getValue().getPart().createNetworkElement(getPartContainer(), DimPos.of(getWorld(), getPos()), entry.getKey());
                    element.invalidate(network);
                }
            }
        }
    }
}
