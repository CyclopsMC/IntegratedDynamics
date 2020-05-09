package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Default implementation of {@link IFullNetworkListener}.
 * @author rubensworks
 */
public class FullNetworkListenerAdapter implements IFullNetworkListener {
    @Override
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
        return true;
    }

    @Override
    public boolean removeNetworkElementPre(INetworkElement element) {
        return true;
    }

    @Override
    public void removeNetworkElementPost(INetworkElement element) {

    }

    @Override
    public void kill() {

    }

    @Override
    public void update() {

    }

    @Override
    public boolean removePathElement(IPathElement pathElement, EnumFacing side) {
        return true;
    }

    @Override
    public void afterServerLoad() {

    }

    @Override
    public void beforeServerStop() {

    }

    @Override
    public boolean canUpdate(INetworkElement element) {
        return true;
    }

    @Override
    public void postUpdate(INetworkElement element) {

    }

    @Override
    public void onSkipUpdate(INetworkElement element) {

    }

    @Override
    public void invalidateElement(INetworkElement element) {

    }

    @Override
    public void revalidateElement(INetworkElement element) {

    }
}
