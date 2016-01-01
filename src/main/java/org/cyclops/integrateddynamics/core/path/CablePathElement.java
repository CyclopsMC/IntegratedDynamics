package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;

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
                Block block = world.getBlockState(posOffset).getBlock();
                if(!(block instanceof ICable)) {
                    IntegratedDynamics.clog(Level.ERROR, String.format("The position at %s was incorrectly marked " +
                            "as reachable as cable by %s.", pos, getCable()));
                } else {
                    elements.add(((ICable<ICablePathElement>) block).createPathElement(world, posOffset));
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
        if(cable.hashCode() == o.getCable().hashCode()) {
            return position.compareTo(o.getPosition());
        }
        return Integer.compare(cable.hashCode(), o.getCable().hashCode());
    }

    @Override
    public int hashCode() {
        int result = cable.hashCode();
        result = 31 * result + position.hashCode();
        return result;
    }
}
