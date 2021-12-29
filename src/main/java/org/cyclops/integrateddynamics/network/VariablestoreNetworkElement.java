package org.cyclops.integrateddynamics.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
public class VariablestoreNetworkElement extends TileNetworkElement<BlockEntityVariablestore>
        implements IEventListenableNetworkElement<BlockEntityVariablestore> {

    public VariablestoreNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        return NetworkHelpers.getPartNetwork(network)
                .map(partNetwork -> partNetwork.addVariableContainer(getPos()))
                .orElse(false);
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        NetworkHelpers.getPartNetwork(network)
                .ifPresent(partNetwork -> partNetwork.removeVariableContainer(getPos()));
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
    public int getConsumptionRate() {
        return GeneralConfig.variablestoreBaseConsumption;
    }

    @Override
    protected Class<BlockEntityVariablestore> getTileClass() {
        return BlockEntityVariablestore.class;
    }

    @Nullable
    @Override
    public Optional<BlockEntityVariablestore> getNetworkEventListener() {
        return getTile();
    }
}
