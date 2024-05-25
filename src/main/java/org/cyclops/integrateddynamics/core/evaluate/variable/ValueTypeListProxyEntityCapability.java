package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A list proxy for a capability of an entity.
 */
public abstract class ValueTypeListProxyEntityCapability<C, Context, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyEntityBase<T, V> {

    private final EntityCapability<C, Context> capability;
    private Direction side;

    public ValueTypeListProxyEntityCapability(ResourceLocation name, T valueType, Level world, Entity entity,
                                              EntityCapability<C, Context> capability, @Nullable Direction side) {
        super(name, valueType, world, entity);
        this.capability = capability;
        this.side = side;
    }

    protected Optional<C> getCapability() {
        Entity e = getEntity();
        if(e != null) {
            return Optional.ofNullable(e.getCapability(this.capability, capability.contextClass() == Direction.class ? (Context) this.side : null));
        }
        return Optional.empty();
    }

    @Override
    public void writeGeneratedFieldsToNBT(CompoundTag tag) {
        super.writeGeneratedFieldsToNBT(tag);
        if (side != null) {
            tag.putInt("side", side.ordinal());
        }
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundTag tag) {
        super.readGeneratedFieldsFromNBT(tag);
        if (tag.contains("side", Tag.TAG_INT)) {
            this.side = Direction.values()[tag.getInt("side")];
        }
    }
}
