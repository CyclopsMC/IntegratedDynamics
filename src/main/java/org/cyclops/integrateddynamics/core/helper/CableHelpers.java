package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableFakeableConfig;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Helpers related to cables.
 * @author rubensworks
 */
public class CableHelpers {

    public static final Collection<Direction> ALL_SIDES = Sets.newIdentityHashSet();
    static {
        for (Direction side : Direction.values()) {
            ALL_SIDES.add(side);
        }
    }

    /**
     * Get the cable capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional cable capability.
     */
    public static LazyOptional<ICable> getCable(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, CableConfig.CAPABILITY);
    }

    /**
     * Get the fakeable cable capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional fakeable cable capability.
     */
    public static LazyOptional<ICableFakeable> getCableFakeable(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, CableFakeableConfig.CAPABILITY);
    }

    /**
     * Get the path element capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional path element capability.
     */
    public static LazyOptional<IPathElement> getPathElement(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, PathElementConfig.CAPABILITY);
    }

    /**
     * Request to update the cable connections of all neighbours of the given position.
     * @param world The world.
     * @param pos The center position.
     * @param sides The sides to update connections for.
     */
    public static void updateConnectionsNeighbours(IBlockReader world, BlockPos pos, Collection<Direction> sides) {
        for(Direction side : sides) {
            updateConnections(world, pos.offset(side), side.getOpposite());
        }
    }

    /**
     * Request to update the cable connections at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     */
    public static void updateConnections(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        getCable(world, pos, side)
                .ifPresent(ICable::updateConnections);
    }

    /**
     * Check if there is a cable at the given position AND if it is connected for the given side.
     * @param world The world.
     * @param pos The position.
     * @param side The side to check a connection for.
     * @return If there is a cable that is connected.
     */
    public static boolean isCableConnected(IBlockReader world, BlockPos pos, Direction side) {
        return getCable(world, pos, side)
                .map(cable -> cable.isConnected(side))
                .orElse(false);
    }

    /**
     * Check if one side of the given cable at the given position can connect to the given side.
     * To be used when the cable connections are being updated.
     * This will check if the origin cable can connect to that side,
     * if there is a cable at the target side and if that cable can connect with this side.
     * This ignores any current cable connections.
     * @param world The world.
     * @param pos The center position.
     * @param side The side from the center position to check.
     * @param originCable The cable at the center position.
     * @return If it can connect.
     */
    public static boolean canCableConnectTo(IBlockReader world, BlockPos pos, Direction side, ICable originCable) {
        BlockPos neighbourPos = pos.offset(side);
        return getCable(world, neighbourPos, side.getOpposite())
                .map(neighbourCable -> originCable.canConnect(neighbourCable, side)
                        && neighbourCable.canConnect(originCable, side.getOpposite()))
                .orElse(false);
    }

    /**
     * Check if the given position is not a fake cable.
     * This can mean that there is no cable at all!
     * But if there is a cable, this method will return true only if it is a real cable.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return If there is no fake cable.
     */
    public static boolean isNoFakeCable(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return getCableFakeable(world, pos, side)
                .map(ICableFakeable::isRealCable)
                .orElse(true);
    }

    /**
     * Actions to be performed when a player right clicks on a cable.
     * @param world The world  of the cable.
     * @param pos The position of the cable.
     * @param state The blockstate of the cable.
     * @param player The player activating the cable.
     * @param heldItem The item with which the player is right-clicking.
     * @param side The side of the block the player right-clicked on.
     * @param cableConnectionHit The side identifying the cable connection that is being activated,
     *                           this will be null if the center part of the cable is activated.
     * @return Action result.
     */
    public static ActionResultType onCableActivated(World world, BlockPos pos, BlockState state, PlayerEntity player,
                                                    ItemStack heldItem, Direction side, @Nullable Direction cableConnectionHit) {
        ICable cable = CableHelpers.getCable(world, pos, side).orElse(null);
        if (cable == null) {
            return ActionResultType.PASS;
        }

        if(WrenchHelpers.isWrench(player, heldItem, world, pos, side)) {
            if (world.isRemote()) {
                return ActionResultType.SUCCESS; // Don't do anything client-side
            }
            if (player.isCrouching()) {
                removeCable(world, pos, player);
            } else if (cableConnectionHit != null) {
                // Disconnect cable side

                // Store the disconnection in the part entity
                cable.disconnect(cableConnectionHit);

                // Signal changes
                cable.updateConnections();
                Collection<Direction> sidesToUpdate = getCableConnections(cable);
                sidesToUpdate.add(cableConnectionHit);
                CableHelpers.updateConnectionsNeighbours(world, pos, sidesToUpdate);

                // Reinit the networks for this block and the disconnected neighbour.
                NetworkHelpers.initNetwork(world, pos, side);
                NetworkHelpers.initNetwork(world, pos.offset(cableConnectionHit), side.getOpposite());
            } else if (cableConnectionHit == null) {
                // Reconnect cable side
                BlockPos neighbourPos = pos.offset(side);
                ICable neighbourCable = CableHelpers.getCable(world, neighbourPos, side.getOpposite()).orElse(null);
                if(neighbourCable != null && !cable.isConnected(side) &&
                        (cable.canConnect(neighbourCable, side) || neighbourCable.canConnect(cable, side.getOpposite()))
                        ) {
                    // Notify the reconnection in the part entity of this and the neighbour block,
                    // since we don't know in which one the disconnection was made.
                    cable.reconnect(side);
                    neighbourCable.reconnect(side.getOpposite());

                    // Signal changes
                    cable.updateConnections();
                    Collection<Direction> sidesToUpdate = getCableConnections(cable);
                    sidesToUpdate.add(side);
                    CableHelpers.updateConnectionsNeighbours(world, pos, sidesToUpdate);

                    // Reinit the networks for this block and the connected neighbour.
                    NetworkHelpers.initNetwork(world, pos, side);
                    NetworkHelpers.initNetwork(world, neighbourPos, side.getOpposite());
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    /**
     * This should be called when a cable is added.
     * This method automatically notifies the neighbours and (re-)initializes the network if this cable carries one.
     * This should in most cases only be called server-side.
     * @param world The world.
     * @param pos The position.
     */
    public static void onCableAdded(World world, BlockPos pos) {
        CableHelpers.updateConnectionsNeighbours(world, pos, CableHelpers.ALL_SIDES);
        if(!world.isRemote()) {
            NetworkHelpers.initNetwork(world, pos, null)
                    .ifPresent(network -> MinecraftForge.EVENT_BUS.post(new NetworkInitializedEvent(network, world, pos, null)));
        }
    }

    /**
     * This should be called when a cable was added by a player.
     * This should be called after {@link CableHelpers#onCableAdded(World, BlockPos)}.
     * It simply emits an player-sensitive init event on the network bus.
     * @param world The world.
     * @param pos The position.
     * @param placer The entity who placed the cable.
     */
    public static void onCableAddedByPlayer(World world, BlockPos pos, @Nullable LivingEntity placer) {
        CableHelpers.updateConnectionsNeighbours(world, pos, CableHelpers.ALL_SIDES);
        if(!world.isRemote()) {
            NetworkHelpers.initNetwork(world, pos, null)
                    .ifPresent(network -> MinecraftForge.EVENT_BUS.post(new NetworkInitializedEvent(network, world, pos, placer)));
        }
    }

    /**
     * This should be called when a cable is being removed, while the part entity is still present.
     * This method won't do anything when called client-side.
     * @param world The world.
     * @param pos The position.
     * @param dropMainElement If the main part element should be dropped.
     * @param saveState If the element state should be saved in the item.
     * @return If the cable was removed from the network.
     */
    public static boolean onCableRemoving(World world, BlockPos pos, boolean dropMainElement, boolean saveState) {
        if (!world.isRemote() && CableHelpers.isNoFakeCable(world, pos, null)) {
            INetworkCarrier networkCarrier = NetworkHelpers.getNetworkCarrier(world, pos, null).orElse(null);

            // Get all drops from the network elements this cable provides.
            List<ItemStack> itemStacks = Lists.newLinkedList();
            INetworkElementProvider networkElementProvider = NetworkHelpers.getNetworkElementProvider(world, pos, null).orElse(null);
            if (networkElementProvider != null) {
                for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                    networkElement.addDrops(itemStacks, dropMainElement, saveState);
                }
                for (ItemStack itemStack : itemStacks) {
                    Block.spawnAsEntity(world, pos, itemStack);
                }
            }

            // If the cable has a network, remove it from the network.
            if(networkCarrier != null && networkCarrier.getNetwork() != null) {
                IPathElement pathElement = getPathElement(world, pos, null)
                        .orElseThrow(() -> new IllegalStateException("Could not find a valid path element capability"));
                INetwork network = networkCarrier.getNetwork();
                networkCarrier.setNetwork(null);
                return network.removePathElement(pathElement, null);
            }
        }
        return true;
    }

    /**
     * This should be called AFTER a cable is removed, at this stage the part entity will not exist anymore.
     * This method won't do anything when called client-side.
     * @param world The world.
     * @param pos The position.
     * @param sides The sides to update connections for.
     * @return If the cable was removed from the network.
     */
    public static boolean onCableRemoved(World world, BlockPos pos, Collection<Direction> sides) {
        updateConnectionsNeighbours(world, pos, sides);
        if (!world.isRemote()) {
            // Reinit neighbouring networks.
            for(Direction side : sides) {
                BlockPos sidePos = pos.offset(side);
                NetworkHelpers.initNetwork(world, sidePos, side.getOpposite());
            }
        }
        return true;
    }

    private static boolean removingCable = false;
    /**
     * @return If {@link #removeCable} is currently being called.
     */
    public static boolean isRemovingCable() {
        return removingCable;
    }
    /**
     * @param removingCable If the removing cable flag should be set
     */
    public static void setRemovingCable(boolean removingCable) {
        CableHelpers.removingCable = removingCable;
    }

    /**
     * Remove a cable.
     * This will automatically handle sounds, drops,
     * fakeable cables, network element removal and network (re)intialization.
     * @param world The world.
     * @param pos The position.
     * @param player The player removing the cable or null.
     */
    public static void removeCable(World world, BlockPos pos, @Nullable PlayerEntity player) {
        removingCable = true;
        ICable cable = getCable(world, pos, null).orElse(null);
        ICableFakeable cableFakeable = getCableFakeable(world, pos, null).orElse(null);
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos, null).orElse(null);
        BlockState blockState = world.getBlockState(pos);
        if (cable == null) {
            removingCable = false;
            return;
        }

        Collection<Direction> connectedCables = getCableConnections(cable);
        CableHelpers.onCableRemoving(world, pos, false, false);
        // If the cable has no parts or is not fakeable, remove the block,
        // otherwise mark the cable as being fake.
        if (cableFakeable == null || partContainer == null || !partContainer.hasParts()) {
            cable.destroy();
        } else {
            cableFakeable.setRealCable(false);
        }
        if (player == null) {
            ItemStackHelpers.spawnItemStack(world, pos, cable.getItemStack());
        } else if (!player.isCreative()) {
            ItemStackHelpers.spawnItemStackToPlayer(world, pos, cable.getItemStack(), player);
        }
        CableHelpers.onCableRemoved(world, pos, connectedCables);

        ItemBlockCable.playBreakSound(world, pos, blockState);

        removingCable = false;
    }

    /**
     * Check if the target has a facade.
     * @param world The world.
     * @param pos The position.
     * @return If it has a facade.
     */
    public static boolean hasFacade(IBlockReader world, BlockPos pos) {
        LazyOptional<IFacadeable> ret = TileHelpers.getCapability(world, pos, null, FacadeableConfig.CAPABILITY);
        ret.resolve(); // TODO: remove me (temp workaround to resolve race condition https://github.com/CyclopsMC/IntegratedDynamics/issues/986)
        return ret
                .map(IFacadeable::hasFacade)
                .orElse(false);
    }

    /**
     * Get the target's facade
     * @param world The world.
     * @param pos The position.
     * @return The optional facade.
     */
    public static LazyOptional<BlockState> getFacade(IBlockReader world, BlockPos pos) {
        return TileHelpers.getCapability(world, pos, null, FacadeableConfig.CAPABILITY)
                .lazyMap(IFacadeable::getFacade);
    }

    public static boolean isLightTransparent(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return PartHelpers.getPartContainer(world, pos, side)
                .map(partContainer -> {
                    for (Map.Entry<Direction, IPartType<?, ?>> entry : partContainer.getParts().entrySet()) {
                        IPartType part = entry.getValue();
                        if (part.forceLightTransparency(partContainer.getPartState(entry.getKey()))) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get the sides the cable is currently connected to.
     * @param cable A cable.
     * @return The cable connections.
     */
    public static Collection<Direction> getCableConnections(ICable cable) {
        Collection<Direction> sides = Sets.newIdentityHashSet();
        for (Direction side : Direction.values()) {
            if (cable.isConnected(side)) {
                sides.add(side);
            }
        }
        return sides;
    }

    /**
     * Get the sides that are externally connected to the given position.
     * @param world The world.
     * @param pos The position.
     * @return The sides.
     */
    public static Collection<Direction> getExternallyConnectedCables(World world, BlockPos pos) {
        Collection<Direction> sides = Sets.newIdentityHashSet();
        for (Direction side : Direction.values()) {
            if (CableHelpers.isCableConnected(world, pos.offset(side), side.getOpposite())) {
                sides.add(side);
            }
        }
        return sides;
    }
}
