package org.cyclops.integrateddynamics.core.parts.write;

import org.cyclops.integrateddynamics.core.parts.IPart;
import org.cyclops.integrateddynamics.core.parts.IPartState;

/**
 * A part type for writers.
 * @author rubensworks
 */
public interface IPartWriter<P extends IPart<P, S>, S extends IPartState<P>> extends IPart<P, S> {
}
