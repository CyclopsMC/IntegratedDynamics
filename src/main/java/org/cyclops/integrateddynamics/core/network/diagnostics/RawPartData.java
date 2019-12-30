package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author rubensworks
 */
@Data
public class RawPartData implements IRawData {

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
        tag.putInt("side", side.ordinal());
        tag.putString("name", name);
        tag.putLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawPartData fromNbt(CompoundNBT tag) {
        return new RawPartData(DimensionType.byName(new ResourceLocation(tag.getString("dimension"))), BlockPos.fromLong(tag.getLong("pos")),
                Direction.values()[tag.getInt("side")], tag.getString("name"), tag.getLong("last20TicksDurationNs"));
    }

}
