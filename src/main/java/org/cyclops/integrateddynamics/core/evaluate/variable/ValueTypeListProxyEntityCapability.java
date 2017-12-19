package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A list proxy for a capability of an entity.
 */
public abstract class ValueTypeListProxyEntityCapability<C, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyEntityBase<T, V> {

    private final Capability<C> capability;
    private EnumFacing side;

    public ValueTypeListProxyEntityCapability(String name, T valueType, World world, Entity entity,
                                              Capability<C> capability, @Nullable EnumFacing side) {
        super(name, valueType, world, entity);
        this.capability = capability;
        this.side = side;
    }

    protected Optional<C> getCapability() {
        Entity e = getEntity();
        if(e != null && e.hasCapability(this.capability, this.side)) {
            return Optional.of(e.getCapability(this.capability, this.side));
        }
        return Optional.empty();
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {
        super.writeGeneratedFieldsToNBT(tag);
        if (side != null) {
            tag.setInteger("side", side.ordinal());
        }
    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {
        super.readGeneratedFieldsFromNBT(tag);
        if (tag.hasKey("side", Constants.NBT.TAG_INT)) {
            this.side = EnumFacing.VALUES[tag.getInteger("side")];
        }
    }
}
