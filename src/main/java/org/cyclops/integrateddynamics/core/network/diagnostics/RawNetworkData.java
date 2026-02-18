package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Objects;

/**
 * @author rubensworks
 */
public class RawNetworkData implements IRawData {

    private final boolean killed;
    private final int id;
    private final int cables;
    private final List<RawPartData> parts;
    private final List<RawObserverData> observers;

    public RawNetworkData(boolean killed, int id, int cables, List<RawPartData> parts, List<RawObserverData> observers) {
        this.killed = killed;
        this.id = id;
        this.cables = cables;
        this.parts = parts;
        this.observers = observers;
    }

    public boolean isKilled() {
        return killed;
    }

    public int getId() {
        return id;
    }

    public int getCables() {
        return cables;
    }

    public List<RawPartData> getParts() {
        return parts;
    }

    public List<RawObserverData> getObservers() {
        return observers;
    }

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
        ListTag listParts = tag.getList("parts").orElseThrow();
        for (int i = 0; i < listParts.size(); i++) {
            CompoundTag partTag = listParts.getCompound(i).orElseThrow();
            parts.add(RawPartData.fromNbt(partTag));
        }

        List<RawObserverData> observers = Lists.newArrayList();
        ListTag listObservers = tag.getList("observers").orElseThrow();
        for (int i = 0; i < listObservers.size(); i++) {
            CompoundTag observerTag = listObservers.getCompound(i).orElseThrow();
            observers.add(RawObserverData.fromNbt(observerTag));
        }

        return new RawNetworkData(
                tag.getBoolean("killed").orElseThrow(),
                tag.getInt("id").orElseThrow(),
                tag.getInt("cables").orElseThrow(),
                parts,
                observers
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawNetworkData that = (RawNetworkData) o;
        return killed == that.killed
                && id == that.id
                && cables == that.cables
                && Objects.equals(parts, that.parts)
                && Objects.equals(observers, that.observers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(killed, id, cables, parts, observers);
    }

}
