package org.cyclops.integrateddynamics.core.parts.write;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.parts.IPartContainer;
import org.cyclops.integrateddynamics.core.parts.IPartState;

/**
 * A part that can write redstone levels.
 * @author rubensworks
 */
public interface IPartRedstoneWriter<P extends IPartWriter<P, S>, S extends IPartState<P>> extends IPartWriter<P, S> {

    /**
     * Set the redstone level for given container.
     * @param partContainer The container to apply to.
     * @param side          The side this part is on.
     * @param level The level to set the redstone output.
     */
    public void setRedstoneLevel(IPartContainer partContainer, EnumFacing side, int level);

}
