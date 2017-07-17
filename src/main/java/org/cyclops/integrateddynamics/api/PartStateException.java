package org.cyclops.integrateddynamics.api;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.DimensionManager;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

/**
 * A runtime exception that can be thrown when a part is in an invalid state.
 * @author rubensworks
 */
public class PartStateException extends IllegalArgumentException {

    public PartStateException(DimPos dimPos, EnumFacing side) {
        super(String.format("No part state for part at position %s side %s was found." +
                        "\nWorld loaded: %s\nChunk loaded: %s\nPart container: %s\nParts: %s",
                dimPos,
                side,
                DimensionManager.getWorld(dimPos.getDimensionId()) != null,
                dimPos.isLoaded(),
                dimPos.isLoaded() ? PartHelpers.getPartContainer(dimPos) : null,
                dimPos.isLoaded() && PartHelpers.getPartContainer(dimPos) != null
                        ? PartHelpers.getPartContainer(dimPos).getParts() : null));
    }

}
