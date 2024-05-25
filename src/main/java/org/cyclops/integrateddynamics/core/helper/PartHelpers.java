package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.network.event.UnknownPartEvent;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Helpers related to parts.
 * @author rubensworks
 */
public class PartHelpers {

    /**
     * Get the part container capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional part container capability.
     */
    public static Optional<IPartContainer> getPartContainer(Level world, BlockPos pos, @Nullable Direction side) {
        return BlockEntityHelpers.getCapability(world, pos, side, Capabilities.PartContainer.BLOCK);
    }

    /**
     * Get the part container capability at the given position.
     * @param dimPos The dimensional position.
     * @param side The side.
     * @return The optional part container capability.
     */
    public static Optional<IPartContainer> getPartContainer(DimPos dimPos, @Nullable Direction side) {
        return BlockEntityHelpers.getCapability(dimPos, side, Capabilities.PartContainer.BLOCK);
    }

    /**
     * Get the part container capability at the given position.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a part container present.
     *
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The part container capability.
     */
    public static IPartContainer getPartContainerChecked(Level world, BlockPos pos, @Nullable Direction side) {
        return getPartContainer(world, pos, side)
                .orElseThrow(() -> new PartStateException(DimPos.of(world, pos), side));
    }

    /**
     * Get the part container capability at the given position.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a part container present.
     *
     * @param dimPos The dimensional position.
     * @param side The side.
     * @return The part container capability.
     */
    public static IPartContainer getPartContainerChecked(DimPos dimPos, @Nullable Direction side) {
       return PartHelpers.getPartContainer(dimPos, side)
               .orElseThrow(() -> new PartStateException(dimPos, side));
    }

    /**
     * Get the part container capability at the given position.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a part container present.
     *
     * @param pos The part position.
     * @return The part container capability.
     */
    public static IPartContainer getPartContainerChecked(PartPos pos) {
        return PartHelpers.getPartContainerChecked(pos.getPos(), pos.getSide());
    }

    /**
     * Check if the given part type is null and run it through the network even bus in an {@link UnknownPartEvent}
     * to get another type.
     * @param network The network.
     * @param partTypeName The part name.
     * @param partType The part type.
     * @return A possibly non-null part type.
     */
    public static IPartType validatePartType(INetwork network, String partTypeName, @Nullable IPartType partType) {
        if(network != null && partType == null) {
            UnknownPartEvent event = new UnknownPartEvent(network, partTypeName);
            network.getEventBus().post(event);
            partType = event.getPartType();
        }
        return partType;
    }

    /**
     * Write the given part type to nbt.
     * @param partTag The tag to write to.
     * @param side The side to write.
     * @param partType The part type to write.
     */
    public static void writePartTypeToNBT(CompoundTag partTag, Direction side, IPartType partType) {
        partTag.putString("__partType", partType.getUniqueName().toString());
        partTag.putString("__side", side.getSerializedName());
    }

