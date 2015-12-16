package org.cyclops.integrateddynamics.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

import java.util.List;

/**
 * Network element for coal generators.
 * @author rubensworks
 */
@Data
public class CoalGeneratorNetworkElement implements INetworkElement<IPartNetwork> {

    private final DimPos pos;

    protected TileCoalGenerator getTile() {
        return TileHelpers.getSafeTile(getPos().getWorld(), getPos().getBlockPos(), TileCoalGenerator.class);
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
        TileCoalGenerator tile = getTile();
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
        if(o instanceof CoalGeneratorNetworkElement) {
            return getPos().compareTo(((CoalGeneratorNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

}
