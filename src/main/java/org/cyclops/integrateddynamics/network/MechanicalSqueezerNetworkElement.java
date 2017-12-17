package org.cyclops.integrateddynamics.network;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.NetworkElementBase;

/**
 * Network element for mechanical squeezers.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class MechanicalSqueezerNetworkElement extends NetworkElementBase {

    private final DimPos pos;

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
        if(o instanceof MechanicalSqueezerNetworkElement) {
            return getPos().compareTo(((MechanicalSqueezerNetworkElement) o).getPos());
        }
        return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
    }

}
