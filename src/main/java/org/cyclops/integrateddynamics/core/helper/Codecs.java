package org.cyclops.integrateddynamics.core.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public class Codecs {

    public static final Codec<IPartType> PART_TYPE = Codec.STRING.xmap(
            name -> {
                IPartType<?, ?> partType = PartTypes.REGISTRY.getPartType(new ResourceLocation(name));
                if (partType == null) {
                    throw new JsonSyntaxException("No part type found with name: " + name);
                }
                return partType;
            }, (partType) -> partType.getUniqueName().toString());

    public static final Codec<IAspect> ASPECT = Codec.STRING.xmap(
            name -> {
                IAspect<?, ?> aspect = Aspects.REGISTRY.getAspect(new ResourceLocation(name));
                if (aspect == null) {
                    throw new JsonSyntaxException("No aspect found with name: " + name);
                }
                return aspect;
            }, (aspect) -> aspect.getUniqueName().toString());

    public static final Codec<IOperator> OPERATOR = Codec.STRING.xmap(
            name -> {
                IOperator operator = Operators.REGISTRY.getOperator(new ResourceLocation(name));
                if (operator == null) {
                    throw new JsonSyntaxException("No operator found with name: " + name);
                }
                return operator;
            }, (operator) -> operator.getUniqueName().toString());

    public static final Codec<IValueType> VALUE_TYPE = Codec.STRING.xmap(
            name -> {
                IValueType<?> valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(name));
                if (valueType == null) {
                    throw new JsonSyntaxException("Unknown value type '" + name + "', valid types are: "
                            + ValueTypes.REGISTRY.getValueTypes().stream().map(IValueType::getUniqueName).collect(Collectors.toList()));
                }
                return valueType;
            }, (valueType) -> valueType.getUniqueName().toString());

    public static final Codec<ValuePredicate> VALUE = xorCommon(
            xorCommon(
                    xorCommon(
                            xorCommon(
                                    null,
                                    RecordCodecBuilder.<ValueTypeOperator.ValueOperatorPredicate>create(
                                            builder -> builder.group(
                                                            VALUE_TYPE.fieldOf("type").forGetter(v -> v.getValueType().get()),
                                                            ExtraCodecs.strictOptionalField(OPERATOR, "operator").forGetter(ValueTypeOperator.ValueOperatorPredicate::getOperator)
                                                    )
                                                    .apply(builder, (type, operator) -> {
                                                        if ("operator".equals(type)) {
                                                            throw new JsonSyntaxException("Value predicate is expected to have 'type': 'operator'");
                                                        }

                                                        return new ValueTypeOperator.ValueOperatorPredicate(operator);
                                                    })
                                    )
                            ),
                            RecordCodecBuilder.<ValueObjectTypeItemStack.ValueItemStackPredicate>create(
                                    builder -> builder.group(
                                                    VALUE_TYPE.fieldOf("type").forGetter(v -> v.getValueType().get()),
                                                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(ValueObjectTypeItemStack.ValueItemStackPredicate::getItemPredicate)
                                            )
                                            .apply(builder, (type, itemPredicate) -> {
                                                if ("itemstack".equals(type)) {
                                                    throw new JsonSyntaxException("Value predicate is expected to have 'type': 'itemstack'");
                                                }

                                                return new ValueObjectTypeItemStack.ValueItemStackPredicate(itemPredicate);
                                            })
                            )
                    ),
                    RecordCodecBuilder.<ValueObjectTypeEntity.ValueEntityPredicate>create(
                            builder -> builder.group(
                                            VALUE_TYPE.fieldOf("type").forGetter(v -> v.getValueType().get()),
                                            ExtraCodecs.strictOptionalField(Codec.STRING, "entity").forGetter(ValueObjectTypeEntity.ValueEntityPredicate::getEntityTypeName)
                                    )
                                    .apply(builder, (type, entityTypeName) -> {
                                        if ("entity".equals(type)) {
                                            throw new JsonSyntaxException("Value predicate is expected to have 'type': 'entity'");
                                        }

                                        Optional<EntityType<? extends Entity>> entityType = Optional.empty();
                                        if (entityTypeName.isPresent()) {
                                            try {
                                                entityType = Optional.of(BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(entityTypeName.get())));
                                            } catch (ResourceLocationException e) {
                                                throw new JsonSyntaxException("Invalid entity type name '" + entityTypeName + "'");
                                            }
                                        }

                                        return new ValueObjectTypeEntity.ValueEntityPredicate(entityTypeName, entityType);
                                    })
                    )
            ),
            RecordCodecBuilder.create(
                    builder -> builder.group(
                                    VALUE_TYPE.fieldOf("type").forGetter(v -> (IValueType) v.getValueType().get()),
                                    ExtraCodecs.FLAT_JSON.fieldOf("value").forGetter(v -> (JsonElement) v.getValueJson().get())
                            )
                            .apply(builder, (valueType, valueJson) -> {
                                Optional<IValue> value = Optional.empty();
                                try {
                                    Tag tag = TagParser.parseTag(valueJson.toString());
                                    if (((CompoundTag) tag).contains("Primitive")) {
                                        tag = ((CompoundTag) tag).get("Primitive");
                                    }
                                    value = Optional.of(ValueHelpers.deserializeRaw(ValueDeseralizationContext.ofAllEnabled(), valueType, tag));
                                } catch (CommandSyntaxException e) {
                                    e.printStackTrace();
                                }
                                return new ValuePredicate(Optional.of(valueType), value, Optional.of(valueJson));
                            })
            )
    );

    public static final Codec<VariablePredicate> VARIABLE = xorCommon(
            xorCommon(
                    RecordCodecBuilder.<OperatorRegistry.OperatorVariablePredicate>create(
                            builder -> builder.group(
                                            Codec.STRING.fieldOf("type").forGetter(p -> "operator"),
                                            ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(OperatorRegistry.OperatorVariablePredicate::getValueType),
                                            ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(OperatorRegistry.OperatorVariablePredicate::getValuePredicate),
                                            ExtraCodecs.strictOptionalField(OPERATOR, "operator").forGetter(OperatorRegistry.OperatorVariablePredicate::getOperator),
                                            ExtraCodecs.FLAT_JSON.fieldOf("input").forGetter(OperatorRegistry.OperatorVariablePredicate::getInputJson)
                                    )
                                    .apply(builder, (type, valueType, value, operator, input) -> {
                                        if ("operator".equals(type)) {
                                            throw new JsonSyntaxException("Variable predicate is expected to have 'type': 'operator'");
                                        }
                                        return new OperatorRegistry.OperatorVariablePredicate(valueType, value, operator, input);
                                    })
                    ),
                    RecordCodecBuilder.<AspectRegistry.AspectVariablePredicate>create(
                            builder -> builder.group(
                                            Codec.STRING.fieldOf("type").forGetter(p -> "aspect"),
                                            ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(AspectRegistry.AspectVariablePredicate::getValueType),
                                            ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(AspectRegistry.AspectVariablePredicate::getValuePredicate),
                                            ExtraCodecs.strictOptionalField(ASPECT, "aspect").forGetter(AspectRegistry.AspectVariablePredicate::getAspect)
                                    )
                                    .apply(builder, (type, valueType, value, aspect) -> {
                                        if ("aspect".equals(type)) {
                                            throw new JsonSyntaxException("Variable predicate is expected to have 'type': 'aspect'");
                                        }
                                        return new AspectRegistry.AspectVariablePredicate(valueType, value, aspect);
                                    })
                    )
            ),
            RecordCodecBuilder.create(
                    builder -> builder.group(
                                    ExtraCodecs.strictOptionalField(VALUE_TYPE, "type").forGetter(VariablePredicate::getValueType),
                                    ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(VariablePredicate::getValuePredicate)
                            )
                            .apply(builder, (valueType, valuePredicate) -> new VariablePredicate<>(IVariable.class, valueType, valuePredicate))
            )
    );

    public static final Codec<VariableFacadePredicate> VARIABLE_FACADE = xorCommon(
            xorCommon(
                    RecordCodecBuilder.<AspectRegistry.AspectVariableFacadePredicate>create(
                            builder -> builder.group(
                                            Codec.STRING.fieldOf("type").forGetter(p -> "aspect"),
                                            ExtraCodecs.strictOptionalField(ASPECT, "aspect").forGetter(AspectRegistry.AspectVariableFacadePredicate::getAspect)
                                    )
                                    .apply(builder, (type, aspect) -> {
                                        if ("aspect".equals(type)) {
                                            throw new JsonSyntaxException("Variable facade predicate is expected to have 'type': 'aspect'");
                                        }
                                        return new AspectRegistry.AspectVariableFacadePredicate(aspect);
                                    })
                    ),
                    RecordCodecBuilder.<ValueTypeRegistry.ValueTypeVariableFacadePredicate>create(
                            builder -> builder.group(
                                            Codec.STRING.fieldOf("type").forGetter(p -> "value_type"),
                                            ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(ValueTypeRegistry.ValueTypeVariableFacadePredicate::getValueType),
                                            ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(ValueTypeRegistry.ValueTypeVariableFacadePredicate::getValuePredicate)
                                    )
                                    .apply(builder, (type, valueType, value) -> {
                                        if ("value_type".equals(type)) {
                                            throw new JsonSyntaxException("Variable facade predicate is expected to have 'type': 'value_type'");
                                        }
                                        return new ValueTypeRegistry.ValueTypeVariableFacadePredicate(valueType, value);
                                    })
                    )
            ),
            Codec.unit(new VariableFacadePredicate<>(IVariableFacade.class))
    );

    public static <X, F extends X, S extends X> Codec<X> xorCommon(Codec<F> p_144640_, Codec<S> p_144641_) {
        return new XorCodecCommon<>(p_144640_, p_144641_);
    }

    static record XorCodecCommon<X, F extends X, S extends X>(Codec<F> first, Codec<S> second) implements Codec<X> {
        @Override
        public <T> DataResult<Pair<X, T>> decode(DynamicOps<T> p_144679_, T p_144680_) {
            DataResult<Pair<X, T>> dataresult = this.first.decode(p_144679_, p_144680_).map(p_144673_ -> (Pair<X, T>) p_144673_);
            DataResult<Pair<X, T>> dataresult1 = this.second.decode(p_144679_, p_144680_).map(p_144673_ -> (Pair<X, T>) p_144673_);
            Optional<Pair<X, T>> optional = dataresult.result();
            Optional<Pair<X, T>> optional1 = dataresult1.result();
            if (optional.isPresent() && optional1.isPresent()) {
                return DataResult.error(
                        () -> "Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional1.get(),
                        optional.get()
                );
            } else if (optional.isPresent()) {
                return dataresult;
            } else {
                return optional1.isPresent() ? dataresult1 : dataresult.apply2((p_300790_, p_300791_) -> p_300791_, dataresult1);
            }
        }

        public <T> DataResult<T> encode(X p_144663_, DynamicOps<T> p_144664_, T p_144665_) {
            try {
                return this.first.encode((F) p_144663_, p_144664_, p_144665_);
            } catch (ClassCastException e) {
                return this.second.encode((S) p_144663_, p_144664_, p_144665_);
            }
        }

        @Override
        public String toString() {
            return "XorCodecCommon[" + this.first + ", " + this.second + "]";
        }
    }

}
