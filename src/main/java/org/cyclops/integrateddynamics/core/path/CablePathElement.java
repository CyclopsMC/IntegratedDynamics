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
import org.cyclops.integrateddynamics.block.ICableConnectable;

import java.util.Set;

/**
 * A path element in the form of a cable.
 * @author rubensworks
 */
@Data
public class CablePathElement implements IPathElement<CablePathElement> {

    private final ICableConnectable cable;
    private final DimPos position;

    @Override
    public Set<CablePathElement> getReachableElements() {
        Set<CablePathElement> elements = Sets.newHashSet();
        World world = getPosition().getWorld();
        BlockPos pos = getPosition().getBlockPos();
        for(EnumFacing side : EnumFacing.VALUES) {
            if(cable.isConnected(world, pos, side)) {
                Block block = world.getBlockState(pos).getBlock();
                if(!(block instanceof ICableConnectable)) {
                    IntegratedDynamics.clog(Level.ERROR, String.format("The position at %s was incorrectly marked " +
                            "as reachable as cable by %s.", pos, getCable()));
                } else {
                    elements.add(((ICableConnectable<CablePathElement>) block).createPathElement(world, pos.offset(side)));
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
    public int compareTo(CablePathElement o) {
        if(cable.hashCode() == o.cable.hashCode()) {
            return position.compareTo(o.position);
        }
        return Integer.compare(cable.hashCode(), o.hashCode());
    }
}
