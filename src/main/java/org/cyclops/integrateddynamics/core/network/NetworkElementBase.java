package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base implementation for a network element.
 * @author rubensworks
 */
@Data
public abstract class NetworkElementBase implements INetworkElement {

    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public boolean isUpdate() {
        return false;
    }

    @Override
    public void update(INetwork network) {

    }

    @Override
    public void beforeNetworkKill(INetwork network) {

    }

    @Override
    public void afterNetworkAlive(INetwork network) {

    }

    @Override
    public void afterNetworkReAlive(INetwork network) {

    }

    @Override
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {

    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        return true;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {

    }

    @Override
    public void onPreRemoved(INetwork network) {

    }

    @Override
    public void onPostRemoved(INetwork network) {

    }

    @Override
    public void onNeighborBlockChange(@Nullable INetwork network, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public void invalidate(INetwork network) {
        network.invalidateElement(this);
    }

    @Override
    public void revalidate(INetwork network) {
        network.revalidateElement(this);
    }

    protected boolean canRevalidatePositioned(INetwork network, DimPos dimPos) {
        return dimPos.isLoaded();
    }

    protected void revalidatePositioned(INetwork network, DimPos dimPos) {
        INetworkCarrier networkCarrier = NetworkHelpers.getNetworkCarrier(dimPos.getWorld(), dimPos.getBlockPos(), null);
        if (networkCarrier != null) {
            networkCarrier.setNetwork(network);
        }
    }
}
