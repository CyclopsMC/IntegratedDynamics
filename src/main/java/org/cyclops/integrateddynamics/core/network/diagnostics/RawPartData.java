package org.cyclops.integrateddynamics.core.network.diagnostics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * @author rubensworks
 */
public class RawPartData implements IRawData {

    private final ResourceKey<Level> dimension;
    private final BlockPos pos;
    private final Direction side;
    private final String name;
    private final long last20TicksDurationNs;

    public RawPartData(ResourceKey<Level> dimension, BlockPos pos, Direction side, String name, long last20TicksDurationNs) {
        this.dimension = dimension;
        this.pos = pos;
        this.side = side;
        this.name = name;
        this.last20TicksDurationNs = last20TicksDurationNs;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }

    public String getName() {
        return name;
    }

    public long getLast20TicksDurationNs() {
        return last20TicksDurationNs;
    }

    @Override
    public String toString() {
        return String.format("%s: %s,%s,%s,%s (%s)", name, pos.getX(), pos.getY(), pos.getZ(), side, dimension.identifier());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension.identifier().toString());
        tag.putLong("pos", pos.asLong());
        tag.putInt("side", side.ordinal());
        tag.putString("name", name);
        tag.putLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawPartData fromNbt(CompoundTag tag) {
        return new RawPartData(
                ResourceKey.create(Registries.DIMENSION, Identifier.parse(tag.getString("dimension").orElseThrow())),
                BlockPos.of(tag.getLong("pos").orElseThrow()),
                Direction.values()[tag.getInt("side").orElseThrow()],
                tag.getString("name").orElseThrow(),
                tag.getLong("last20TicksDurationNs").orElseThrow()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawPartData that = (RawPartData) o;
        return last20TicksDurationNs == that.last20TicksDurationNs
                && Objects.equals(dimension, that.dimension)
                && Objects.equals(pos, that.pos)
                && Objects.equals(side, that.side)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, pos, side, name, last20TicksDurationNs);
    }

}
