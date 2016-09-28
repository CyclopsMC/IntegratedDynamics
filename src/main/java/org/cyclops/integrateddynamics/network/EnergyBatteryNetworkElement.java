package org.cyclops.integrateddynamics.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.NetworkElementBase;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import java.util.List;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class EnergyBatteryNetworkElement extends NetworkElementBase {

    private final DimPos pos;

    protected TileEnergyBattery getTile() {
        return TileHelpers.getSafeTile(getPos().getWorld(), getPos().getBlockPos(), TileEnergyBattery.class);
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
    public void update(INetwork network) {

    }

    @Override
    public void beforeNetworkKill(INetwork network) {

    }

    @Override
    public void afterNetworkAlive(INetwork network) {

    }

    @Override
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement) {

    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        return NetworkHelpers.getEnergyNetwork(network).addEnergyBattery(PartPos.of(getPos(), null), 0);
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        NetworkHelpers.getEnergyNetwork(network).removeEnergyBattery(PartPos.of(getPos(), null));
    }

    @Override
    public void onPreRemoved(INetwork network) {

    }

    @Override
    public void onNeighborBlockChange(INetwork network, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public void setPriority(int priority) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof EnergyBatteryNetworkElement) {
            return getPos().compareTo(((EnergyBatteryNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

}
