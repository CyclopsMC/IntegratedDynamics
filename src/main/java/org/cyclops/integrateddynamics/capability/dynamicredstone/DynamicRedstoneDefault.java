package org.cyclops.integrateddynamics.capability.dynamicredstone;

import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Default implementation of {@link IDynamicRedstone}.
 * @author rubensworks
 */
public class DynamicRedstoneDefault implements IDynamicRedstone {

    @Override
    public void setRedstoneLevel(int level, boolean direct) {

    }

    @Override
    public int getRedstoneLevel() {
        return 0;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public void setAllowRedstoneInput(boolean allow) {

    }

    @Override
    public boolean isAllowRedstoneInput() {
        return false;
    }

    @Override
    public void setLastPulseValue(int value) {

    }

    @Override
    public int getLastPulseValue() {
        return 0;
    }
}
