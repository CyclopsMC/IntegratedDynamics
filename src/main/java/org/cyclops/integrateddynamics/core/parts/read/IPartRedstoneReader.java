package org.cyclops.integrateddynamics.core.parts.read;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.IPartState;

/**
 * A part that can read redstone levels.
 * @author rubensworks
 */
public interface IPartRedstoneReader<P extends IPartRedstoneReader<P, S>, S extends IPartState<P>> extends IPartReader<P, S> {

    /**
     * Get the redstone level from given container.
     * @param partContainer The container to apply to.
     * @param side          The side this part is on.
     * @return The level to redstone level.
     */
    public int getRedstoneLevel(IPartContainer partContainer, EnumFacing side);

}
