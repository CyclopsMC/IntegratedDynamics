package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
     * @return The network carrier capability, or null if not present.
     */
    @Nullable
    public static INetworkCarrier getNetworkCarrier(IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return TileHelpers.getCapability(world, pos, side, NetworkCarrierConfig.CAPABILITY);
    }

    /**
     * Get the network element provider capability at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The network element provider capability, or null if not present.
     */
    @Nullable
    public static INetworkElementProvider getNetworkElementProvider(IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return TileHelpers.getCapability(world, pos, side, NetworkElementProviderConfig.CAPABILITY);
    }

    /**
     * Get the network at the given position.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The network, or null if no network or network carrier present.
     */
    @Nullable
    public static INetwork getNetwork(IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        INetworkCarrier networkCarrier = getNetworkCarrier(world, pos, side);
        if (networkCarrier != null) {
            return networkCarrier.getNetwork();
        }
        return null;
    }

    /**
     * Get the network at the given position.
     * @param pos The position.
     * @return The network, or null if no network or network carrier present.
     */
    @Nullable
    public static INetwork getNetwork(PartPos pos) {
        return getNetwork(pos.getPos().getWorld(), pos.getPos().getBlockPos(), pos.getSide());
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The part network.
     */
    @Nullable
    public static IPartNetwork getPartNetwork(@Nullable INetwork network) {
        return network != null && network.hasCapability(PartNetworkConfig.CAPABILITY)
                ? network.getCapability(PartNetworkConfig.CAPABILITY) : null;
    }

    /**
     * Get the part network capability of a network.
     * @param network The network.
     * @return The part network.
     */
    @Nullable
    public static IEnergyNetwork getEnergyNetwork(@Nullable INetwork network) {
        return network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY)
                ? network.getCapability(EnergyNetworkConfig.CAPABILITY) : null;
    }

    /**
     * Get the ingredient network within a network.
     * @param network The network.
     * @param ingredientComponent The ingredient component type.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The ingredient network.
     */
    @Nullable
    public static <T, M> IPositionedAddonsNetworkIngredients<T, M> getIngredientNetwork(@Nullable INetwork network,
                                                                                        IngredientComponent<T, M> ingredientComponent) {
        return network != null && ingredientComponent.hasCapability(PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY)
                ? ingredientComponent.getCapability(PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY)
                .getStorage(network) : null;
    }

    /**
     * Form a new network starting from the given position.
     * This position should have a {@link IPathElement} capability,
     * otherwise this method will fail silently.
     * This will correctly transfer all passed network elements to this new network.
     * @param world The world.
     * @param pos The starting position.
     * @param side The side.
     * @return The newly created part network.
     * Can be null if the starting position did not have a {@link IPathElement} capability.
     */
    public static @Nullable INetwork initNetwork(World world, BlockPos pos, @Nullable EnumFacing side) {
        IPathElement pathElement = TileHelpers.getCapability(world, pos, side, PathElementConfig.CAPABILITY);
        if (pathElement != null) {
            Network network = Network.initiateNetworkSetup(SidedPathElement.of(pathElement, side));
            network.initialize();
            return network;
        }
        return null;
    }

    /**
     * This MUST be called by blocks having the {@link INetworkElementProvider} capability in
     * when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)},
     * {@link Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)}
     * or {@link Block#observedNeighborChange(IBlockState, World, BlockPos, Block, BlockPos)} is called.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param side The side at the center block.
     * @param neighbourBlock The block type of the neighbour that was updated.
     */
    @Deprecated // TODO: remove in 1.14
    public static void onElementProviderBlockNeighborChange(World world, BlockPos pos, Block neighbourBlock,
                                                            @Nullable EnumFacing side) {
        onElementProviderBlockNeighborChange(world, pos, neighbourBlock, side, null);
    }

    /**
     * This MUST be called by blocks having the {@link INetworkElementProvider} capability in
     * when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)},
     * {@link Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)}
     * or {@link Block#observedNeighborChange(IBlockState, World, BlockPos, Block, BlockPos)} is called.
     * @param world The world in which the neighbour was updated.
     * @param pos The position of the center block.
     * @param side The side at the center block.
     * @param neighbourBlock The block type of the neighbour that was updated.
     * @param neighbourBlockPos The position of the neighbour that was updated.
     */
    public static void onElementProviderBlockNeighborChange(World world, BlockPos pos, Block neighbourBlock,
                                                            @Nullable EnumFacing side, BlockPos neighbourBlockPos) {
        if (!world.isRemote) {
            INetwork network = getNetwork(world, pos, side);
            INetworkElementProvider networkElementProvider = getNetworkElementProvider(world, pos, side);
            for (INetworkElement networkElement : networkElementProvider.createNetworkElements(world, pos)) {
                networkElement.onNeighborBlockChange(network, world, neighbourBlock, neighbourBlockPos);
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
     * Warning: this assumes unsided network carrier capabilities, for example full-block network elements.
     * @param world The world.
     * @param pos The position.
     * @param tile The tile entity that is unloaded.
     */
    public static void invalidateNetworkElements(World world, BlockPos pos, TileEntity tile) {
        INetworkCarrier networkCarrier = tile.getCapability(NetworkCarrierConfig.CAPABILITY, null);
        if (networkCarrier != null) {
            INetwork network = networkCarrier.getNetwork();
            if (network != null) {
                INetworkElementProvider networkElementProvider = tile.getCapability(NetworkElementProviderConfig.CAPABILITY, null);
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
    public static void revalidateNetworkElements(World world, BlockPos pos) {
        INetworkCarrier networkCarrier = TileHelpers.getCapability(world, pos, NetworkCarrierConfig.CAPABILITY);
        IPathElement pathElement = TileHelpers.getCapability(world, pos, PathElementConfig.CAPABILITY);
        if (networkCarrier != null && pathElement != null && networkCarrier.getNetwork() == null) {
            INetworkElementProvider networkElementProvider = TileHelpers.getCapability(world, pos, NetworkElementProviderConfig.CAPABILITY);
            if (networkElementProvider != null) {
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
            }
        }
    }

}
