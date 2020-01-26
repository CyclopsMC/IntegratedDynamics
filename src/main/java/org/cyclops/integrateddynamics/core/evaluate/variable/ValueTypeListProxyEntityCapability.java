package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;

/**
 * A list proxy for a capability of an entity.
 */
public abstract class ValueTypeListProxyEntityCapability<C, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyEntityBase<T, V> {

    private final Capability<C> capability;
    private Direction side;

    public ValueTypeListProxyEntityCapability(ResourceLocation name, T valueType, World world, Entity entity,
                                              Capability<C> capability, @Nullable Direction side) {
        super(name, valueType, world, entity);
        this.capability = capability;
        this.side = side;
    }

    protected LazyOptional<C> getCapability() {
        Entity e = getEntity();
        if(e != null) {
            return e.getCapability(this.capability, this.side);
        }
        return LazyOptional.empty();
    }

    @Override
    public void writeGeneratedFieldsToNBT(CompoundNBT tag) {
        super.writeGeneratedFieldsToNBT(tag);
        if (side != null) {
            tag.putInt("side", side.ordinal());
        }
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundNBT tag) {
        super.readGeneratedFieldsFromNBT(tag);
        if (tag.contains("side", Constants.NBT.TAG_INT)) {
            this.side = Direction.values()[tag.getInt("side")];
        }
    }
}
