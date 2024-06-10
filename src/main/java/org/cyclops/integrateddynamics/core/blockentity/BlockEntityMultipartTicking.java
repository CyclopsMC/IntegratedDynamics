package org.cyclops.integrateddynamics.core.blockentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.capability.cable.CableFakeableMultipartTicking;
import org.cyclops.integrateddynamics.capability.cable.CableTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableDefault;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderPartContainer;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerTileMultipartTicking;
import org.cyclops.integrateddynamics.capability.path.PathElementTileMultipartTicking;
import org.cyclops.integrateddynamics.client.model.CableRenderState;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.event.RegisterPartCapabilitiesEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A ticking part entity which is made up of different parts.
 * @author Ruben Taelman
 */
public class BlockEntityMultipartTicking extends CyclopsBlockEntity implements PartHelpers.IPartStateHolderCallback {

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
    @NBTPersist private CompoundTag facadeBlockTag = null;

    @Getter
    private final PartContainerTileMultipartTicking partContainer;
    @Getter
    private final CableTileMultipartTicking cable;
    @Getter
    private final INetworkCarrier networkCarrier;
    @Getter
    private final ICableFakeable cableFakeable;
    @NBTPersist
    @Setter
    private boolean forceLightCheckAtClient;

    private ModelData cachedState = null;

