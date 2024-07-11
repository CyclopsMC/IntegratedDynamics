package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * @author rubensworks
 */
@Data
public class RawPartData implements IRawData {

    private final ResourceKey<Level> dimension;
    private final BlockPos pos;
    private final Direction side;
    private final String name;
    private final long last20TicksDurationNs;

    @Override
    public String toString() {
        return String.format("%s: %s,%s,%s,%s (%s)", name, pos.getX(), pos.getY(), pos.getZ(), side, dimension.location());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("pos", pos.asLong());
        tag.putInt("side", side.ordinal());
        tag.putString("name", name);
        tag.putLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawPartData fromNbt(CompoundTag tag) {
        return new RawPartData(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension"))), BlockPos.of(tag.getLong("pos")),
                Direction.values()[tag.getInt("side")], tag.getString("name"), tag.getLong("last20TicksDurationNs"));
    }

}
