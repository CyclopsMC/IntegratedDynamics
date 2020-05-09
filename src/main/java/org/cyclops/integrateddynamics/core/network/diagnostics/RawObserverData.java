package org.cyclops.integrateddynamics.core.network.diagnostics;

import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * @author rubensworks
 */
@Data
public class RawObserverData implements IRawData {

    private final int dimension;
    private final BlockPos pos;
    private final EnumFacing side;
    private final String name;
    private final long last20TicksDurationNs;

    @Override
    public String toString() {
        return String.format("%s: %s,%s,%s,%s (%s)", name, pos.getX(), pos.getY(), pos.getZ(), side, dimension);
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimension);
        tag.setLong("pos", pos.toLong());
        if (side != null) {
            tag.setInteger("side", side.ordinal());
        }
        tag.setString("name", name);
        tag.setLong("last20TicksDurationNs", last20TicksDurationNs);
        return tag;
    }

    public static RawObserverData fromNbt(NBTTagCompound tag) {
        return new RawObserverData(tag.getInteger("dimension"), BlockPos.fromLong(tag.getLong("pos")),
                tag.hasKey("side") ? EnumFacing.VALUES[tag.getInteger("side")] : null, tag.getString("name"), tag.getLong("last20TicksDurationNs"));
    }

}
