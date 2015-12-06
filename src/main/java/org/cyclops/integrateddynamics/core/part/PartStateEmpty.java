package org.cyclops.integrateddynamics.core.part;

/**
 * Dummy part state for parts that should not persist their state.
 * @author rubensworks
 */
public class PartStateEmpty<P extends IPartType> extends PartStateBase<P> {
    @Override
    public Class<? extends PartStateEmpty> getPartStateClass() {
        return PartStateEmpty.class;
    }
}
