package org.cyclops.integrateddynamics.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.NetworkElementBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class EnergyBatteryNetworkElement extends NetworkElementBase {

    private final DimPos pos;

    protected Optional<BlockEntityEnergyBattery> getTile() {
        return BlockEntityHelpers.get(getPos(), BlockEntityEnergyBattery.class);
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
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {

    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        PartPos pos = PartPos.of(getPos(), null);
        boolean added = NetworkHelpers.getEnergyNetworkChecked(network).addPosition(pos, 0, IPositionedAddonsNetwork.DEFAULT_CHANNEL);
        scheduleNetworkObservation(network, pos);
        return added;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        PartPos pos = PartPos.of(getPos(), null);
        scheduleNetworkObservation(network, pos);
        NetworkHelpers.getEnergyNetworkChecked(network).removePosition(pos);
    }

    protected void scheduleNetworkObservation(INetwork network, PartPos pos) {
        NetworkHelpers.getEnergyNetwork(network)
                .ifPresent(energyNetwork -> energyNetwork.scheduleObservationForced(IPositionedAddonsNetwork.DEFAULT_CHANNEL, pos));
    }

    @Override
    public void onPreRemoved(INetwork network) {

    }

    @Override
    public void onNeighborBlockChange(@Nullable INetwork network, BlockGetter world, Block neighbourBlock,
                                      BlockPos neighbourBlockPos) {

    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getChannel() {
        return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
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
        if(o instanceof EnergyBatteryNetworkElement) {
            return getPos().compareTo(((EnergyBatteryNetworkElement) o).getPos());
        }
        return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }

}
