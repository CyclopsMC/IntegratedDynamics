package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

import java.util.Set;

/**
 * A path element in the form of a cable.
 * @author rubensworks
 */
@Data
public class CablePathElement implements ICablePathElement {

    private final ICable cable;
    private final DimPos position;

    @Override
    public Set<ICablePathElement> getReachableElements() {
        Set<ICablePathElement> elements = Sets.newHashSet();
        World world = getPosition().getWorld();
        BlockPos pos = getPosition().getBlockPos();
        for(EnumFacing side : EnumFacing.VALUES) {
            if(cable.isConnected(world, pos, side)) {
                BlockPos posOffset = pos.offset(side);
                ICable cable = CableHelpers.getInterface(world, posOffset, ICable.class);
                if(cable == null) {
                    IntegratedDynamics.clog(Level.ERROR, String.format("The position at %s was incorrectly marked " +
                            "as reachable as cable by %s.", pos, getCable()));
                } else {
                    elements.add(((ICable<ICablePathElement>) cable).createPathElement(world, posOffset));
                }
            }
        }
        return elements;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CablePathElement && compareTo((CablePathElement) o) == 0;
    }

    @Override
    public int compareTo(ICablePathElement o) {
        return position.compareTo(o.getPosition());
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
