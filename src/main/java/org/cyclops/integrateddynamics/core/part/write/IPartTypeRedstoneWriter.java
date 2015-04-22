package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;

/**
 * A part that can write redstone levels.
 * @author rubensworks
 */
public interface IPartTypeRedstoneWriter<P extends IPartTypeWriter<P, S>, S extends IPartState<P>> extends IPartTypeWriter<P, S> {

    /**
     * Set the redstone level for given container.
     * @param partContainer The container to apply to.
     * @param side          The side this part is on.
     * @param level The level to set the redstone output.
     */
    public void setRedstoneLevel(IPartContainer partContainer, EnumFacing side, int level);

}
