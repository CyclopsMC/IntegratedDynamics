package org.cyclops.integrateddynamics.core.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;

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
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement) {

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
}
