package org.cyclops.integrateddynamics.core.part.read;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;

/**
 * A part that can read redstone levels.
 * @author rubensworks
 */
public interface IPartTypeRedstoneReader<P extends IPartTypeRedstoneReader<P, S>, S extends IPartState<P>> extends IPartTypeReader<P, S> {

    /**
     * Get the redstone level from given container.
     * @param partContainer The container to apply to.
     * @param side          The side this part is on.
     * @return The level to redstone level.
     */
    public int getRedstoneLevel(IPartContainer partContainer, EnumFacing side);

}
