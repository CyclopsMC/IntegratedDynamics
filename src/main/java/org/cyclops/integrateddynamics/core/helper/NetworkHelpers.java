package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.TickHandler;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Network helper methods.
 * @author rubensworks
 */
public class NetworkHelpers {

    /**
     * Get the network carrier capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional network carrier capability.
     */
    public static Optional<INetworkCarrier> getNetworkCarrier(ILevelExtension world, BlockPos pos, @Nullable Direction side) {
        return BlockEntityHelpers.getCapability(world, pos, side, Capabilities.NetworkCarrier.BLOCK);
    }

    /**
     * Get the network element provider capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional network element provider capability.
     */
    public static Optional<INetworkElementProvider> getNetworkElementProvider(ILevelExtension world, BlockPos pos, @Nullable Direction side) {
        return BlockEntityHelpers.getCapability(world, pos, side, Capabilities.NetworkElementProvider.BLOCK);
    }

    /**
     * Get the network at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional network.
     */
    public static Optional<INetwork> getNetwork(ILevelExtension world, BlockPos pos, @Nullable Direction side) {
        Optional<Optional<INetwork>> networkCarried = getNetworkCarrier(world, pos, side)
                .map(carrier -> {
                    INetwork network = carrier.getNetwork();
                    return network != null ? Optional.of(network) : Optional.empty();
                });
        return networkCarried.orElse(Optional.empty());
    }

    /**
     * Get the network at the given position.
     * @param pos The position.
     * @return The optional network.
     */
    public static Optional<INetwork> getNetwork(PartPos pos) {
        return getNetwork(pos.getPos().getLevel(true), pos.getPos().getBlockPos(), pos.getSide());
    }

    /**
     * Get the network at the given position.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a network present.
     *
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The network.
     */
    public static INetwork getNetworkChecked(ILevelExtension world, BlockPos pos, @Nullable Direction side) {
        return getNetwork(world, pos, side)
                .orElseThrow(() -> new IllegalStateException("Could not find a network container at " + pos.toString()));
    }

    /**
     * Get the network at the given position.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a network present.
     *
     * @param pos The position.
     * @return The network.
     */
    public static INetwork getNetworkChecked(PartPos pos) {
        return getNetwork(pos)
                .orElseThrow(() -> new IllegalStateException("Could not find a network container at " + pos.toString()));
    }

