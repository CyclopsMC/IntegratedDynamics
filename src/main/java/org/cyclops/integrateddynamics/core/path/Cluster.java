package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
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
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for(ISidedPathElement e : elements) {
            CompoundTag elementTag = new CompoundTag();
            elementTag.putString("dimension", e.getPathElement().getPosition().getLevel());
            elementTag.putLong("pos", e.getPathElement().getPosition().getBlockPos().asLong());
            if (e.getSide() != null) {
                elementTag.putInt("side", e.getSide().ordinal());
            }
            list.add(elementTag);
        }

        tag.put("list", list);
        return tag;
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        ListTag list = tag.getList("list", Tag.TAG_COMPOUND);

        for(int i = 0; i < list.size(); i++) {
            CompoundTag elementTag = list.getCompound(i);
            ResourceLocation dimensionId = new ResourceLocation(elementTag.getString("dimension"));
            ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimensionId);
            Level world = ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
            BlockPos pos = BlockPos.of(elementTag.getLong("pos"));
            Direction side = null;
            if (elementTag.contains("side", Tag.TAG_INT)) {
                side = Direction.values()[elementTag.getInt("side")];
            }

            if (world == null) {
                IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Skipped loading part from a network at the " +
                        "invalid dimension id %s.", dimensionId));
            } else {
                IPathElement pathElement = BlockEntityHelpers.getCapability(world, pos, side, PathElementConfig.CAPABILITY).orElse(null);
                if(pathElement == null) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, String.format("Skipped loading part from a network at " +
                            "position %s in world %s because it has no valid path element.", pos, dimensionId));
                } else {
                    elements.add(SidedPathElement.of(pathElement, side));
                }
            }
        }
    }
}
