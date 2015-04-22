package org.cyclops.integrateddynamics.core.part.write;

import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * A part type for writers.
 * @author rubensworks
 */
public interface IPartTypeWriter<P extends IPartType<P, S>, S extends IPartState<P>> extends IPartType<P, S> {
}
