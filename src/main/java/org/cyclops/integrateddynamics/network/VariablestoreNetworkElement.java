package org.cyclops.integrateddynamics.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.network.ConsumingNetworkElementBase;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

import java.util.List;
import java.util.Map;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
@Data
public class VariablestoreNetworkElement extends ConsumingNetworkElementBase<IPartNetwork> {

    private final DimPos pos;

    protected TileVariablestore getTile() {
        return TileHelpers.getSafeTile(getPos().getWorld(), getPos().getBlockPos(), TileVariablestore.class);
    }

    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public boolean isUpdate() {
        return false;
    }

    @Override
    public void update(IPartNetwork network) {

    }

    @Override
    public void beforeNetworkKill(IPartNetwork network) {

    }

    @Override
    public void afterNetworkAlive(IPartNetwork network) {

    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {
        TileVariablestore tile = getTile();
        if(tile != null) {
            InventoryHelper.dropInventoryItems(getPos().getWorld(), getPos().getBlockPos(), tile.getInventory());
        }
    }

    @Override
    public boolean onNetworkAddition(IPartNetwork network) {
        return network.addVariableContainer(getPos());
    }

    @Override
    public void onNetworkRemoval(IPartNetwork network) {
        network.removeVariableContainer(getPos());
    }

    @Override
    public void onPreRemoved(IPartNetwork network) {

    }

    @Override
    public void onNeighborBlockChange(IPartNetwork network, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof VariablestoreNetworkElement) {
            return getPos().compareTo(((VariablestoreNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

    /**
     * @return The stored variable facades for this network element.
     */
    public Map<Integer, IVariableFacade> getVariableCache() {
        return getTile().getVariableCache();
    }

    @Override
    public int getConsumptionRate() {
        return 4;
    }

    @Override
    public void postUpdate(IPartNetwork network, boolean updated) {

    }
}
