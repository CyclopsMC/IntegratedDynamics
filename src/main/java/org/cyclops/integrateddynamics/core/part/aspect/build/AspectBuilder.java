package org.cyclops.integrateddynamics.core.part.aspect.build;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBase;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBase;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable builder for aspects.
 * @param <V> The value type.
 * @param <T> The value type type.
 * @param <O> The current output type for value handling.
 * @author rubensworks
 */
public class AspectBuilder<V extends IValue, T extends IValueType<V>, O> {

    private final boolean read;
    private final T valueType;
    private final List<String> kinds;
    private final IAspectProperties defaultAspectProperties;
    private final List<IAspectValuePropagator> valuePropagators;
    private final List<IAspectWriteActivator> writeActivators;
    private final List<IAspectWriteDeactivator> writeDeactivators;
    private final ModBase mod;
    private final ModBase modGui;
    private final List<IAspectUpdateListener.Before> beforeUpdateListeners;
    private final List<IAspectUpdateListener.After> afterUpdateListeners;
    private final AspectUpdateType updateType;

    private AspectBuilder(boolean read, T valueType, List<String> kinds, IAspectProperties defaultAspectProperties,
                          List<IAspectValuePropagator> valuePropagators, List<IAspectWriteActivator> writeActivators,
                          List<IAspectWriteDeactivator> writeDeactivators, ModBase mod, ModBase modGui,
                          List<IAspectUpdateListener.Before> beforeUpdateListeners, List<IAspectUpdateListener.After> afterUpdateListeners,
                          AspectUpdateType updateType) {
        this.read = read;
        this.valueType = valueType;
        this.kinds = kinds;
        this.defaultAspectProperties = defaultAspectProperties;
        this.valuePropagators = valuePropagators;
        this.writeActivators = writeActivators;
        this.writeDeactivators = writeDeactivators;
        this.mod = Objects.requireNonNull(mod);
        this.modGui = Objects.requireNonNull(modGui);
        this.beforeUpdateListeners = beforeUpdateListeners;
        this.afterUpdateListeners = afterUpdateListeners;
        this.updateType = updateType;
    }

    /**
     * Add the given value propagator.
     * @param valuePropagator The value propagator.
     * @param <O2> The new output type.
     * @return The new builder instance.
     */
    public <O2> AspectBuilder<V, T, O2> handle(IAspectValuePropagator<O, O2> valuePropagator) {
        return handle(valuePropagator, null);
    }

