package org.cyclops.integrateddynamics.core.block.cable;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.api.tileentity.ITileCable;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.core.network.PartNetwork;
import org.cyclops.integrateddynamics.core.path.CablePathElement;

/**
 * A component for {@link ICableNetwork}.
 * @author rubensworks
 */
public class CableNetworkComponent<C extends Block & ICableNetwork<IPartNetwork, ICablePathElement>> implements ICableNetwork<IPartNetwork, ICablePathElement> {

    private final C cable;

    public CableNetworkComponent(C cable) {
        this.cable = cable;
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return new CablePathElement(cable, DimPos.of(world, blockPos));
    }

    @Override
    public void initNetwork(World world, BlockPos pos) {
        PartNetwork.initiateNetworkSetup(cable, world, pos).initialize();
    }

    @Override
    public boolean canConnect(World world, BlockPos pos, ICable connector, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        return tile != null && tile.canConnect(connector, side);
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        if(tile != null) {
            tile.updateConnections();
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        return tile != null && tile.isConnected(side);
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        if(tile != null) {
            tile.disconnect(side);
        }
    }

    @Override
    public void reconnect(World world, BlockPos pos, EnumFacing side) {
        ITileCable tile = TileHelpers.getSafeTile(world, pos, ITileCable.class);
        if(tile != null) {
            tile.reconnect(side);
        }
    }

    @Override
    public void resetCurrentNetwork(World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            tile.resetCurrentNetwork();
        }
    }

    @Override
    public void setNetwork(IPartNetwork network, World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            if(tile.getNetwork() != null) {
                IntegratedDynamics.clog(Level.WARN, "Tried to set a new network for a tile without the previous one being removed.");
            }
            tile.setNetwork(network);
        }
    }

    @Override
    public IPartNetwork getNetwork(World world, BlockPos pos) {
        ITileCableNetwork tile = TileHelpers.getSafeTile(world, pos, ITileCableNetwork.class);
        if(tile != null) {
            return tile.getNetwork();
        }
        return null;
    }

    /**
     * Request to update the cable connections at the given position.
     * @param world The world.
     * @param pos The position of this block.
     */
    public void requestConnectionsUpdate(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof ICable) {
            ((ICable) block).updateConnections(world, pos);
        }
    }

    /**
     * Trigger a connections update for all neighbours.
     * @param world The world.
     * @param blockPos The center positions.
     */
    public void triggerNeighbourConnections(World world, BlockPos blockPos) {
        for(EnumFacing side : EnumFacing.VALUES) {
            requestConnectionsUpdate(world, blockPos.offset(side));
        }
    }

    /**
     * Add this block to a network.
     * @param world The world.
     * @param pos The position.
     */
    public void addToNetwork(World world, BlockPos pos) {
        triggerNeighbourConnections(world, pos);
        if(!world.isRemote) {
            initNetwork(world, pos);
        }
    }

    /**
     * Remove this block from its current network.
     * @param world The world.
     * @param pos The position.
     * @return If the cable was removed.
     */
    public boolean removeFromNetwork(World world, BlockPos pos) {
        return removeFromNetwork(world, pos, true) && removeFromNetwork(world, pos, false);
    }

    /**
     * Remove this block from its current network.
     * @param world The world.
     * @param pos The position.
     * @param preDestroy At which stage of the block destruction this is being called.
     * @return If the cable was removed from the network.
     */
    public boolean removeFromNetwork(World world, BlockPos pos, boolean preDestroy) {
        if(preDestroy) {
            // Remove the cable from this network if it exists
            IPartNetwork network = getNetwork(world, pos);
            if(network != null) {
                return network.removeCable(cable, createPathElement(world, pos));
            }
        } else {
            triggerNeighbourConnections(world, pos);
            // Reinit neighbouring networks.
            for(EnumFacing side : EnumFacing.VALUES) {
                if(!world.isRemote) {
                    BlockPos sidePos = pos.offset(side);
                    Block block = world.getBlockState(sidePos).getBlock();
                    if(block instanceof ICableNetwork) {
                        ((ICableNetwork<IPartNetwork, ICablePathElement>) block).initNetwork(world, sidePos);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Called before this block is destroyed.
     * @param world The world.
     * @param pos The position.
     * @return If the cable can be removed.
     */
    public boolean onPreBlockDestroyed(World world, BlockPos pos) {
        if(!world.isRemote) {
            return removeFromNetwork(world, pos, true);
        }
        return true;
    }

    /**
     * Called before after block is destroyed.
     * @param world The world.
     * @param pos The position.
     * @return If the cable was removed.
     */
    public boolean onPostBlockDestroyed(World world, BlockPos pos) {
        if(!world.isRemote) {
            return removeFromNetwork(world, pos, false);
        }
        return true;
    }

    /**
     * Check if one side of a cable can connect.
     * To be used when the cable connections are being updated.
     * @param world The world.
     * @param pos The center position.
     * @param side The side from the center position to check.
     * @param originCable The cable at the center position.
     * @return If it can connect.
     */
    public static boolean canSideConnect(World world, BlockPos pos, EnumFacing side, ICable originCable) {
        BlockPos neighbourPos = pos.offset(side);
        Block neighbourBlock = world.getBlockState(neighbourPos).getBlock();
        return neighbourBlock instanceof ICable &&
                ((ICable) neighbourBlock).canConnect(world, neighbourPos, (ICable) originCable,
                        side.getOpposite());
    }

}
