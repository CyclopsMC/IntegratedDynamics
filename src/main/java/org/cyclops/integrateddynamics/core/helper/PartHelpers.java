package org.cyclops.integrateddynamics.core.helper;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.network.event.UnknownPartEvent;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Helpers related to parts.
 * @author rubensworks
 */
public class PartHelpers {

    /**
     * Check if the given part type is null and run it through the network even bus in an {@link UnknownPartEvent}
     * to get another type.
     * @param network The network.
     * @param partTypeName The part name.
     * @param partType The part type.
     * @return A possibly non-null part type.
     */
    public static IPartType validatePartType(IPartNetwork network, String partTypeName, @Nullable IPartType partType) {
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
    public static void writePartTypeToNBT(NBTTagCompound partTag, EnumFacing side, IPartType partType) {
        partTag.setString("__partType", partType.getName());
        partTag.setString("__side", side.getName());
    }

    /**
     * Write the given part data to nbt.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to write to.
     * @param partData The part data.
     * @return If the writing succeeded.
     */
    public static boolean writePartToNBT(BlockPos pos, NBTTagCompound partTag, Pair<EnumFacing, PartStateHolder<?, ?>> partData) {
        IPartType part = partData.getValue().getPart();
        IPartState partState = partData.getValue().getState();
        writePartTypeToNBT(partTag, partData.getKey(), part);
        try {
            part.toNBT(partTag, partState);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            IntegratedDynamics.clog(Level.ERROR,  String.format("The part %s at position %s was errored " +
                    "and is removed.", part.getName(), pos));
            return false;
        }
    }

    /**
     * Write the given parts to nbt.
     * @param pos The position of the part, used for error reporting.
     * @param tag The tag to write to.
     * @param partData The part data.
     */
    public static void writePartsToNBT(BlockPos pos, NBTTagCompound tag, Map<EnumFacing, PartStateHolder<?, ?>> partData) {
        NBTTagList partList = new NBTTagList();
        for(Map.Entry<EnumFacing, PartHelpers.PartStateHolder<?, ?>> entry : partData.entrySet()) {
            NBTTagCompound partTag = new NBTTagCompound();
            if(writePartToNBT(pos, partTag, Pair.<EnumFacing, PartStateHolder<?, ?>>of(entry.getKey(), entry.getValue()))) {
                partList.appendTag(partTag);
            }
        }
        tag.setTag("parts", partList);
    }

    /**
     * Read a part from nbt.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to read from.
     * @return The part data.
     */
    public static Pair<EnumFacing, IPartType> readPartTypeFromNBT(@Nullable IPartNetwork network, BlockPos pos, NBTTagCompound partTag) {
        String partTypeName = partTag.getString("__partType");
        IPartType partType = validatePartType(network, partTypeName, PartTypes.REGISTRY.getPartType(partTypeName));
        if(partType != null) {
            EnumFacing side = EnumFacing.byName(partTag.getString("__side"));
            if (side != null) {
                return Pair.of(side, partType);
            } else {
                IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was at an invalid " +
                                "side and removed.",
                        partType.getName(), pos));
            }
        } else {
            IntegratedDynamics.clog(Level.WARN, String.format("The part %s at position %s was unknown and removed.",
                    partTypeName, pos));
        }
        return null;
    }

    /**
     * Read part data from nbt.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param partTag The tag to read from.
     * @return The part data.
     */
    public static Pair<EnumFacing, ? extends PartStateHolder<?, ?>> readPartFromNBT(@Nullable IPartNetwork network, BlockPos pos, NBTTagCompound partTag) {
        Pair<EnumFacing, IPartType> partData = readPartTypeFromNBT(network, pos, partTag);
        if(partData != null) {
            IPartState partState = partData.getValue().fromNBT(partTag);
            return Pair.of(partData.getKey(), PartStateHolder.of(partData.getValue(), partState));
        }
        return null;
    }

    /**
     * Read parts data from nbt.
     * @param network The network the part will be part of.
     * @param pos The position of the part, used for error reporting.
     * @param tag The tag to read from.
     * @param partData The map of part data to write to.
     */
    public static void readPartsFromNBT(@Nullable IPartNetwork network, BlockPos pos, NBTTagCompound tag, Map<EnumFacing, PartStateHolder<?, ?>> partData) {
        partData.clear();
        NBTTagList partList = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for(int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound partTag = partList.getCompoundTagAt(i);
            Pair<EnumFacing, ? extends PartStateHolder<?, ?>> part = readPartFromNBT(network, pos, partTag);
            if(part != null) {
                partData.put(part.getKey(), part.getValue());
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
     */
    public static void removePart(World world, BlockPos pos, EnumFacing side, @Nullable EntityPlayer player, boolean destroyIfEmpty) {
        IPartContainerFacade partContainerFacade = CableHelpers.getInterface(world, pos, IPartContainerFacade.class);
        ICable cable = CableHelpers.getInterface(world, pos, ICable.class);
        IPartContainer partContainer = partContainerFacade.getPartContainer(world, pos);
        partContainer.removePart(side, player);
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
        // Remove full cable block if this was the last part and if it was already an unreal cable.
        if(destroyIfEmpty && (!(cable instanceof ICableFakeable) || !((ICableFakeable) cable).isRealCable(world, pos)) && !partContainer.hasParts()) {
            //world.destroyBlock(pos, player == null || !player.capabilities.isCreativeMode); // We don't call this directly because we don't want breaking sounds to play
            if(player == null || !player.capabilities.isCreativeMode) {
                ((Block) cable).dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            }
            world.setBlockState(pos, Blocks.air.getDefaultState(), 3);
        }
    }

    /**
     * Set a part at the given side.
     * @param network The network.
     * @param world The world.
     * @param pos The position of the container.
     * @param side The side.
     * @param part The part to set.
     * @param partState The part state to set.
     * @param callback The callback for the part state holder.
     * @return If the part could be placed.
     */
    public static boolean setPart(@Nullable IPartNetwork network, World world, BlockPos pos, EnumFacing side, IPartType part, IPartState partState, IPartStateHolderCallback callback) {
        callback.onSet(PartStateHolder.of(part, partState));
        if(network != null) {
            IPartContainerFacade partContainerFacade = CableHelpers.getInterface(world, pos, IPartContainerFacade.class);
            INetworkElement networkElement = part.createNetworkElement(partContainerFacade, DimPos.of(world, pos), side);
            if(!network.addNetworkElement(networkElement, false)) {
                // In this case, the addition failed because that part id is already present in the network,
                // therefore we have to make a new state for that part (with a new id) and retry.
                partState = part.getDefaultState();
                callback.onSet(PartStateHolder.of(part, partState));
                IntegratedDynamics.clog(Level.WARN, "A part already existed in the network, this is possibly a " +
                        "result from item duplication.");
                network.addNetworkElement(networkElement, false);
            }
            return true;
        }
        return false;
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
