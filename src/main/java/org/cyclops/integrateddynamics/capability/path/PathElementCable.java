package org.cyclops.integrateddynamics.capability.path;

import com.google.common.collect.Sets;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import java.util.Set;

/**
 * Implementation of {@link IPathElement} for cables.
 * @author rubensworks
 */
public abstract class PathElementCable extends PathElementDefault {

    protected abstract ICable getCable();

    @Override
    public Set<IPathElement> getReachableElements() {
        Set<IPathElement> elements = Sets.newHashSet();
        World world = getPosition().getWorld();
        BlockPos pos = getPosition().getBlockPos();
        for(EnumFacing side : EnumFacing.VALUES) {
            if(getCable().isConnected(side)) {
                BlockPos posOffset = pos.offset(side);
                IPathElement pathElement = TileHelpers.getCapability(world, posOffset, PathElementConfig.CAPABILITY);
                if(pathElement == null) {
                    IntegratedDynamics.clog(Level.ERROR, String.format("The position at %s was incorrectly marked " +
                            "as reachable as path element by %s at %s side %s.", posOffset, getCable(), pos, side));
                } else {
                    elements.add(pathElement);
                }
            }
        }
        return elements;
    }
}
