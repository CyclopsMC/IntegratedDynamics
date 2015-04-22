package org.cyclops.integrateddynamics.core.part.read;

import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * A part type for readers.
 * @author rubensworks
 */
public interface IPartTypeReader<P extends IPartTypeReader<P, S>, S extends IPartState<P>> extends IPartType<P, S> {
}
