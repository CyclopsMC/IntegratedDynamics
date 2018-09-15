package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.api.network.IPartPosIteratorHandler;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An {@link IPartPosIteratorHandler} that returns the given iterator unchanged.
 * @author rubensworks
 */
public class PartPosIteratorHandlerDummy implements IPartPosIteratorHandler {

    public static final PartPosIteratorHandlerDummy INSTANCE = new PartPosIteratorHandlerDummy();

    @Override
    public Iterator<PartPos> handleIterator(Supplier<Iterator<PartPos>> iteratorSupplier, int channel) {
        return iteratorSupplier.get();
    }

    @Override
    public IPartPosIteratorHandler clone() {
        return this;
    }
}
