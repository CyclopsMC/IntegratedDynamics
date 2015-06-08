package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

/**
 * An element that can be used inside parts to access a specific aspect of something to write.
 * @author rubensworks
 */
public interface IAspectWrite<V extends IValue, T extends IValueType<V>> extends IAspect<V, T> {

}
