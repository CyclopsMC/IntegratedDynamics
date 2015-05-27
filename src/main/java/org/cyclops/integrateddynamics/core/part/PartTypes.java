package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.part.PartTypeRedstoneReader;

/**
 * Collection of parts types.
 * @author rubensworks
 */
public final class PartTypes {

    public static final EnumPartType REDSTONE_READER = EnumPartType.create("redstoneReader", new PartTypeRedstoneReader());

}
