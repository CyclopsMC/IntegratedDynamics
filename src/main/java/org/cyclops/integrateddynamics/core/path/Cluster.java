package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.api.path.SidedPathElementParams;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A cluster for a collection of path elements.
 * @author rubensworks
 */
@Data
public class Cluster implements Collection<ISidedPathElement> {

    @Delegate
    private final Set<ISidedPathElement> elements;

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public Cluster() {
        this.elements = Sets.newTreeSet();
    }

    public Cluster(TreeSet<ISidedPathElement> elements) {
        this.elements = elements;
    }

    public void fromParams(List<SidedPathElementParams> pathElements) {
        for (SidedPathElementParams pathElementParam : pathElements) {
            Identifier dimensionId = Identifier.parse(pathElementParam.dimension());
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            Level world = ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
            BlockPos pos = pathElementParam.pos();
            Direction side = pathElementParam.side().orElse(null);

            if (world == null) {
                IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Skipped loading part from a network at the " +
                        "invalid dimension id %s.", dimensionId));
            } else {
                IPathElement pathElement = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(world, pos, side, Capabilities.PathElement.BLOCK).orElse(null);
                if(pathElement == null) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Skipped loading part from a network at " +
                            "position %s in world %s because it has no valid path element.", pos, dimensionId));
                } else {
                    elements.add(SidedPathElement.of(pathElement, side));
                }
            }
        }
    }

    public List<SidedPathElementParams> toParams() {
        List<SidedPathElementParams> list = Lists.newArrayList();
        for(ISidedPathElement e : elements) {
            list.add(e.getParams());
        }
        return list;
    }
}
