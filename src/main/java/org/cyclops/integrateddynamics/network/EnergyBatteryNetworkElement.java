package org.cyclops.integrateddynamics.network;

import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import java.util.List;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
@Data
public class EnergyBatteryNetworkElement implements INetworkElement<IEnergyNetwork> {

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
    public void update(IEnergyNetwork network) {

    }

    @Override
    public void beforeNetworkKill(IEnergyNetwork network) {

    }

    @Override
    public void afterNetworkAlive(IEnergyNetwork network) {

    }

    @Override
    public void addDrops(List<ItemStack> itemStacks) {

    }

    @Override
    public boolean onNetworkAddition(IEnergyNetwork network) {
        return network.addEnergyBattery(getPos());
    }

    @Override
    public void onNetworkRemoval(IEnergyNetwork network) {
        network.removeEnergyBattery(getPos());
    }

    @Override
    public void onPreRemoved(IEnergyNetwork network) {

    }

    @Override
    public void onNeighborBlockChange(IEnergyNetwork network, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public int compareTo(INetworkElement o) {
        if(o instanceof EnergyBatteryNetworkElement) {
            return getPos().compareTo(((EnergyBatteryNetworkElement) o).getPos());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

}
