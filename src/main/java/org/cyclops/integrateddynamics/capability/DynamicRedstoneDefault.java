package org.cyclops.integrateddynamics.capability;

import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Default implementation of {@link IDynamicRedstone}.
 * @author rubensworks
 */
public class DynamicRedstoneDefault implements IDynamicRedstone {

    @Override
    public void setRedstoneLevel(int level) {

    }

    @Override
    public int getRedstoneLevel() {
        return 0;
    }

    @Override
    public void setAllowRedstoneInput(boolean allow) {

    }

    @Override
    public boolean isAllowRedstoneInput() {
        return false;
    }
}
