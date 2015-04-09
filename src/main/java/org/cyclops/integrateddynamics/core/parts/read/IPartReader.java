package org.cyclops.integrateddynamics.core.parts.read;

import org.cyclops.integrateddynamics.core.parts.IPart;
import org.cyclops.integrateddynamics.core.parts.IPartState;

/**
 * A part type for readers.
 * @author rubensworks
 */
public interface IPartReader<P extends IPartReader<P, S>, S extends IPartState<P>> extends IPart<P, S> {
}
