package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.RegistryEntries;
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
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    @NBTPersist private EnumFacingMap<Integer> lastRedstonePulses = EnumFacingMap.newMap();
    @Getter
    @NBTPersist private EnumFacingMap<Integer> lightLevels = EnumFacingMap.newMap();
    private EnumFacingMap<Integer> previousLightLevels;
    @Getter
    @Setter
    @NBTPersist private INBT facadeBlockTag = null;

    @Getter
    private final PartContainerTileMultipartTicking partContainer;
    @Getter
    private final CableTileMultipartTicking cable;
    @Getter
    private final INetworkCarrier networkCarrier;
    @Getter
    private final ICableFakeable cableFakeable;

    private IModelData cachedState = null;

    public TileMultipartTicking() {
        super(RegistryEntries.TILE_ENTITY_MULTIPART_TICKING);
        partContainer = new PartContainerTileMultipartTicking(this);
        addCapabilityInternal(PartContainerConfig.CAPABILITY, LazyOptional.of(() -> partContainer));
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderPartContainer(partContainer)));
        addCapabilityInternal(FacadeableConfig.CAPABILITY, LazyOptional.of(() -> new FacadeableTileMultipartTicking(this)));
        cable = new CableTileMultipartTicking(this);
        addCapabilityInternal(CableConfig.CAPABILITY, LazyOptional.of(() -> cable));
        networkCarrier = new NetworkCarrierDefault();
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, LazyOptional.of(() -> networkCarrier));
        cableFakeable = new CableFakeableMultipartTicking(this);
        addCapabilityInternal(CableFakeableConfig.CAPABILITY, LazyOptional.of(() -> cableFakeable));
        addCapabilityInternal(PathElementConfig.CAPABILITY, LazyOptional.of(() -> new PathElementTileMultipartTicking(this, cable)));
        for (Direction facing : Direction.values()) {
            addCapabilitySided(DynamicLightConfig.CAPABILITY, facing, LazyOptional.of(() -> new DynamicLightTileMultipartTicking(this, facing)));
            addCapabilitySided(DynamicRedstoneConfig.CAPABILITY, facing, LazyOptional.of(() -> new DynamicRedstoneTileMultipartTicking(this, facing)));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        tag.put("partContainer", partContainer.serializeNBT());
        tag.putBoolean("realCable", cableFakeable.isRealCable());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        EnumFacingMap<Boolean> lastConnected = EnumFacingMap.newMap(connected);
        INBT lastFacadeBlock = facadeBlockTag;
        boolean lastRealCable = cableFakeable.isRealCable();
        partContainer.deserializeNBT(tag.getCompound("partContainer"));
        boolean wasLightTransparent = getWorld() != null && CableHelpers.isLightTransparent(getWorld(), getPos(), null);

        super.read(tag);
        cableFakeable.setRealCable(tag.getBoolean("realCable"));
        boolean isLightTransparent = getWorld() != null && CableHelpers.isLightTransparent(getWorld(), getPos(), null);
        if (getWorld() != null && (lastConnected == null || connected == null || !lastConnected.equals(connected)
                || !Objects.equals(lastFacadeBlock, facadeBlockTag)
                || lastRealCable != cableFakeable.isRealCable() || wasLightTransparent != isLightTransparent)) {
            BlockHelpers.markForUpdate(getWorld(), getPos());
            // TODO: pre-1.14: getWorld().checkLight(getPos());
        }
    }

    @Override
    public void onUpdateReceived() {
        if(!lightLevels.equals(previousLightLevels)) {
            previousLightLevels = lightLevels;
            // TODO: pre-1.14: getWorld().checkLight(getPos());
        }
        cachedState = null;
    }

    public IModelData getConnectionState() {
        if (cachedState != null) {
            return cachedState;
        }
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        if (partContainer.getPartData() != null) { // Can be null in rare cases where rendering happens before data sync
            builder.withInitial(BlockCable.REALCABLE, cableFakeable.isRealCable());
            if (connected.isEmpty()) {
                getCable().updateConnections();
            }
            for (Direction side : Direction.values()) {
                builder.withInitial(BlockCable.CONNECTED[side.ordinal()],
                        !cable.isForceDisconnected(side) && connected.get(side));
                builder.withInitial(BlockCable.PART_RENDERPOSITIONS[side.ordinal()],
                        partContainer.hasPart(side) ? partContainer.getPart(side).getPartRenderPosition() : PartRenderPosition.NONE);
            }
            IFacadeable facadeable = getCapability(FacadeableConfig.CAPABILITY).orElse(null);
            builder.withInitial(BlockCable.FACADE, facadeable.hasFacade() ? Optional.of(facadeable.getFacade()) : Optional.empty());
            builder.withInitial(BlockCable.PARTCONTAINER, partContainer);
            builder.withInitial(BlockCable.RENDERSTATE, new CableRenderState(
                    this.cableFakeable.isRealCable(),
                    EnumFacingMap.newMap(this.connected),
                    EnumFacingMap.newMap(this.partContainer.getPartData()),
                    facadeBlockTag
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

        // Revalidate network if that hasn't happened yet
        if (getNetwork() == null && getWorld() != null && !getWorld().isRemote) {
            NetworkHelpers.revalidateNetworkElements(getWorld(), getPos());
        }
    }

    public void updateRedstoneInfo(Direction side, boolean strongPower) {
        this.markDirty();
        if (getWorld().isBlockLoaded(getPos().offset(side))) {
            getWorld().neighborChanged(getPos().offset(side), getBlockState().getBlock(), getPos());
            if (strongPower) {
                // When we are emitting a strong power, also update all neighbours of the target
                getWorld().notifyNeighborsOfStateChange(getPos().offset(side), getBlockState().getBlock());
            }
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

    // TODO: remove in 1.14? Used to be related to MinecraftForgeClient#getRenderPass()
    /*@Override
    public boolean shouldRenderInPass(int pass) {
        return true;
    }*/

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        LazyOptional<T> value = super.getCapability(capability, facing);
        if (value.isPresent()) {
            return value;
        }
        return partContainer.getCapability(capability, facing);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        invalidateParts();
    }

    protected void invalidateParts() {
        if (getWorld() != null && !getWorld().isRemote) {
            INetwork network = getNetwork();
            if (network != null) {
                for (Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : partContainer.getPartData().entrySet()) {
                    INetworkElement element = entry.getValue().getPart().createNetworkElement(getPartContainer(), DimPos.of(getWorld(), getPos()), entry.getKey());
                    element.invalidate(network);
                }
            }
        }
    }

    @Override
    protected Direction transformFacingForRotation(Direction facing) {
        // Ignore rotations on this tile
        return facing;
    }
}
