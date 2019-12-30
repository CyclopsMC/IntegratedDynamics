package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
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

    public CompoundNBT toNbt() {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("killed", killed);
        tag.putInt("id", id);
        tag.putLong("cables", cables);

        ListNBT listParts = new ListNBT();
        for (RawPartData part : parts) {
            listParts.add(part.toNbt());
        }
        tag.put("parts", listParts);

        ListNBT listObservers = new ListNBT();
        for (RawObserverData observer : observers) {
            listObservers.add(observer.toNbt());
        }
        tag.put("observers", listObservers);

        return tag;
    }

    public static RawNetworkData fromNbt(CompoundNBT tag) {
        List<RawPartData> parts = Lists.newArrayList();
        ListNBT listParts = tag.getList("parts", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listParts.size(); i++) {
            CompoundNBT partTag = listParts.getCompound(i);
            parts.add(RawPartData.fromNbt(partTag));
        }

        List<RawObserverData> observers = Lists.newArrayList();
        ListNBT listObservers = tag.getList("observers", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listObservers.size(); i++) {
            CompoundNBT observerTag = listObservers.getCompound(i);
            observers.add(RawObserverData.fromNbt(observerTag));
        }

        return new RawNetworkData(tag.getBoolean("killed"), tag.getInt("id"),
                tag.getInt("cables"), parts, observers);
    }

}
