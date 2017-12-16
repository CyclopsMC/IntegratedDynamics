package org.cyclops.integrateddynamics.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
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
    public void setPriority(INetwork network, int priority) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean canRevalidate(INetwork network) {
        return canRevalidatePositioned(network, pos);
    }

    @Override
    public void revalidate(INetwork network) {
        super.revalidate(network);
        revalidatePositioned(network, pos);
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof CoalGeneratorNetworkElement) {
            return getPos().compareTo(((CoalGeneratorNetworkElement) o).getPos());
        }
        return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }

}