    /**
     * Get the part network capability of a network.
     * @param optionalNetwork The optional network.
     * @return The optional part network.
     */
    public static Optional<IPartNetwork> getPartNetwork(Optional<INetwork> optionalNetwork) {
        return optionalNetwork
                .map(network -> network.getCapability(Capabilities.PartNetwork.NETWORK))
                .orElse(Optional.empty());
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The optional part network.
     */
    public static Optional<IPartNetwork> getPartNetwork(@Nullable INetwork network) {
        if (network == null) {
            return Optional.empty();
        }
        return network.getCapability(Capabilities.PartNetwork.NETWORK);
    }

    /**
     * Get the part network capability of a network.
     * If it is not present, then an illegal state exception will be thrown.
     *
     * This should only be called if you know for certain that there will be a part network present.
     *
     * @param network The network.
     * @return The part network.
     */
    public static IPartNetwork getPartNetworkChecked(INetwork network) {
        return network.getCapability(Capabilities.PartNetwork.NETWORK)
                .orElseThrow(() -> new IllegalStateException("Could not find a network's part network"));
    }

    /**
     * Get the part network capability of a network.
     * @param optionalNetwork The optional network.
     * @return The optional energy network.
     */
    public static Optional<IEnergyNetwork> getEnergyNetwork(Optional<INetwork> optionalNetwork) {
        return optionalNetwork
                .map(network -> network.getCapability(Capabilities.EnergyNetwork.NETWORK))
                .orElse(Optional.empty());
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The optional energy network.
     */
    public static Optional<IEnergyNetwork> getEnergyNetwork(@Nullable INetwork network) {
        if (network == null) {
            return Optional.empty();
        }
        return network.getCapability(Capabilities.EnergyNetwork.NETWORK);
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The energy network.
     */
    public static IEnergyNetwork getEnergyNetworkChecked(INetwork network) {
        return network.getCapability(Capabilities.EnergyNetwork.NETWORK)
                .orElseThrow(() -> new IllegalStateException("Could not find a network's energy network"));
    }

    /**
     * Get the ingredient network within a network.
     * @param optionalNetwork The optional network.
     * @param ingredientComponent The ingredient component type.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The optional ingredient network.
     */
    public static <T, M> Optional<IPositionedAddonsNetworkIngredients<T, M>> getIngredientNetwork(Optional<INetwork> optionalNetwork,
                                                                                                      IngredientComponent<T, M> ingredientComponent) {
        return optionalNetwork
                .map(network -> ingredientComponent.getCapability(Capabilities.PositionedAddonsNetworkIngredientsHandler.INGREDIENT)
                        .map(handler -> handler.getStorage(network))
                        .orElse(Optional.empty()))
                .orElse(Optional.empty());
    }

    /**
     * Form a new network starting from the given position.
     * This position should have a {@link IPathElement} capability,
     * otherwise this method will fail silently.
     * This will correctly transfer all passed network elements to this new network.
     * @param world The world.
     * @param pos The starting position.
     * @param side The side.
     * @return The optionally created part network.
     * Can be absent if the starting position did not have a {@link IPathElement} capability.
     */
    public static Optional<INetwork> initNetwork(ILevelExtension world, BlockPos pos, @Nullable Direction side) {
        return BlockEntityHelpers.getCapability(world, pos, side, Capabilities.PathElement.BLOCK)
                .map(pathElement -> {
                    Network network = Network.initiateNetworkSetup(SidedPathElement.of(pathElement, side));
                    network.initialize();
                    return Optional.<INetwork>of(network);
                })
                .orElse(Optional.empty());
    }

    /**
     * This MUST be called by blocks having the {@link INetworkElementProvider} capability in
     * when a neighbouring block is updated, more specifically when
     * {@link Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)},
     * {@link Block#onNeighborChange(BlockState, LevelReader, BlockPos, BlockPos)}
     * or {@link Block#updateShape(BlockState, Direction, BlockState, LevelAccessor, BlockPos, BlockPos)} is called.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param side The side at the center block.
     * @param neighbourBlock The block type of the neighbour that was updated.
     * @param neighbourBlockPos The position of the neighbour that was updated.
     */
    public static void onElementProviderBlockNeighborChange(Level world, BlockPos pos, Block neighbourBlock,
                                                            @Nullable Direction side, BlockPos neighbourBlockPos) {
        if (!world.isClientSide()) {
            getNetwork(world, pos, side).ifPresent(network -> {
                getNetworkElementProvider(world, pos, side).ifPresent(networkElementProvider -> {
                    for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                        networkElement.onNeighborBlockChange(network, world, neighbourBlock, neighbourBlockPos);
                    }
                });
            });
        }
    }

    /**
     * @return If networks should work and evaluations should be done.
     */
    public static boolean shouldWork() {
        return !GeneralConfig.safeMode;
    }

    /**
     * Invalidate all network elements at the given position.
     * Warning: this assumes unsided network carrier capabilities, for example full-block network elements.
     * @param world The world.
     * @param pos The position.
     * @param tile The tile entity that is unloaded.
     */
    public static void invalidateNetworkElements(Level world, BlockPos pos, BlockEntity tile) {
        INetworkCarrier networkCarrier = world.getCapability(Capabilities.NetworkCarrier.BLOCK, pos, null);
        if (networkCarrier != null) {
            INetwork network = networkCarrier.getNetwork();
            if (network != null) {
                INetworkElementProvider networkElementProvider = world.getCapability(Capabilities.NetworkElementProvider.BLOCK, pos, null);
                if (networkElementProvider != null) {
                    for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                        networkElement.invalidate(network);
                    }
                }
            }
        }
    }

    /**
     * Revalidate all network elements at the given position.
     * Warning: this assumes unsided network carrier capabilities, for example full-block network elements.
     * @param world The world.
     * @param pos The position.
     */
    public static void revalidateNetworkElements(Level world, BlockPos pos) {
        INetworkCarrier networkCarrier = BlockEntityHelpers.getCapability(world, pos, Capabilities.NetworkCarrier.BLOCK).orElse(null);
        IPathElement pathElement = BlockEntityHelpers.getCapability(world, pos, Capabilities.PathElement.BLOCK).orElse(null);
        if (TickHandler.getInstance().ticked
                && networkCarrier != null && pathElement != null && networkCarrier.getNetwork() == null
                && BlockEntityHelpers.getCapability(world, pos, Capabilities.CableFakeable.BLOCK).map(ICableFakeable::isRealCable).orElse(false)) {
            BlockEntityHelpers.getCapability(world, pos, Capabilities.NetworkElementProvider.BLOCK).ifPresent(networkElementProvider -> {
                // Attempt to revalidate the network elements in this provider
                boolean foundNetwork = false;
                for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                    if (network.containsSidedPathElement(SidedPathElement.of(pathElement, null))) {
                        // Revalidate all network elements
                        for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                            networkElement.revalidate(network);
                        }
                        foundNetwork = true;
                        break; // No need to check the other networks anymore
                    }
                }

                // If no existing network was found, create a new network
                if (!foundNetwork && GeneralConfig.recreateCorruptedNetworks) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Detected network position at " +
                            "position %s in world %s with corrupted network, recreating network...", pos, world.dimension().location()));
                    NetworkHelpers.initNetwork(world, pos, null);
                }
            });
        }
    }

}
