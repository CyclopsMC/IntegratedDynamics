package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.PartNetworkConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import javax.annotation.Nullable;

/**
 * Network helper methods.
 * @author rubensworks
 */
public class NetworkHelpers {

    /**
     * Get the network carrier capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @return The network carrier capability, or null if not present.
     */
    public static INetworkCarrier getNetworkCarrier(IBlockAccess world, BlockPos pos) {
        return TileHelpers.getCapability(world, pos, NetworkCarrierConfig.CAPABILITY);
    }

    /**
     * Get the network element provider capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @return The network element provider capability, or null if not present.
     */
    public static INetworkElementProvider getNetworkElementProvider(IBlockAccess world, BlockPos pos) {
        return TileHelpers.getCapability(world, pos, NetworkElementProviderConfig.CAPABILITY);
    }

    /**
     * Get the network at the given position.
     * @param world The world.
     * @param pos The position.
     * @return The network, or null if no network or network carrier present.
     */
    public static INetwork getNetwork(IBlockAccess world, BlockPos pos) {
        INetworkCarrier networkCarrier = getNetworkCarrier(world, pos);
        if (networkCarrier != null) {
            return networkCarrier.getNetwork();
        }
        return null;
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The part network.
     */
    public static IPartNetwork getPartNetwork(@Nullable INetwork network) {
        return network != null && network.hasCapability(PartNetworkConfig.CAPABILITY)
                ? network.getCapability(PartNetworkConfig.CAPABILITY) : null;
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The part network.
     */
    public static IEnergyNetwork getEnergyNetwork(@Nullable INetwork network) {
        return network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY)
                ? network.getCapability(EnergyNetworkConfig.CAPABILITY) : null;
    }

    /**
     * Form a new network starting from the given position.
     * This position should have a {@link IPathElement} capability,
     * otherwise this method will fail silently.
     * This will correctly transfer all passed network elements to this new network.
     * @param world The world.
     * @param pos The starting position.
     * @return The newly created part network.
     * Can be null if the starting position did not have a {@link IPathElement} capability.
     */
    public static @Nullable INetwork initNetwork(World world, BlockPos pos) {
        IPathElement pathElement = TileHelpers.getCapability(world, pos, null, PathElementConfig.CAPABILITY);
        if (pathElement != null) {
            Network network = Network.initiateNetworkSetup(pathElement);
            network.initialize();
            return network;
        }
        return null;
    }

    /**
     * This MUST be called by blocks having the {@link INetworkElementProvider} capability in
     * when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)} is called.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param neighborBlock The block type of the neighbour that was updated.
     */
    public static void onElementProviderBlockNeighborChange(World world, BlockPos pos, Block neighborBlock) {
        if (!world.isRemote) {
            INetwork network = getNetwork(world, pos);
            INetworkElementProvider networkElementProvider = getNetworkElementProvider(world, pos);
            for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.onNeighborBlockChange(network, world, neighborBlock);
            }
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
     * @param world The world.
     * @param pos The position.
     */
    public static void invalidateNetworkElements(World world, BlockPos pos) {
        INetworkCarrier networkCarrier = TileHelpers.getCapability(world, pos, NetworkCarrierConfig.CAPABILITY);
        if (networkCarrier != null) {
            INetwork network = networkCarrier.getNetwork();
            if (network != null) {
                INetworkElementProvider networkElementProvider = TileHelpers.getCapability(world, pos, NetworkElementProviderConfig.CAPABILITY);
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
     * @param world The world.
     * @param pos The position.
     */
    public static void revalidateNetworkElements(World world, BlockPos pos) {
        INetworkCarrier networkCarrier = TileHelpers.getCapability(world, pos, NetworkCarrierConfig.CAPABILITY);
        IPathElement pathElement = TileHelpers.getCapability(world, pos, PathElementConfig.CAPABILITY);
        if (networkCarrier != null && pathElement != null && networkCarrier.getNetwork() == null) {
            INetworkElementProvider networkElementProvider = TileHelpers.getCapability(world, pos, NetworkElementProviderConfig.CAPABILITY);
            if (networkElementProvider != null) {
                // Attempt to revalidate the network elements in this provider
                for (INetwork network : NetworkWorldStorage.getInstance(IntegratedDynamics._instance).getNetworks()) {
                    if (network.containsPathElement(pathElement)) {
                        // Revalidate all network elements
                        for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                            networkElement.revalidate(network);
                        }
                        break; // No need to check the other networks anymore
                    }
                }
            }
        }
    }

}
