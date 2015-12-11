package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Interface for redstone reading the component.
 * @author rubensworks
 */
public interface IReadRedstoneComponent {

    public void setAllowRedstoneInput(PartTarget target, boolean allow);

    public IDynamicRedstoneBlock getDynamicRedstoneBlock(DimPos dimPos);

}
