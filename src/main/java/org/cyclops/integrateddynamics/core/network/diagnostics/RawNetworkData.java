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
    private final List<RawObserverData> observers;

    @Override
    public String toString() {
        return String.format("Network %s (cables: %s; elements: %s)", id, cables, parts.size());
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("killed", killed);
        tag.setInteger("id", id);
        tag.setLong("cables", cables);

        NBTTagList listParts = new NBTTagList();
        for (RawPartData part : parts) {
            listParts.appendTag(part.toNbt());
        }
        tag.setTag("parts", listParts);

        NBTTagList listObservers = new NBTTagList();
        for (RawObserverData observer : observers) {
            listObservers.appendTag(observer.toNbt());
        }
        tag.setTag("observers", listObservers);

        return tag;
    }

    public static RawNetworkData fromNbt(NBTTagCompound tag) {
        List<RawPartData> parts = Lists.newArrayList();
        NBTTagList listParts = tag.getTagList("parts", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for (int i = 0; i < listParts.tagCount(); i++) {
            NBTTagCompound partTag = listParts.getCompoundTagAt(i);
            parts.add(RawPartData.fromNbt(partTag));
        }

        List<RawObserverData> observers = Lists.newArrayList();
        NBTTagList listObservers = tag.getTagList("observers", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        for (int i = 0; i < listObservers.tagCount(); i++) {
            NBTTagCompound observerTag = listObservers.getCompoundTagAt(i);
            observers.add(RawObserverData.fromNbt(observerTag));
        }

        return new RawNetworkData(tag.getBoolean("killed"), tag.getInteger("id"),
                tag.getInteger("cables"), parts, observers);
    }

}
