package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

/**
 * @author rubensworks
 */
@Data
public class RawObserverData implements IRawData {

    private final DimensionType dimension;
    private final BlockPos pos;
    private final Direction side;
    private final String name;
    private final long last20TicksDurationNs;

    @Override
    public String toString() {
        return String.format("%s: %s,%s,%s,%s (%s)", name, pos.getX(), pos.getY(), pos.getZ(), side, dimension);
    }

    public CompoundNBT toNbt() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("dimension", dimension.getRegistryName().toString());
        tag.putLong("pos", pos.toLong());
        if (side != null) {
            tag.putInt("side", side.ordinal());
        }
        tag.putString("name", name);
        tag.putLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawObserverData fromNbt(CompoundNBT tag) {
        return new RawObserverData(DimensionType.byName(new ResourceLocation(tag.getString("dimension"))), BlockPos.fromLong(tag.getLong("pos")),
                tag.contains("side") ? Direction.values()[tag.getInt("side")] : null, tag.getString("name"), tag.getLong("last20TicksDurationNs"));
    }

}