    /**
     * Write the given part data to nbt.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to write to.
     * @param partData The part data.
     * @return If the writing succeeded.
     */
    public static boolean writePartToNBT(BlockPos pos, CompoundTag partTag, Pair<Direction, PartStateHolder<?, ?>> partData) {
        IPartType part = partData.getValue().getPart();
        IPartState partState = partData.getValue().getState();
        writePartTypeToNBT(partTag, partData.getKey(), part);
        try {
            part.toNBT(partTag, partState);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.ERROR,  String.format("The part %s at position %s was errored " +
                    "and is removed.", part.getUniqueName(), pos));
            return false;
        }
    }

    /**
     * Write the given parts to nbt.
     * @param pos The position of the part, used for error reporting.
     * @param tag The tag to write to.
     * @param partData The part data.
     */
    public static void writePartsToNBT(BlockPos pos, CompoundTag tag, Map<Direction, PartStateHolder<?, ?>> partData) {
        ListTag partList = new ListTag();
        for(Map.Entry<Direction, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
            CompoundTag partTag = new CompoundTag();
            if(writePartToNBT(pos, partTag, Pair.<Direction, PartStateHolder<?, ?>>of(entry.getKey(), entry.getValue()))) {
                partList.add(partTag);
            }
        }
        tag.put("parts", partList);
    }

    /**
     * Read a part from nbt.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to read from.
     * @return The part data.
     */
    public static Pair<Direction, IPartType> readPartTypeFromNBT(@Nullable INetwork network, BlockPos pos, CompoundTag partTag) {
        String partTypeName = partTag.getString("__partType");
        IPartType partType = validatePartType(network, partTypeName, PartTypes.REGISTRY.getPartType(new ResourceLocation(partTypeName)));
        if(partType != null) {
            Direction side = Direction.byName(partTag.getString("__side"));
            if (side != null) {
                return Pair.of(side, partType);
            } else {
                IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("The part %s at position %s was at an invalid " +
                                "side and removed.",
                        partType.getUniqueName(), pos));
            }
        } else {
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("The part %s at position %s was unknown and removed.",
                    partTypeName, pos));
        }
        return null;
    }

    /**
     * Read part data from nbt.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to read from.
     * @param level The world.
     * @return The part data.
     */
    public static Pair<Direction, ? extends PartStateHolder<?, ?>> readPartFromNBT(@Nullable INetwork network, BlockPos pos, CompoundTag partTag, Level level) {
        Pair<Direction, IPartType> partData = readPartTypeFromNBT(network, pos, partTag);
        if(partData != null) {
            IPartState partState = partData.getValue().fromNBT(ValueDeseralizationContext.of(level), partTag);
            return Pair.of(partData.getKey(), PartStateHolder.of(partData.getValue(), partState));
        }
        return null;
    }

    /**
     * Read parts data from nbt.
     * If the world is not null and we are running client-side,
     * a block render update will automatically be triggered if needed.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param tag The tag to read from.
     * @param partData The map of part data to write to.
     * @param world The world.
     */
    public static void readPartsFromNBT(@Nullable INetwork network, BlockPos pos, CompoundTag tag,
                                        Map<Direction, PartStateHolder<?, ?>> partData, Level world) {
        Map<Direction, PartStateHolder<?, ?>> oldPartData = ImmutableMap.copyOf(partData);
        partData.clear();
        ListTag partList = tag.getList("parts", Tag.TAG_COMPOUND);
        for(int i = 0; i < partList.size(); i++) {
            CompoundTag partTag = partList.getCompound(i);
            Pair<Direction, ? extends PartStateHolder<?, ?>> part = readPartFromNBT(network, pos, partTag, world);
            if(part != null) {
                partData.put(part.getKey(), part.getValue());
            }
        }

        // Trigger block render update if at least one of the parts requires it.
        if (world != null && world.isClientSide()) {
            boolean triggerBlockRenderUpdate = false;
            for (Direction side : Direction.values()) {
                PartStateHolder<?, ?> oldData = oldPartData.get(side);
                PartStateHolder<?, ?> newData = partData.get(side);
                if (oldData != null || newData != null) {
                    IPartType oldPartType = oldData != null ? oldData.getPart() : null;
                    IPartType newPartType = newData != null ? newData.getPart() : null;
                    IPartState oldPartState = oldData != null ? oldData.getState() : null;
                    IPartState newPartState = newData != null ? newData.getState() : null;

                    if (oldPartType != newPartType
                            || oldPartType.shouldTriggerBlockRenderUpdate(oldPartState, newPartState)) {
                        triggerBlockRenderUpdate = true;
                        break;
                    }
                }
            }
            if (triggerBlockRenderUpdate) {
                BlockHelpers.markForUpdate(world, pos);
            }
        }
    }

    /**
     * Remove a part from the given side of the given part container.
     * @param world The world.
     * @param pos The position of the container.
     * @param side The side.
     * @param player The player that is removing the part or null.
     * @param destroyIfEmpty If the cable block must be removed if no other parts are present after this removal.
     * @param dropMainElement If the main part element should be dropped.
     * @param saveState If the element state should be saved in the item.
     * @return If the block was set to air (removed).
     */
    public static boolean removePart(Level world, BlockPos pos, Direction side, @Nullable Player player,
                                     boolean destroyIfEmpty, boolean dropMainElement, boolean saveState) {
        IPartContainer partContainer = getPartContainerChecked(world, pos, side);
        ICableFakeable cableFakeable = CableHelpers.getCableFakeable(world, pos, side).orElse(null);
        partContainer.removePart(side, player, dropMainElement, saveState);

        // Remove full cable block if this was the last part and if it was already an unreal cable.
        boolean removeCompletely = destroyIfEmpty && (cableFakeable == null || !cableFakeable.isRealCable()) && !partContainer.hasParts();
        if(removeCompletely) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
            // If there is a cable in the direction of the removed part, try connecting with it.
            if (CableHelpers.getCable(world, pos.relative(side), side.getOpposite()).isPresent()) {
                CableHelpers.updateConnections(world, pos, side);
                CableHelpers.updateConnections(world, pos.relative(side), side.getOpposite());
                NetworkHelpers.initNetwork(world, pos, side);
            }
        }

        return !removeCompletely;
    }

    /**
     * Add a part to the given side with the part state in the given item.
     * @param level The world.
     * @param pos The position of the container.
     * @param side The side.
     * @param partType The part type.
     * @param itemStack The item holding the part state.
     * @return If the part was added.
     */
    public static boolean addPart(Level level, BlockPos pos, Direction side, IPartType partType, ItemStack itemStack) {
        IPartContainer partContainer = getPartContainerChecked(level, pos, side);
        if(partContainer.canAddPart(side, partType)) {
            if(!level.isClientSide()) {
                partContainer.setPart(side, partType, partType.getState(ValueDeseralizationContext.of(level), itemStack));
            }
            return true;
        }
        return false;
    }

    /**
     * Add a part to the given side with the part state.
     * @param world The world.
     * @param pos The position of the container.
     * @param side The side.
     * @param partType The part type.
     * @param partState The part state.
     * @return If the part was added.
     */
    public static boolean addPart(Level world, BlockPos pos, Direction side, IPartType partType, IPartState partState) {
        IPartContainer partContainer = getPartContainerChecked(world, pos, side);
        if(partContainer.canAddPart(side, partType)) {
            if(!world.isClientSide()) {
                partContainer.setPart(side, partType, partState);
            }
            return true;
        }
        return false;
    }

    /**
     * Forcefully set a part at the given side.
     * @param network The network.
     * @param world The world.
     * @param pos The position of the container.
     * @param side The side.
     * @param part The part to set.
     * @param partState The part state to set.
     * @param callback The callback for the part state holder.
     * @return If the part could be placed.
     */
    public static boolean setPart(@Nullable INetwork network, Level world, BlockPos pos, Direction side, IPartType part, IPartState partState, IPartStateHolderCallback callback) {
        callback.onSet(PartStateHolder.of(part, partState));
        if(network != null) {
            IPartContainer partContainer = PartHelpers.getPartContainerChecked(world, pos, side);
            INetworkElement networkElement = part.createNetworkElement(partContainer, DimPos.of(world, pos), side);
            if(!network.addNetworkElement(networkElement, false)) {
                // In this case, the addition failed because that part id is already present in the network,
                // therefore we have to make a new state for that part (with a new id) and retry.
                partState = part.defaultBlockState();
                callback.onSet(PartStateHolder.of(part, partState));
                IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, "A part already existed in the network, this is possibly a " +
                        "result from item duplication.");
                network.addNetworkElement(networkElement, false);
            }
            return true;
        }
        return false;
    }

    /**
     * If the given player can currently interact with the part gui at the given position.
     * @param target The part target.
     * @param player The player.
     * @param expectedPartContainer The expected part container.
     * @return If the player can interact with it.
     */
    public static boolean canInteractWith(PartTarget target, Player player, IPartContainer expectedPartContainer) {
        IPartContainer partContainer = PartHelpers.getPartContainer(target.getCenter().getPos(), target.getCenter().getSide()).orElse(null);
        return partContainer == expectedPartContainer;
    }

    /**
     * Get a part at the given position.
     * @param partPos The part position.
     * @return The part.
     */
    public static @Nullable PartStateHolder<?, ?> getPart(PartPos partPos) {
        Direction side = partPos.getSide();
        IPartContainer partContainer = PartHelpers.getPartContainer(partPos.getPos(), partPos.getSide()).orElse(null);
        if (partContainer != null && partContainer.hasPart(side)) {
            return PartStateHolder.of(partContainer.getPart(side), partContainer.getPartState(side));
        }
        return null;
    }

    /**
     * Open a part gui container from the server.
     * @param player The player opening the gui.
     * @param pos The part position.
     * @param partType The part type.
     * @return The action result.
     */
    public static InteractionResult openContainerPart(ServerPlayer player, PartPos pos, IPartType<?, ?> partType) {
        return partType.getContainerProvider(pos)
                .map(containerProvider -> {
                    player.openMenu(containerProvider, packetBuffer -> partType.writeExtraGuiData(packetBuffer, pos, player));
                    return InteractionResult.SUCCESS;
                })
                .orElse(InteractionResult.PASS);
    }

    /**
     * Open a part settings gui container from the server.
     * @param player The player opening the gui.
     * @param pos The part position.
     * @param partType The part type.
     * @return If the part has a container provider for settings.
     */
    public static boolean openContainerPartSettings(ServerPlayer player, PartPos pos, IPartType<?, ?> partType) {
        return partType.getContainerProviderSettings(pos)
                .map(containerProvider -> {
                    player.openMenu(containerProvider, packetBuffer -> partType.writeExtraGuiDataSettings(packetBuffer, pos, player));
                    return true;
                })
                .orElse(false);
    }

    /**
     * Open an aspect settings gui container from the server.
     * @param player The player opening the gui.
     * @param pos The part position.
     * @param aspect The aspect for which to show the settings.
     */
    public static void openContainerAspectSettings(ServerPlayer player, PartPos pos, IAspect<?, ?> aspect) {
        player.openMenu(aspect.getPropertiesContainerProvider(pos),
                packetBuffer -> packetBuffer.writeUtf(aspect.getUniqueName().toString()));
    }

    /**
     * Open a part offset gui container from the server.
     * @param player The player opening the gui.
     * @param pos The part position.
     * @param partType The part type.
     * @return If the part has a container provider for offsets.
     */
    public static boolean openContainerPartOffsets(ServerPlayer player, PartPos pos, IPartType<?, ?> partType) {
        return partType.getContainerProviderOffsets(pos)
                .map(containerProvider -> {
                    player.openMenu(containerProvider, packetBuffer -> partType.writeExtraGuiDataOffsets(packetBuffer, pos, player));
                    return true;
                })
                .orElse(false);
    }

    /**
     * Construct a data holder for constructing a part-related container.
     * @param pos A part position.
     * @return A data holder.
     */
    public static Triple<IPartContainer, PartTypeBase, PartTarget> getContainerPartConstructionData(PartPos pos) {
        IPartContainer partContainer = PartHelpers.getPartContainer(pos.getPos(), pos.getSide()).orElse(null);
        if(partContainer == null) {
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("The tile at %s is not a valid part container.", pos));
            return null;
        }
        IPartType partType = partContainer.getPart(pos.getSide());
        if(partType == null || !(partType instanceof PartTypeBase)) {
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("The part container at %s side %s does not " +
                    "have a valid part.", pos, pos.getSide()));
            return null;
        }
        PartTarget target = partType.getTarget(pos, partContainer.getPartState(pos.getSide()));
        return Triple.of(partContainer, (PartTypeBase) partType, target);
    }

    /**
     * Read a part target from a packet buffer.
     * @param packetBuffer A packet buffer.
     * @return A part target.
     */
    public static PartTarget readPartTarget(FriendlyByteBuf packetBuffer) {
        return PartTarget.fromCenter(PacketCodec.read(packetBuffer, PartPos.class));
    }

    /**
     * Read a part from a packet buffer.
     * @param packetBuffer A packet buffer.
     * @return A part.
     * @param <P> The part type type.
     * @param <S> The part state type.
     */
    public static <P extends IPartType<P, S>, S extends IPartState<P>> P readPart(FriendlyByteBuf packetBuffer) {
        String name = packetBuffer.readUtf();
        return (P) Objects.requireNonNull(PartTypeRegistry.getInstance().getPartType(new ResourceLocation(name)),
                String.format("Could not find a part by name %s", name));
    }

    /**
     * A part and state holder.
     * @param <P> The part type type.
     * @param <S> The part state type.
     */
    @Data
    public static class PartStateHolder<P extends IPartType<P, S>, S extends IPartState<P>> {

        private final IPartType<P, S> part;
        private final S state;

        public static PartStateHolder<?, ?> of(IPartType part, IPartState partState) {
            return new PartStateHolder(part, partState);
        }

    }

    /**
     * A callback for setting part state holders.
     */
    public static interface IPartStateHolderCallback {

        public void onSet(PartStateHolder<?, ?> partStateHolder);

    }

}
