package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A cluster for a collection of path elements.
 * @author rubensworks
 */
@Data
public class Cluster implements Collection<ISidedPathElement>, INBTSerializable {

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

    @Override
    public CompoundNBT toNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT list = new ListNBT();

        for(ISidedPathElement e : elements) {
            CompoundNBT elementTag = new CompoundNBT();
            elementTag.putString("dimension", e.getPathElement().getPosition().getDimension().getRegistryName().toString());
            elementTag.putLong("pos", e.getPathElement().getPosition().getBlockPos().toLong());
            if (e.getSide() != null) {
                elementTag.putInt("side", e.getSide().ordinal());
            }
            list.add(elementTag);
        }

        tag.put("list", list);
        return tag;
    }

    @Override
    public void fromNBT(CompoundNBT tag) {
        ListNBT list = tag.getList("list", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < list.size(); i++) {
            CompoundNBT elementTag = list.getCompound(i);
            ResourceLocation dimensionId = new ResourceLocation(elementTag.getString("dimension"));
            DimensionType dimension = DimensionType.byName(dimensionId);
            BlockPos pos = BlockPos.fromLong(elementTag.getLong("pos"));
            Direction side = null;
            if (elementTag.contains("side", Constants.NBT.TAG_INT)) {
                side = Direction.values()[elementTag.getInt("side")];
            }

            if (dimension == null) {
                IntegratedDynamics.clog(Level.WARN, String.format("Skipped loading part from a network at the " +
                        "invalid dimension id %s.", dimensionId));
            } else {
                World world = ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
                IPathElement pathElement = TileHelpers.getCapability(world, pos, side, PathElementConfig.CAPABILITY).orElse(null);
                if(pathElement == null) {
                    IntegratedDynamics.clog(Level.WARN, String.format("Skipped loading part from a network at " +
                            "position %s in world %s because it has no valid path element.", pos, dimensionId));
                } else {
                    elements.add(SidedPathElement.of(pathElement, side));
                }
            }
        }
    }
}
