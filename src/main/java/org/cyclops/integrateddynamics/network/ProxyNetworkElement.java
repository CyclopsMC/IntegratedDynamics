package org.cyclops.integrateddynamics.network;

import lombok.Data;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.network.ConsumingNetworkElementBase;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import java.util.List;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
@Data
public class ProxyNetworkElement extends ConsumingNetworkElementBase<IPartNetwork> {

    private final DimPos pos;

    protected TileProxy getTile() {
        return TileHelpers.getSafeTile(getPos().getWorld(), getPos().getBlockPos(), TileProxy.class);
    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {
        TileProxy tile = getTile();
        if(tile != null) {
            InventoryHelper.dropInventoryItems(getPos().getWorld(), getPos().getBlockPos(), tile.getInventory());
        }
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof ProxyNetworkElement) {
            return getPos().compareTo(((ProxyNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

    @Override
    public int getConsumptionRate() {
        return 2;
    }
}
