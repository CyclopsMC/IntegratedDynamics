package org.cyclops.integrateddynamics.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.NetworkElementBase;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

import java.util.List;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CoalGeneratorNetworkElement extends NetworkElementBase {

    private final DimPos pos;

    protected TileCoalGenerator getTile() {
        return TileHelpers.getSafeTile(getPos().getWorld(), getPos().getBlockPos(), TileCoalGenerator.class);
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement) {
        TileCoalGenerator tile = getTile();
        if(tile != null) {
            InventoryHelper.dropInventoryItems(getPos().getWorld(), getPos().getBlockPos(), tile.getInventory());
        }
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
        return partNetwork != null && partNetwork.addVariableContainer(getPos());
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
        if (partNetwork != null) {
            partNetwork.removeVariableContainer(getPos());
        }
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof CoalGeneratorNetworkElement) {
            return getPos().compareTo(((CoalGeneratorNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

}
