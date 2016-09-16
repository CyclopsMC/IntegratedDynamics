package org.cyclops.integrateddynamics.capability.cable;

import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;

/**
 * Default implementation of {@link ICableFakeable}.
 * @author rubensworks
 */
public abstract class CableFakeableDefault implements ICableFakeable {

    private boolean real = true;

    @Override
    public boolean isRealCable() {
        return real;
    }

    @Override
    public void setRealCable(boolean real) {
        this.real = real;
        sendUpdate();
    }

    protected abstract void sendUpdate();
}