    public BlockEntityMultipartTicking(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_MULTIPART_TICKING.get(), blockPos, blockState);
        partContainer = new PartContainerTileMultipartTicking(this);
        cable = new CableTileMultipartTicking(this);
        networkCarrier = new NetworkCarrierDefault();
        cableFakeable = new CableFakeableMultipartTicking(this);
    }

    public static void registerMultipartTickingCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityMultipartTicking> blockEntityType) {
        event.registerBlockEntity(
                Capabilities.PartContainer.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getPartContainer()
        );
        event.registerBlockEntity(
                Capabilities.NetworkElementProvider.BLOCK,
                blockEntityType,
                (blockEntity, context) -> new NetworkElementProviderPartContainer(blockEntity.getPartContainer())
        );
        event.registerBlockEntity(
                Capabilities.Facadeable.BLOCK,
                blockEntityType,
                (blockEntity, context) -> new FacadeableTileMultipartTicking(blockEntity)
        );
        event.registerBlockEntity(
                Capabilities.Cable.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getCable()
        );
        event.registerBlockEntity(
                Capabilities.NetworkCarrier.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getNetworkCarrier()
        );
        event.registerBlockEntity(
                Capabilities.CableFakeable.BLOCK,
                blockEntityType,
                (blockEntity, context) -> blockEntity.getCableFakeable()
        );
        event.registerBlockEntity(
                Capabilities.PathElement.BLOCK,
                blockEntityType,
                (blockEntity, context) -> new PathElementTileMultipartTicking(blockEntity, blockEntity.getCable())
        );
        registerPartCapabilityAsBlockCapability(event, blockEntityType, Capabilities.ValueInterface.BLOCK, Capabilities.ValueInterface.PART);
        registerPartCapabilityAsBlockCapability(event, blockEntityType, Capabilities.VariableContainer.BLOCK, Capabilities.VariableContainer.PART);
        ModLoader.get().postEventWrapContainerInModOrder(new RegisterPartCapabilitiesEvent(event, blockEntityType));

        for (Direction facing : Direction.values()) {
            event.registerBlockEntity(
                    Capabilities.DynamicLight.BLOCK,
                    blockEntityType,
                    (blockEntity, context) -> new DynamicLightTileMultipartTicking(blockEntity, facing)
            );
            event.registerBlockEntity(
                    Capabilities.DynamicRedstone.BLOCK,
                    blockEntityType,
                    (blockEntity, context) -> new DynamicRedstoneTileMultipartTicking(blockEntity, facing)
            );
        }
    }

    public static <T> void registerPartCapabilityAsBlockCapability(RegisterCapabilitiesEvent event, BlockEntityType<? extends BlockEntityMultipartTicking> blockEntityType, BlockCapability<T, Direction> blockCapability, PartCapability<T> partCapability) {
        event.registerBlockEntity(
                blockCapability,
                blockEntityType,
                (blockEntity, context) -> {
                    INetwork network = blockEntity.getNetwork();
                    IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
                    return blockEntity.getPartContainer()
                            .getCapability(partCapability, network, partNetwork, PartTarget.fromCenter(PartPos.of(blockEntity.getLevel(), blockEntity.getBlockPos(), context)))
                            .orElse(null);
                }
        );
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        forceLightCheckAtClient = false;
        tag.put("partContainer", partContainer.serializeNBT());
        tag.putBoolean("realCable", cableFakeable.isRealCable());
    }

    @Override
    public void read(CompoundTag tag) {
        EnumFacingMap<Boolean> lastConnected = EnumFacingMap.newMap(connected);
        Tag lastFacadeBlock = facadeBlockTag;
        boolean lastRealCable = cableFakeable.isRealCable();
        partContainer.deserializeNBT(tag.getCompound("partContainer"));
        boolean wasLightTransparent = getLevel() != null && CableHelpers.isLightTransparent(getLevel(), getBlockPos(), null);

        super.read(tag);
        cableFakeable.setRealCable(tag.getBoolean("realCable"));
        boolean isLightTransparent = getLevel() != null && CableHelpers.isLightTransparent(getLevel(), getBlockPos(), null);
        if (getLevel() != null && (lastConnected == null || connected == null || !lastConnected.equals(connected)
                || !Objects.equals(lastFacadeBlock, facadeBlockTag)
                || lastRealCable != cableFakeable.isRealCable() || wasLightTransparent != isLightTransparent)) {
            BlockHelpers.markForUpdate(getLevel(), getBlockPos());
        }
    }

    @Override
    public void onUpdateReceived() {
        if(!lightLevels.equals(previousLightLevels)) {
            previousLightLevels = lightLevels;
        }
        cachedState = null;
        BlockHelpers.markForUpdate(getLevel(), getBlockPos());
        if (forceLightCheckAtClient) {
            getLevel().getLightEngine().checkBlock(getBlockPos());
        }
    }

    public ModelData getConnectionState() {
        if (cachedState != null) {
            return cachedState;
        }
        ModelData.Builder builder = ModelData.builder();
        if (partContainer.getPartData() != null) { // Can be null in rare cases where rendering happens before data sync
            builder.with(BlockCable.REALCABLE, cableFakeable.isRealCable());
            if (connected.isEmpty()) {
                getCable().updateConnections();
            }
            for (Direction side : Direction.values()) {
                builder.with(BlockCable.CONNECTED[side.ordinal()],
                        !cable.isForceDisconnected(side) && connected.get(side));
                builder.with(BlockCable.PART_RENDERPOSITIONS[side.ordinal()],
                        partContainer.hasPart(side) ? partContainer.getPart(side).getPartRenderPosition() : PartRenderPosition.NONE);
            }
            IFacadeable facadeable = Optional.ofNullable(level.getCapability(Capabilities.Facadeable.BLOCK, getBlockPos(), getBlockState(), this, null))
                    .orElseGet(FacadeableDefault::new);
            builder.with(BlockCable.FACADE, facadeable.hasFacade() ? Optional.of(facadeable.getFacade()) : Optional.empty());
            builder.with(BlockCable.PARTCONTAINER, partContainer);
            builder.with(BlockCable.RENDERSTATE, new CableRenderState(
                    this.cableFakeable.isRealCable(),
                    EnumFacingMap.newMap(this.connected),
                    EnumFacingMap.newMap(this.partContainer.getPartData()),
                    facadeBlockTag
                    ));
        }
        return cachedState = builder.build();
    }

    public void updateRedstoneInfo(Direction side, boolean strongPower) {
        this.setChanged();
        if (getLevel().isLoaded(getBlockPos().relative(side))) {
            getLevel().neighborChanged(getBlockPos().relative(side), getBlockState().getBlock(), getBlockPos());
            if (strongPower) {
                // When we are emitting a strong power, also update all neighbours of the target
                getLevel().updateNeighborsAt(getBlockPos().relative(side), getBlockState().getBlock());
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
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        invalidateParts();
    }

    protected void invalidateParts() {
        if (getLevel() != null && !getLevel().isClientSide) {
            INetwork network = getNetwork();
            if (network != null) {
                for (Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : partContainer.getPartData().entrySet()) {
                    INetworkElement element = entry.getValue().getPart().createNetworkElement(getPartContainer(), DimPos.of(getLevel(), getBlockPos()), entry.getKey());
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

    public static class Ticker<T extends BlockEntityMultipartTicking> extends BlockEntityTickerDelayed<T> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, T blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (blockEntity.getConnected().isEmpty()) {
                blockEntity.getCable().updateConnections();
            }
            blockEntity.getPartContainer().update();

            // Revalidate network if that hasn't happened yet
            if (blockEntity.getNetwork() == null) {
                NetworkHelpers.revalidateNetworkElements(level, pos);
            }
        }
    }
}
