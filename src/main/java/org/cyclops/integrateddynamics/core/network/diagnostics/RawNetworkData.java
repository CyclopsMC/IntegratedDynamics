package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

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

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("killed", killed);
        tag.putInt("id", id);
        tag.putLong("cables", cables);

        ListTag listParts = new ListTag();
        for (RawPartData part : parts) {
            listParts.add(part.toNbt());
        }
        tag.put("parts", listParts);

        ListTag listObservers = new ListTag();
        for (RawObserverData observer : observers) {
            listObservers.add(observer.toNbt());
        }
        tag.put("observers", listObservers);

        return tag;
    }

    public static RawNetworkData fromNbt(CompoundTag tag) {
        List<RawPartData> parts = Lists.newArrayList();
        ListTag listParts = tag.getList("parts", Tag.TAG_COMPOUND);
        for (int i = 0; i < listParts.size(); i++) {
            CompoundTag partTag = listParts.getCompound(i);
            parts.add(RawPartData.fromNbt(partTag));
        }

        List<RawObserverData> observers = Lists.newArrayList();
        ListTag listObservers = tag.getList("observers", Tag.TAG_COMPOUND);
        for (int i = 0; i < listObservers.size(); i++) {
            CompoundTag observerTag = listObservers.getCompound(i);
            observers.add(RawObserverData.fromNbt(observerTag));
        }

        return new RawNetworkData(tag.getBoolean("killed"), tag.getInt("id"),
                tag.getInt("cables"), parts, observers);
    }

}
