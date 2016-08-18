package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

import java.util.List;

/**
 * @author rubensworks
 */
@Data
public class RawNetworkData implements IRawData {

    private final boolean killed;
    private final int id;
    private final int cables;
    private final List<RawPartData> parts;

    @Override
    public String toString() {
        return String.format("Network %s (cables: %s; elements: %s)", id, cables, parts.size());
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("killed", killed);
        tag.setInteger("id", id);
        tag.setLong("cables", cables);
        NBTTagList list = new NBTTagList();
        for (RawPartData part : parts) {
            list.appendTag(part.toNbt());
        }
        tag.setTag("parts", list);
        return tag;
    }

    public static RawNetworkData fromNbt(NBTTagCompound tag) {
        List<RawPartData> parts = Lists.newArrayList();
        NBTTagList list = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound partTag = list.getCompoundTagAt(i);
            parts.add(RawPartData.fromNbt(partTag));
        }
        return new RawNetworkData(tag.getBoolean("killed"), tag.getInteger("id"),
                tag.getInteger("cables"), parts);
    }

}