    /**
     * Add the given value propagator.
     * @param valuePropagator The value propagator.
     * @param kind The kind to append.
     * @param <O2> The new output type.
     * @return The new builder instance.
     */
    public <O2> AspectBuilder<V, T, O2> handle(IAspectValuePropagator<O, O2> valuePropagator, String kind) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, kind),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, valuePropagator),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Add the given kind.
     * @param kind The kind to append.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> appendKind(String kind) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, kind),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Set the given default aspect properties.
     * @param aspectProperties The aspect properties.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> withProperties(IAspectProperties aspectProperties) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                aspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Add the given aspect activator.
     * Only applicable for writers.
     * @param activator The aspect activator callback.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> appendActivator(IAspectWriteActivator activator) {
        if(this.read) {
            throw new RuntimeException("Activators are only applicable for writers.");
        }
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, activator),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Add the given aspect deactivator.
     * Only applicable for writers.
     * @param deactivator The aspect deactivator callback.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> appendDeactivator(IAspectWriteDeactivator deactivator) {
        if(this.read) {
            throw new RuntimeException("Deactivators are only applicable for writers.");
        }
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, deactivator),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Set the mod that provides the aspect.
     * @param mod The mod.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> byMod(ModBase mod) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Set the gui mod that provides the aspect.
     * @param modGui The gui mod.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> byModGui(ModBase modGui) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * Add a before-update listener.
     * @param listener The listener.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> appendBeforeUpdateListener(IAspectUpdateListener.Before listener) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                Helpers.joinList(beforeUpdateListeners, listener),
                Helpers.joinList(afterUpdateListeners, null),
                updateType);
    }

    /**
     * Add an after-update listener.
     * @param listener The listener.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> appendAfterUpdateListener(IAspectUpdateListener.After listener) {
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                Helpers.joinList(beforeUpdateListeners, null),
                Helpers.joinList(afterUpdateListeners, listener),
                updateType);
    }

    /**
     * Set the update type of the reader aspect.
     * @return The new builder instance.
     */
    public AspectBuilder<V, T, O> withUpdateType(AspectUpdateType updateType) {
        if(!this.read) {
            throw new RuntimeException("Custom update types are only applicable to readers.");
        }
        return new AspectBuilder<>(
                this.read, this.valueType,
                Helpers.joinList(this.kinds, null),
                this.defaultAspectProperties,
                Helpers.joinList(this.valuePropagators, null),
                Helpers.joinList(writeActivators, null),
                Helpers.joinList(writeDeactivators, null),
                mod,
                modGui,
                beforeUpdateListeners,
                afterUpdateListeners,
                updateType);
    }

    /**
     * @return The built read aspect.
     */
    public IAspectRead<V, T> buildRead() {
        if(!this.read) {
            throw new RuntimeException("Tried to build a reader from a writer builder");
        }
        return new BuiltReader<V, T>((AspectBuilder<V, T, V>) this);
    }

    /**
     * @return The built write aspect.
     */
    public IAspectWrite<V, T> buildWrite() {
        if(this.read) {
            throw new RuntimeException("Tried to build a writer from a reader builder");
        }
        return new BuiltWriter<V, T>((AspectBuilder<V, T, V>) this);
    }

    /**
     * Create a new read builder for the given value type.
     * @param valueType The value type the eventual built aspect will output.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The builder instance.
     */
    public static <V extends IValue, T extends IValueType<V>> AspectBuilder<V, T, Pair<PartTarget, IAspectProperties>> forReadType(T valueType) {
        return new AspectBuilder<>(true, valueType, ImmutableList.of(valueType.getTypeName()), null,
                Collections.<IAspectValuePropagator>emptyList(), Collections.<IAspectWriteActivator>emptyList(),
                Collections.<IAspectWriteDeactivator>emptyList(), IntegratedDynamics._instance, IntegratedDynamics._instance, Lists.newArrayList(), Lists.newArrayList(), AspectUpdateType.NETWORK_TICK);
    }

    /**
     * Create a new write builder for the given value type.
     * @param valueType The value type the eventual built aspect expects.
     * @param <V> The value type.
     * @param <T> The value type type.
     * @return The builder instance.
     */
    public static <V extends IValue, T extends IValueType<V>> AspectBuilder<V, T, Triple<PartTarget, IAspectProperties, IVariable<V>>> forWriteType(T valueType) {
        return new AspectBuilder<>(false, valueType, ImmutableList.of(valueType.getTypeName()), null,
                Collections.<IAspectValuePropagator>emptyList(), Collections.<IAspectWriteActivator>emptyList(),
                Collections.<IAspectWriteDeactivator>emptyList(), IntegratedDynamics._instance, IntegratedDynamics._instance, Lists.newArrayList(), Lists.newArrayList(), AspectUpdateType.NETWORK_TICK);
    }

    private static class BuiltReader<V extends IValue, T extends IValueType<V>> extends AspectReadBase<V, T> {

        private final T valueType;
        private final List<IAspectValuePropagator> valuePropagators;
        private final List<IAspectUpdateListener.Before> beforeUpdateListeners;
        private final List<IAspectUpdateListener.After> afterUpdateListeners;

        public BuiltReader(AspectBuilder<V, T, V> aspectBuilder) {
            super(aspectBuilder.mod, aspectBuilder.modGui,
                    deriveUnlocalizedType(aspectBuilder), aspectBuilder.defaultAspectProperties,
                    aspectBuilder.updateType);
            this.valueType = aspectBuilder.valueType;
            this.valuePropagators = aspectBuilder.valuePropagators;
            this.beforeUpdateListeners = aspectBuilder.beforeUpdateListeners;
            this.afterUpdateListeners = aspectBuilder.afterUpdateListeners;
        }

        protected static <V extends IValue, T extends IValueType<V>> String deriveUnlocalizedType(AspectBuilder<V, T, V> aspectBuilder) {
            StringBuilder sb = new StringBuilder();
            for(String kind : aspectBuilder.kinds) {
                sb.append(".");
                sb.append(kind);
            }
            return sb.toString();
        }

        @Override
        protected V getValue(PartTarget target, IAspectProperties properties) throws EvaluationException {
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

        @Override
        public <P extends IPartType<P, S>, S extends IPartState<P>> void update(IPartNetwork network, P partType, PartTarget target, S state) {
            this.beforeUpdateListeners.forEach(l -> l.onUpdate(network, partType, target, state));
            super.update(network, partType, target, state);
            this.afterUpdateListeners.forEach(l -> l.onUpdate(network, partType, target, state));
        }
    }

    private static class BuiltWriter<V extends IValue, T extends IValueType<V>> extends AspectWriteBase<V, T> {

        private final T valueType;
        private final List<IAspectValuePropagator> valuePropagators;
        private final List<IAspectWriteActivator> writeActivators;
        private final List<IAspectWriteDeactivator> writeDeactivators;
        private final List<IAspectUpdateListener.Before> beforeUpdateListeners;
        private final List<IAspectUpdateListener.After> afterUpdateListeners;

        public BuiltWriter(AspectBuilder<V, T, V> aspectBuilder) {
            super(aspectBuilder.mod, aspectBuilder.modGui,
                    deriveUnlocalizedType(aspectBuilder), aspectBuilder.defaultAspectProperties);
            this.valueType = aspectBuilder.valueType;
            this.valuePropagators = aspectBuilder.valuePropagators;
            this.writeActivators = aspectBuilder.writeActivators;
            this.writeDeactivators = aspectBuilder.writeDeactivators;
            this.beforeUpdateListeners = aspectBuilder.beforeUpdateListeners;
            this.afterUpdateListeners = aspectBuilder.afterUpdateListeners;
        }

        protected static <V extends IValue, T extends IValueType<V>> String deriveUnlocalizedType(AspectBuilder<V, T, V> aspectBuilder) {
            StringBuilder sb = new StringBuilder();
            for(String kind : aspectBuilder.kinds) {
                sb.append(".");
                sb.append(kind);
            }
            return sb.toString();
        }

        @Override
        public T getValueType() {
            return valueType;
        }

        @Override
        public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target, S state, IVariable<V> variable) throws EvaluationException {
            IAspectProperties properties = hasProperties() ? getProperties(partType, target, state) : null;
            Object output = Triple.of(target, properties, variable);
            for(IAspectValuePropagator valuePropagator : valuePropagators) {
                output = valuePropagator.getOutput(output);
            }
        }

        @Override
        public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
            super.onActivate(partType, target, state);
            for (IAspectWriteActivator writeActivator : this.writeActivators) {
                writeActivator.onActivate(partType, target, state);
            }
        }

        @Override
        public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType, PartTarget target, S state) {
            super.onDeactivate(partType, target, state);
            for (IAspectWriteDeactivator writeDeactivator : this.writeDeactivators) {
                writeDeactivator.onDeactivate(partType, target, state);
            }
        }

        @Override
        public <P extends IPartType<P, S>, S extends IPartState<P>> void update(IPartNetwork network, P partType, PartTarget target, S state) {
            this.beforeUpdateListeners.forEach(l -> l.onUpdate(network, partType, target, state));
            super.update(network, partType, target, state);
            this.afterUpdateListeners.forEach(l -> l.onUpdate(network, partType, target, state));
        }
    }

}
