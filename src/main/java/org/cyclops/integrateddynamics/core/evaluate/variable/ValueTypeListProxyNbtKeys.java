package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Optional;

/**
 * A list of NBT keys.
 */
public class ValueTypeListProxyNbtKeys extends ValueTypeListProxyBase<ValueTypeString, ValueTypeString.ValueString> {

    private final Optional<Tag> tag;

    public ValueTypeListProxyNbtKeys(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_KEYS.getName(), ValueTypes.STRING);
        this.tag = tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return tag
                .map(t -> t instanceof CompoundTag ? ((CompoundTag) t).keySet().size() : 0)
                .orElse(0);
    }

    @Override
    public ValueTypeString.ValueString get(int index) throws EvaluationException {
        if (index < getLength()) {
            return ValueTypeString.ValueString.of(Iterables.get(((CompoundTag) tag.get()).keySet(), index));
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<ValueTypeString, ValueTypeString.ValueString, ValueTypeListProxyNbtKeys> {

        @Override
        public Identifier getName() {
            return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nbt.keys");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyNbtKeys value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            value.tag.ifPresent(inbt -> valueOutput.store("tag", ExtraCodecs.NBT, inbt));
        }

        @Override
        protected ValueTypeListProxyNbtKeys deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return new ValueTypeListProxyNbtKeys(valueInput.read("tag", ExtraCodecs.NBT));
        }
    }
}
