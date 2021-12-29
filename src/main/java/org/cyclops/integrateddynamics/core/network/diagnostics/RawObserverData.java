package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * @author rubensworks
 */
@Data
public class RawObserverData implements IRawData {

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
        if (side != null) {
            tag.putInt("side", side.ordinal());
        }
        tag.putString("name", name);
        tag.putLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawObserverData fromNbt(CompoundTag tag) {
        return new RawObserverData(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension"))), BlockPos.of(tag.getLong("pos")),
                tag.contains("side") ? Direction.values()[tag.getInt("side")] : null, tag.getString("name"), tag.getLong("last20TicksDurationNs"));
    }

}
