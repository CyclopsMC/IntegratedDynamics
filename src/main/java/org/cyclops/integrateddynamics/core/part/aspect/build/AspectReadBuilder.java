package org.cyclops.integrateddynamics.core.part.aspect.build;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBase;

import java.util.Collections;
import java.util.List;

/**
 * Immutable builder for read aspects.
 * @param <V> The value type.
 * @param <T> The value type type.
 * @param <O> The current output type for value handling.
 * @author rubensworks
 */
public class AspectReadBuilder<V extends IValue, T extends IValueType<V>, O> {

    private final T valueType;
    private final List<String> kinds;
    private final IAspectProperties defaultAspectProperties;
    private final List<IAspectValuePropagator> valuePropagators;

    private AspectReadBuilder(T valueType, List<String> kinds, IAspectProperties defaultAspectProperties, List<IAspectValuePropagator> valuePropagators) {
        this.valueType = valueType;
        this.kinds = kinds;
        this.defaultAspectProperties = defaultAspectProperties;
        this.valuePropagators = valuePropagators;
    }

    /**
     * Add the given value propagator.
     * @param valuePropagator The value propagator.
     * @param <O2> The new output type.
     * @return The new builder instance.
     */
    public <O2> AspectReadBuilder<V, T, O2> handle(IAspectValuePropagator<O, O2> valuePropagator) {
        return handle(valuePropagator, null);
    }

    /**
     * Add the given value propagator.
     * @param valuePropagator The value propagator.
     * @param kind The kind to append.
     * @param <O2> The new output type.
     * @return The new builder instance.
     */
    public <O2> AspectReadBuilder<V, T, O2> handle(IAspectValuePropagator<O, O2> valuePropagator, String kind) {
        return new AspectReadBuilder<>(
                this.valueType,
                join(this.kinds, kind),
                this.defaultAspectProperties,
                join(this.valuePropagators, valuePropagator)
        );
    }

    /**
     * Add the given kind.
     * @param kind The kind to append.
     * @return The new builder instance.
     */
    public AspectReadBuilder<V, T, O> appendKind(String kind) {
        return new AspectReadBuilder<>(
                this.valueType,
                join(this.kinds, kind),
                this.defaultAspectProperties,
                join(this.valuePropagators, null)
        );
    }

    /**
     * Set the given default aspect properties.
     * @param aspectProperties The aspect properties.
     * @return The new builder instance.
     */
    public AspectReadBuilder<V, T, O> withProperties(IAspectProperties aspectProperties) {
        return new AspectReadBuilder<>(
                this.valueType,
                join(this.kinds, null),
                aspectProperties,
                join(this.valuePropagators, null)
        );
    }

    /**
     * @return The built aspect.
     */
    public IAspectRead<V, T> build() {
        return new Built<V, T>((AspectReadBuilder<V, T, V>) this);
    }

    protected static <T> List<T> join(List<T> list, T newElement) {
        List<T> newList = Lists.newArrayListWithExpectedSize(list.size() + 1);
        newList.addAll(list);
        if(newElement != null) {
            newList.add(newElement);
        }
        return newList;
    }

    /**
     * Create a new builder for the given value type.
     * @param valueType The value type the eventual built aspect will output.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The builder instance.
     */
    public static <V extends IValue, T extends IValueType<V>> AspectReadBuilder<V, T, Pair<PartTarget, IAspectProperties>> forType(T valueType) {
        return new AspectReadBuilder<>(valueType, Lists.newArrayList(valueType.getTypeName()), null, Collections.<IAspectValuePropagator>emptyList());
    }

    private static class Built<V extends IValue, T extends IValueType<V>> extends AspectReadBase<V, T> {

        private final T valueType;
        private final List<IAspectValuePropagator> valuePropagators;

        public Built(AspectReadBuilder<V, T, V> aspectBuilder) {
            super(deriveUnlocalizedType(aspectBuilder), aspectBuilder.defaultAspectProperties);
            this.valueType = aspectBuilder.valueType;
            this.valuePropagators = aspectBuilder.valuePropagators;
        }

        protected static <V extends IValue, T extends IValueType<V>> String deriveUnlocalizedType(AspectReadBuilder<V, T, V> aspectBuilder) {
            StringBuilder sb = new StringBuilder();
            for(String kind : aspectBuilder.kinds) {
                sb.append(".");
                sb.append(kind);
            }
            return sb.toString();
        }

        @Override
        protected V getValue(PartTarget target, IAspectProperties properties) {
            Object output = Pair.of(target, properties);
            for(IAspectValuePropagator valuePropagator : valuePropagators) {
                output = valuePropagator.getOutput(output);
            }
            return (V) output;
        }

        @Override
        public T getValueType() {
            return valueType;
        }
    }

}
