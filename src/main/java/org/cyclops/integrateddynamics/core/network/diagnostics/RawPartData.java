package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * @author rubensworks
 */
@Data
public class RawPartData implements IRawData {

    private final int dimension;
    private final BlockPos pos;
    private final EnumFacing side;
    private final String name;
    private final long lastTickDuration;

    @Override
    public String toString() {
        return String.format("%s: %s,%s,%s,%s (%s)", name, pos.getX(), pos.getY(), pos.getZ(), side, dimension);
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimension);
        tag.setLong("pos", pos.toLong());
        tag.setInteger("side", side.ordinal());
        tag.setString("name", name);
        tag.setLong("lastTickDuration", lastTickDuration);
        return tag;
    }

    public static RawPartData fromNbt(NBTTagCompound tag) {
        return new RawPartData(tag.getInteger("dimension"), BlockPos.fromLong(tag.getLong("pos")),
                EnumFacing.VALUES[tag.getInteger("side")], tag.getString("name"), tag.getLong("lastTickDuration"));
    }

}
