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
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.VariableFacadePredicateTyped;
import org.cyclops.integrateddynamics.core.evaluate.variable.VariablePredicateTyped;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Arrays;
import java.util.List;
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

    public static final Codec<EntityType<? extends Entity>> ENTITY_TYPE = Codec.STRING.xmap(
            name -> {
                try {
                    return BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name));
                } catch (ResourceLocationException e) {
                    throw new JsonSyntaxException("Invalid entity type name '" + name + "'");
                }
            }, (entityType) -> BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());

    public static final Codec<ValuePredicate> VALUE = xorCommonList(Arrays.asList(
            RecordCodecBuilder.<ValueTypeOperator.ValueOperatorPredicate>create(
                    builder -> builder.group(
                                    staticTypeField("operator"),
                                    ExtraCodecs.strictOptionalField(OPERATOR, "operator").forGetter(ValueTypeOperator.ValueOperatorPredicate::getOperator)
                            )
                            .apply(builder, (type, operator) -> new ValueTypeOperator.ValueOperatorPredicate(operator))
            ),
            RecordCodecBuilder.<ValueObjectTypeItemStack.ValueItemStackPredicate>create(
                    builder -> builder.group(
                                    staticTypeField("itemstack"),
                                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(ValueObjectTypeItemStack.ValueItemStackPredicate::getItemPredicate)
                            )
                            .apply(builder, (type, itemPredicate) -> new ValueObjectTypeItemStack.ValueItemStackPredicate(itemPredicate))
            ),
            RecordCodecBuilder.<ValueObjectTypeEntity.ValueEntityPredicate>create(
                    builder -> builder.group(
                                    staticTypeField("entity"),
                                    ExtraCodecs.strictOptionalField(ENTITY_TYPE, "entity").forGetter(ValueObjectTypeEntity.ValueEntityPredicate::getEntityType)
                            )
                            .apply(builder, (type, entityType) -> new ValueObjectTypeEntity.ValueEntityPredicate(entityType))
            ),
            RecordCodecBuilder.<ValueTypeList.ValueListPredicate>create(
                    builder -> builder.group(
                                    staticTypeField("list"),
                                    ExtraCodecs.strictOptionalField(Codec.BOOL, "infinite_list").forGetter(ValueTypeList.ValueListPredicate::getInfinite)
                            )
                            .apply(builder, (type, infinite) -> new ValueTypeList.ValueListPredicate(infinite))
            ),
            RecordCodecBuilder.create(
                    builder -> builder.group(
                                    staticTypeField("serialized"),
                                    VALUE_TYPE.fieldOf("value_type").forGetter(v -> (IValueType) v.getValueType().get()),
                                    ExtraCodecs.FLAT_JSON.fieldOf("value").forGetter(v -> (JsonElement) v.getValueJson().get())
                            )
                            .apply(builder, (raw, valueType, valueJson) -> {
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
    ));

    public static Codec<VariablePredicate> getVariableCodec() { // This is a function so we can achieve recursion using lazyInitializedCodec
        return xorCommonList(Arrays.asList(
                RecordCodecBuilder.<OperatorRegistry.OperatorVariablePredicate>create(
                        builder -> builder.group(
                                        staticTypeField("operator"),
                                        ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(OperatorRegistry.OperatorVariablePredicate::getValueType),
                                        ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(OperatorRegistry.OperatorVariablePredicate::getValuePredicate),
                                        ExtraCodecs.strictOptionalField(OPERATOR, "operator").forGetter(OperatorRegistry.OperatorVariablePredicate::getOperator),
                                        ExtraCodecs.strictOptionalField(ExtraCodecs.strictUnboundedMap(Codec.STRING.xmap(
                                                Integer::parseInt,
                                                integer -> Integer.toString(integer)
                                        ), ExtraCodecs.lazyInitializedCodec(Codecs::getVariableCodec)), "input").forGetter(OperatorRegistry.OperatorVariablePredicate::getInputPredicates)
                                )
                                .apply(builder, (type, valueType, value, operator, input) -> new OperatorRegistry.OperatorVariablePredicate(valueType, value, operator, input))
                ),
                RecordCodecBuilder.<AspectRegistry.AspectVariablePredicate>create(
                        builder -> builder.group(
                                        staticTypeField("aspect"),
                                        ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(AspectRegistry.AspectVariablePredicate::getValueType),
                                        ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(AspectRegistry.AspectVariablePredicate::getValuePredicate),
                                        ExtraCodecs.strictOptionalField(ASPECT, "aspect").forGetter(AspectRegistry.AspectVariablePredicate::getAspect)
                                )
                                .apply(builder, (type, valueType, value, aspect) -> new AspectRegistry.AspectVariablePredicate(valueType, value, aspect))
                ),
                RecordCodecBuilder.create(
                        builder -> builder.group(
                                        staticTypeField("value_type"),
                                        ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(VariablePredicate::getValueType),
                                        ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(VariablePredicate::getValuePredicate)
                                )
                                .apply(builder, (type, valueType, valuePredicate) -> new VariablePredicate<>(IVariable.class, valueType, valuePredicate))
                ),
                RecordCodecBuilder.<VariablePredicateTyped>create(
                        builder -> builder.group(
                                        ExtraCodecs.validate(
                                                Codec.STRING,
                                                type -> IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandler(new ResourceLocation(type)) == null ?
                                                        DataResult.error(() -> "Variable facade predicate is expected to have as 'type' one of: " + String.join(", ", IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandlerNames())) :
                                                        DataResult.success(type)
                                        ).fieldOf("type").forGetter(p -> p.getHandler().getUniqueName().toString()),
                                        ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(VariablePredicate::getValueType),
                                        ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(VariablePredicate::getValuePredicate)
                                )
                                .apply(builder, (type, valueType, valuePredicate) -> new VariablePredicateTyped(IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandler(new ResourceLocation(type)), valueType, valuePredicate))
                )
        ));
    }
    public static final Codec<VariablePredicate> VARIABLE = getVariableCodec();

    public static final Codec<VariableFacadePredicate> VARIABLE_FACADE = xorCommonList(Arrays.asList(
            RecordCodecBuilder.<AspectRegistry.AspectVariableFacadePredicate>create(
                    builder -> builder.group(
                                    staticTypeField("aspect"),
                                    ExtraCodecs.strictOptionalField(ASPECT, "aspect").forGetter(AspectRegistry.AspectVariableFacadePredicate::getAspect)
                            )
                            .apply(builder, (type, aspect) -> new AspectRegistry.AspectVariableFacadePredicate(aspect))
            ),
            RecordCodecBuilder.<ValueTypeRegistry.ValueTypeVariableFacadePredicate>create(
                    builder -> builder.group(
                                    staticTypeField("value_type"),
                                    ExtraCodecs.strictOptionalField(VALUE_TYPE, "value_type").forGetter(ValueTypeRegistry.ValueTypeVariableFacadePredicate::getValueType),
                                    ExtraCodecs.strictOptionalField(VALUE, "value").forGetter(ValueTypeRegistry.ValueTypeVariableFacadePredicate::getValuePredicate)
                            )
                            .apply(builder, (type, valueType, value) -> new ValueTypeRegistry.ValueTypeVariableFacadePredicate(valueType, value))
            ),
            RecordCodecBuilder.<VariableFacadePredicateTyped>create(
                    builder -> builder.group(
                                    ExtraCodecs.validate(
                                            Codec.STRING,
                                            type -> IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandler(new ResourceLocation(type)) == null ?
                                                    DataResult.error(() -> "Variable facade predicate is expected to have as 'type' one of: " + String.join(", ", IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandlerNames())) :
                                                    DataResult.success(type)
                                    ).fieldOf("type").forGetter(p -> p.getHandler().getUniqueName().toString())
                            )
                            .apply(builder, (type) -> new VariableFacadePredicateTyped(IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).getHandler(new ResourceLocation(type))))
            ),
            Codec.unit(new VariableFacadePredicate<>(IVariableFacade.class))
    ));

    public static <T> RecordCodecBuilder<T, String> staticTypeField(String value) {
        return ExtraCodecs.validate(
                Codec.STRING,
                type -> !value.equals(type) ?
                        DataResult.error(() -> "Variable facade predicate is expected to have 'type': '" + value + "'") :
                        DataResult.success(type)
        ).fieldOf("type").forGetter(p -> value);
    }

    public static <X> Codec<X> xorCommonList(List<Codec<? extends X>> codecs) {
        Codec<X> codec = (Codec<X>) codecs.get(0);
        for (Codec<? extends X> codecEntry : codecs.subList(1, codecs.size())) {
            codec = xorCommon(codec, codecEntry);
        }
        return codec;
    }

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
