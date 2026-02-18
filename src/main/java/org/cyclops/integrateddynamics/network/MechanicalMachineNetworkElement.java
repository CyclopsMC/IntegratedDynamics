package org.cyclops.integrateddynamics.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.network.NetworkElementBase;

import java.util.Objects;

/**
 * Network element for mechanical machines.
 * @author rubensworks
 */
public class MechanicalMachineNetworkElement extends NetworkElementBase {

    private final DimPos pos;

    public MechanicalMachineNetworkElement(DimPos pos) {
        this.pos = pos;
    }

    public DimPos getPos() {
        return pos;
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
        if(o instanceof MechanicalMachineNetworkElement) {
            return getPos().compareTo(((MechanicalMachineNetworkElement) o).getPos());
        }
        return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }

    @Override
    public boolean isLoaded() {
        return INetworkElement.shouldTick(this.getPos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MechanicalMachineNetworkElement that = (MechanicalMachineNetworkElement) o;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }
}
