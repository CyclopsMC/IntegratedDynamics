package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Interface for redstone writing the component.
 * @author rubensworks
 */
public interface IWriteRedstoneComponent {

    public void setRedstoneLevel(PartTarget target, int level);

    public void deactivate(PartTarget target);

    public IDynamicRedstoneBlock getDynamicRedstoneBlock(DimPos dimPos);

}
