package org.cyclops.integrateddynamics.core.path;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A cluster for a collection of path elements.
 * @author rubensworks
 */
@Data
public class Cluster implements Collection<IPathElement>, INBTSerializable {

    @Delegate
    private final Set<IPathElement> elements;

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public Cluster() {
        this.elements = Sets.newTreeSet();
    }

    public Cluster(TreeSet<IPathElement> elements) {
        this.elements = elements;
    }

    @Override
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();

        for(IPathElement e : elements) {
            NBTTagCompound elementTag = new NBTTagCompound();
            elementTag.setInteger("dimension", e.getPosition().getWorld().provider.getDimension());
            elementTag.setLong("pos", e.getPosition().getBlockPos().toLong());
            list.appendTag(elementTag);
        }

        tag.setTag("list", list);
        return tag;
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("list", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());

        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound elementTag = list.getCompoundTagAt(i);
            int dimensionId = elementTag.getInteger("dimension");
            BlockPos pos = BlockPos.fromLong(elementTag.getLong("pos"));

            if(!net.minecraftforge.common.DimensionManager.isDimensionRegistered(dimensionId)) {
                IntegratedDynamics.clog(Level.WARN, String.format("Skipped loading part from a network at the " +
                        "invalid dimension id %s.", dimensionId));
            } else {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimensionId);
                IPathElement pathElement = TileHelpers.getCapability(world, pos, null, PathElementConfig.CAPABILITY);
                if(pathElement == null) {
                    IntegratedDynamics.clog(Level.WARN, String.format("Skipped loading part from a network at " +
                            "position %s in world %s because it has no valid path element.", pos, dimensionId));
                } else {
                    elements.add(pathElement);
                }
            }
        }
    }
}
