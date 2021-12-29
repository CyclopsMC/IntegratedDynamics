package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Interface for redstone reading the component.
 * @author rubensworks
 */
public interface IReadRedstoneComponent {

    public void setAllowRedstoneInput(PartTarget target, boolean allow);

    public LazyOptional<IDynamicRedstone> getDynamicRedstoneBlock(DimPos dimPos, Direction side);

}
