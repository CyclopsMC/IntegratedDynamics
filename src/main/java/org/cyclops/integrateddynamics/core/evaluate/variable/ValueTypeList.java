package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeListLPElement;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Value type with values that are strings.
 * @author rubensworks
 */
public class ValueTypeList extends ValueObjectTypeBase<ValueTypeList.ValueList> {

    public static final int MAX_RENDER_LINES = 20;

    public ValueTypeList() {
        super("list", Helpers.RGBToInt(175, 3, 1), ChatFormatting.DARK_RED, ValueTypeList.ValueList.class);
    }

    @Override
    public ValueList getDefault() {
        return ValueList.ofList(ValueTypes.CATEGORY_ANY, Collections.<IValue>emptyList());
    }

    @Override
    public MutableComponent toCompactString(ValueList value) {
        return value.getRawValue().toCompactString();
    }

    @Override
    public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, ValueList value) {
        try {
            return ValueTypeListProxyFactories.REGISTRY.serialize(valueDeseralizationContext, value.getRawValue());
        } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
            e.printStackTrace();
        }
        return new CompoundTag();
    }

    @Override
    public Component canDeserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        try {
            IValueTypeListProxy<IValueType<IValue>, IValue> proxy = ValueTypeListProxyFactories.REGISTRY.deserialize(valueDeseralizationContext, value);
            return null;
        } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
            return Component.translatable(e.getMessage());
        }
    }

    @Override
    public ValueList deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        if (!(value.getId() == Tag.TAG_END || (value.getId() == Tag.TAG_COMPOUND && ((CompoundTag) value).isEmpty()))) {
            try {
                IValueTypeListProxy<IValueType<IValue>, IValue> proxy = ValueTypeListProxyFactories.REGISTRY.deserialize(valueDeseralizationContext, value);
                return ValueList.ofFactory(proxy);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                e.printStackTrace();
            }
        }
        return getDefault();
    }

    @Override
    public ValueList materialize(ValueList value) throws EvaluationException {
        IValueTypeListProxy<IValueType<IValue>, IValue> list = value.getRawValue();
        if (list.isInfinite()) {
            return ValueList.ofList(list.getValueType(), Lists.newArrayList(list.get(0)));
        }
        List<IValue> values = ImmutableList.copyOf(list);
        return ValueList.ofList(list.getValueType(), values);
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeListLPElement();
    }

    @ToString
    public static class ValueList<T extends IValueType<V>, V extends IValue> extends ValueBase {

        private final IValueTypeListProxy<T, V> value;

        private ValueList(IValueTypeListProxy<T, V> value) {
            super(ValueTypes.LIST);
            this.value = value;
        }

        public static <T extends IValueType<V>, V extends IValue> ValueList ofList(T valueType, List<V> values) {
            return new ValueList<>(new ValueTypeListProxyMaterialized<>(valueType, values));
        }

        public static <V extends IValue> ValueList ofAll(V... values) {
            return values.length == 0 ? ValueTypes.LIST.getDefault() : ofList(values[0].getType(), ImmutableList.copyOf(values));
        }

        public static <V extends IValue> ValueList ofAll(IValueType type, V... values) {
            return ofList(type, ImmutableList.copyOf(values));
        }

        public static <T extends IValueType<V>, V extends IValue> ValueList ofFactory(IValueTypeListProxy<T, V> proxy) {
            return new ValueList<>(proxy);
        }

        public IValueTypeListProxy<T, V> getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueList && ((ValueList) o).value.equals(this.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

    }

    public static class ListFactoryIterator<T extends IValueType<V>, V extends IValue> implements Iterator<V> {

        private final IValueTypeListProxy<T, V> value;
        private int index = 0;
        private int length;

        public ListFactoryIterator(IValueTypeListProxy<T, V> value) {
            this.value = value;
            try {
                this.length = this.value.getLength();
            } catch (EvaluationException e) {
                this.length = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return index < length;
        }

        @Override
        public V next() {
            try {
                return value.get(index++);
            } catch (EvaluationException e) {
                e.printStackTrace();
                return value.getValueType().getDefault();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    public static class ValueListPredicate extends ValuePredicate<ValueList> {

        private final Optional<Boolean> infinite;

        public ValueListPredicate(Optional<Boolean> infinite) {
            super(Optional.of(ValueTypes.LIST), Optional.empty(), Optional.empty());
            this.infinite = infinite;
        }

        public Optional<Boolean> getInfinite() {
            return infinite;
        }

        @Override
        protected boolean testTyped(ValueList value) {
            return super.testTyped(value)
                    && (infinite.isEmpty() || (value.getRawValue().isInfinite() == infinite.get()));
        }
    }

}
