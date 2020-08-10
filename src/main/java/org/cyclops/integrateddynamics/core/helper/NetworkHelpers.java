package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.PartNetworkConfig;
import org.cyclops.integrateddynamics.capability.network.PositionedAddonsNetworkIngredientsHandlerConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
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
    public static LazyOptional<INetworkCarrier> getNetworkCarrier(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, NetworkCarrierConfig.CAPABILITY);
    }

    /**
     * Get the network element provider capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional network element provider capability.
     */
    public static LazyOptional<INetworkElementProvider> getNetworkElementProvider(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, NetworkElementProviderConfig.CAPABILITY);
    }

    /**
     * Get the network at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The optional network.
     */
    public static LazyOptional<INetwork> getNetwork(IBlockReader world, BlockPos pos, @Nullable Direction side) {
        LazyOptional<LazyOptional<INetwork>> networkCarried = getNetworkCarrier(world, pos, side)
                .map(carrier -> {
                    INetwork network = carrier.getNetwork();
                    return network != null ? LazyOptional.of(() -> network) : LazyOptional.empty();
                });
        return networkCarried.orElse(LazyOptional.empty());
    }

    /**
     * Get the network at the given position.
     * @param pos The position.
     * @return The optional network.
     */
    public static LazyOptional<INetwork> getNetwork(PartPos pos) {
        return getNetwork(pos.getPos().getWorld(true), pos.getPos().getBlockPos(), pos.getSide());
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
    public static INetwork getNetworkChecked(IBlockReader world, BlockPos pos, @Nullable Direction side) {
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
    public static LazyOptional<IPartNetwork> getPartNetwork(LazyOptional<INetwork> optionalNetwork) {
        return optionalNetwork
                .map(network -> network.getCapability(PartNetworkConfig.CAPABILITY))
                .orElse(LazyOptional.empty());
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The optional part network.
     */
    public static LazyOptional<IPartNetwork> getPartNetwork(@Nullable INetwork network) {
        if (network == null) {
            return LazyOptional.empty();
        }
        return network.getCapability(PartNetworkConfig.CAPABILITY);
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
        return network.getCapability(PartNetworkConfig.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not find a network's part network"));
    }

    /**
     * Get the part network capability of a network.
     * @param optionalNetwork The optional network.
     * @return The optional energy network.
     */
    public static LazyOptional<IEnergyNetwork> getEnergyNetwork(LazyOptional<INetwork> optionalNetwork) {
        return optionalNetwork
                .map(network -> network.getCapability(EnergyNetworkConfig.CAPABILITY))
                .orElse(LazyOptional.empty());
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The optional energy network.
     */
    public static LazyOptional<IEnergyNetwork> getEnergyNetwork(@Nullable INetwork network) {
        if (network == null) {
            return LazyOptional.empty();
        }
        return network.getCapability(EnergyNetworkConfig.CAPABILITY);
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The energy network.
     */
    public static IEnergyNetwork getEnergyNetworkChecked(INetwork network) {
        return network.getCapability(EnergyNetworkConfig.CAPABILITY)
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
    public static <T, M> LazyOptional<IPositionedAddonsNetworkIngredients<T, M>> getIngredientNetwork(LazyOptional<INetwork> optionalNetwork,
                                                                                                      IngredientComponent<T, M> ingredientComponent) {
        return optionalNetwork
                .map(network -> ingredientComponent.getCapability(PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY)
                        .map(handler -> handler.getStorage(network))
                        .orElse(LazyOptional.empty()))
                .orElse(LazyOptional.empty());
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
    public static Optional<INetwork> initNetwork(World world, BlockPos pos, @Nullable Direction side) {
        return TileHelpers.getCapability(world, pos, side, PathElementConfig.CAPABILITY)
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
     * {@link net.minecraft.block.Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)},
     * {@link Block#onNeighborChange(BlockState, IWorldReader, BlockPos, BlockPos)}}
     * or {@link Block#observedNeighborChange(BlockState, World, BlockPos, Block, BlockPos)} is called.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param side The side at the center block.
     * @param neighbourBlock The block type of the neighbour that was updated.
     * @param neighbourBlockPos The position of the neighbour that was updated.
     */
    public static void onElementProviderBlockNeighborChange(World world, BlockPos pos, Block neighbourBlock,
                                                            @Nullable Direction side, BlockPos neighbourBlockPos) {
        if (!world.isRemote()) {
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
    public static void invalidateNetworkElements(World world, BlockPos pos, TileEntity tile) {
        tile.getCapability(NetworkCarrierConfig.CAPABILITY, null).ifPresent(networkCarrier -> {
            INetwork network = networkCarrier.getNetwork();
            if (network != null) {
                tile.getCapability(NetworkElementProviderConfig.CAPABILITY, null).ifPresent(networkElementProvider -> {
                    for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                        networkElement.invalidate(network);
                    }
                });
            }
        });
    }

    /**
     * Revalidate all network elements at the given position.
     * Warning: this assumes unsided network carrier capabilities, for example full-block network elements.
     * @param world The world.
     * @param pos The position.
     */
    public static void revalidateNetworkElements(World world, BlockPos pos) {
        INetworkCarrier networkCarrier = TileHelpers.getCapability(world, pos, NetworkCarrierConfig.CAPABILITY).orElse(null);
        IPathElement pathElement = TileHelpers.getCapability(world, pos, PathElementConfig.CAPABILITY).orElse(null);
        if (networkCarrier != null && pathElement != null && networkCarrier.getNetwork() == null) {
            TileHelpers.getCapability(world, pos, NetworkElementProviderConfig.CAPABILITY).ifPresent(networkElementProvider -> {
                // Attempt to revalidate the network elements in this provider
                for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                    if (network.containsSidedPathElement(SidedPathElement.of(pathElement, null))) {
                        // Revalidate all network elements
                        for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                            networkElement.revalidate(network);
                        }
                        break; // No need to check the other networks anymore
                    }
                }
            });
        }
    }

}
