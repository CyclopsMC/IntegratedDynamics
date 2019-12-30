package org.cyclops.integrateddynamics.api;

import net.minecraft.util.Direction;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

/**
 * A runtime exception that can be thrown when a part is in an invalid state.
 * @author rubensworks
 */
public class PartStateException extends IllegalArgumentException {

    public PartStateException(DimPos dimPos, Direction side) {
        super(String.format("No part state for part at position %s side %s was found." +
                        "\nWorld loaded: %s\nChunk loaded: %s\nPart container: %s\nParts: %s",
                dimPos,
                side,
                ServerLifecycleHooks.getCurrentServer().getWorld(dimPos.getDimension()) != null,
                dimPos.isLoaded(),
                dimPos.isLoaded() ? PartHelpers.getPartContainer(dimPos, side) : null,
                dimPos.isLoaded() ? PartHelpers.getPartContainer(dimPos, side).map(IPartContainer::getParts).orElse(null) : null));
    }

}
