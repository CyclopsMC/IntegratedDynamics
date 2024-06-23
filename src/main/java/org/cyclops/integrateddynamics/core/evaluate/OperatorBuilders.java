package org.cyclops.integrateddynamics.core.evaluate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Logical builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL = OperatorBuilder.forType(ValueTypes.BOOLEAN).appendKind("logical");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL_1_PREFIX = LOGICAL.inputTypes(1, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL_2 = LOGICAL.inputTypes(2, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Value propagators ---------------
    public static final IOperatorValuePropagator<Integer, IValue> PROPAGATOR_INTEGER_VALUE = ValueTypeInteger.ValueInteger::of;
    public static final IOperatorValuePropagator<Long, IValue> PROPAGATOR_LONG_VALUE = ValueTypeLong.ValueLong::of;
    public static final IOperatorValuePropagator<Boolean, IValue> PROPAGATOR_BOOLEAN_VALUE = ValueTypeBoolean.ValueBoolean::of;
    public static final IOperatorValuePropagator<Double, IValue> PROPAGATOR_DOUBLE_VALUE = ValueTypeDouble.ValueDouble::of;
    public static final IOperatorValuePropagator<String, IValue> PROPAGATOR_STRING_VALUE = ValueTypeString.ValueString::of;
    public static final IOperatorValuePropagator<Optional<Tag>, IValue> PROPAGATOR_NBT_VALUE = ValueTypeNbt.ValueNbt::of;
    public static final IOperatorValuePropagator<Optional<CompoundTag>, IValue> PROPAGATOR_NBT_COMPOUND_VALUE = opt -> ValueTypeNbt.ValueNbt.of(opt.map(t -> (Tag) t));
    public static final IOperatorValuePropagator<ResourceLocation, ValueTypeString.ValueString> PROPAGATOR_RESOURCELOCATION_MODNAME = resourceLocation -> {
        String modId = resourceLocation.getNamespace();
        Optional<? extends ModContainer> mod = ModList.get().getModContainerById(modId);
        String modName = mod
                .map(modContainer -> modContainer.getModInfo().getDisplayName())
                .orElse("Minecraft");
        return ValueTypeString.ValueString.of(modName);
    };

    // --------------- Arithmetic builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("arithmetic").conditionalOutputTypeDeriver((operator, input) -> {
        IValueType[] original = ValueHelpers.from(input);
        IValueTypeNumber[] types = new IValueTypeNumber[original.length];
        for(int i = 0; i < original.length; i++) {
            if (original[i].isCategory()) {
                // This avoids a class-cast exception in cases where we don't know the exact type.
                return original[i];
            }
            types[i] = (IValueTypeNumber) original[i];
        }
        return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
    });
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC_1_SUFFIX = ARITHMETIC.inputTypes(1, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.SUFFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC_2 = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC_2_PREFIX = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.PREFIX_2);

    // --------------- Integer builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER = OperatorBuilder.forType(ValueTypes.INTEGER).appendKind("integer");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER_1_SUFFIX = INTEGER.inputTypes(1, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.SUFFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER_2 = INTEGER.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Relational builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RELATIONAL = OperatorBuilder.forType(ValueTypes.BOOLEAN).appendKind("relational");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RELATIONAL_2 = RELATIONAL.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Binary builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY = OperatorBuilder.forType(ValueTypes.INTEGER).appendKind("binary");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY_1_PREFIX = BINARY.inputTypes(1, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY_2 = BINARY.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- String builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING = OperatorBuilder.forType(ValueTypes.STRING).appendKind("string");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING_1_PREFIX = STRING.inputTypes(1, ValueTypes.STRING).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING_2 = STRING.inputTypes(2, ValueTypes.STRING).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING_2_LONG = STRING.inputTypes(2, ValueTypes.STRING).renderPattern(IConfigRenderPattern.INFIX_LONG);

    // --------------- Double builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE = OperatorBuilder.forType(ValueTypes.DOUBLE).appendKind("double");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE_1_PREFIX = DOUBLE.inputTypes(1, ValueTypes.DOUBLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Number builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NUMBER = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("number");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NUMBER_1_PREFIX = NUMBER.inputTypes(1, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NUMBER_1_LONG = NUMBER.inputTypes(1, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);

    // --------------- Nullable builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE = OperatorBuilder.forType(ValueTypes.CATEGORY_NULLABLE).appendKind("general");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE_1_PREFIX = NULLABLE.inputTypes(1, ValueTypes.CATEGORY_NULLABLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- List builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST = OperatorBuilder.forType(ValueTypes.LIST).appendKind("list");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST_1_PREFIX = LIST.inputTypes(1, ValueTypes.LIST).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Block builders ---------------
    public static final OperatorBuilder BLOCK = OperatorBuilder.forType(ValueTypes.OBJECT_BLOCK).appendKind("block");
    public static final OperatorBuilder BLOCK_1_SUFFIX_LONG = BLOCK.inputTypes(1, ValueTypes.OBJECT_BLOCK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder BLOCK_INFIX_VERYLONG = BLOCK.inputTypes(2, ValueTypes.OBJECT_BLOCK).renderPattern(IConfigRenderPattern.INFIX_VERYLONG);
    public static final IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Optional<SoundType>> BLOCK_SOUND = input -> {
        ValueObjectTypeBlock.ValueBlock block = input.getValue(0, ValueTypes.OBJECT_BLOCK);
        if(block.getRawValue().isPresent()) {
            return Optional.of(block.getRawValue().get().getBlock().getSoundType(block.getRawValue().get()));
        }
        return Optional.empty();
    };

    // --------------- ItemStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_ITEMSTACK).appendKind("itemstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_PREFIX_LONG = ITEMSTACK.inputTypes(1, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.PREFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_SUFFIX_LONG = ITEMSTACK.inputTypes(1, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_2 = ITEMSTACK.inputTypes(2, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_2_LONG = ITEMSTACK.inputTypes(2, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.INFIX_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_INTEGER_1 = ITEMSTACK.inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.INTEGER}).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<ItemStack, IValue> FUNCTION_ITEMSTACK = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeItemStack.ValueItemStack value = input.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return value.getRawValue();
            });
    public static final IterativeFunction.PrePostBuilder<ItemStack, Integer> FUNCTION_ITEMSTACK_TO_INT =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<ItemStack, Boolean> FUNCTION_ITEMSTACK_TO_BOOLEAN =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, IValue> FUNCTION_ENERGYSTORAGEITEM = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                if(!a.getRawValue().isEmpty()) {
                    return a.getRawValue().getCapability(CapabilityEnergy.ENERGY).orElse(null);
                }
                return null;
            });
    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, Integer> FUNCTION_CONTAINERITEM_TO_INT =
            FUNCTION_ENERGYSTORAGEITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, Boolean> FUNCTION_CONTAINERITEM_TO_BOOLEAN =
            FUNCTION_ENERGYSTORAGEITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Entity builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY = OperatorBuilder.forType(ValueTypes.OBJECT_ENTITY).appendKind("entity");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY_1_SUFFIX = ENTITY.inputTypes(1, ValueTypes.OBJECT_ENTITY).renderPattern(IConfigRenderPattern.SUFFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY_1_SUFFIX_LONG = ENTITY.inputTypes(1, ValueTypes.OBJECT_ENTITY).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IterativeFunction.PrePostBuilder<Entity, IValue> FUNCTION_ENTITY = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeEntity.ValueEntity a = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
            });
    public static final IterativeFunction.PrePostBuilder<Entity, Double> FUNCTION_ENTITY_TO_DOUBLE =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<Entity, Boolean> FUNCTION_ENTITY_TO_BOOLEAN =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- FluidStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_FLUIDSTACK).appendKind("fluidstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK_1_SUFFIX_LONG = FLUIDSTACK.inputTypes(1, ValueTypes.OBJECT_FLUIDSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK_2 = FLUIDSTACK.inputTypes(2, ValueTypes.OBJECT_FLUIDSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<FluidStack, IValue> FUNCTION_FLUIDSTACK = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeFluidStack.ValueFluidStack a = input.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                return a.getRawValue();
            });
    public static final IterativeFunction.PrePostBuilder<FluidStack, Integer> FUNCTION_FLUIDSTACK_TO_INT =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<FluidStack, Boolean> FUNCTION_FLUIDSTACK_TO_BOOLEAN =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Operator builders ---------------
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_OPERATOR_TAKE_OPERATOR = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator innerOperator = input.getValue(0, ValueTypes.OPERATOR).getRawValue();
                if (input.getVariables().length > 1) {
                    if (innerOperator.getRequiredInputLength() == 1) {
                        IValue applyingValue = input.getValue(1);
                        MutableComponent error = innerOperator.validateTypes(new IValueType[]{applyingValue.getType()});
                        if (error != null) {
                            throw new EvaluationException(error);
                        }
                    } else if (innerOperator.getRequiredInputLength() > 0) {
                        if (!ValueHelpers.correspondsTo(input.getVariables()[1].getType(), innerOperator.getInputTypes()[0])) {
                            MutableComponent error = Component.translatable(L10NValues.OPERATOR_ERROR_WRONGCURRYINGTYPE,
                                    Component.translatable(innerOperator.getTranslationKey()),
                                    Component.translatable(input.getVariables()[1].getType().getTranslationKey()),
                                    0,
                                    Component.translatable(innerOperator.getInputTypes()[0].getTranslationKey())
                            );
                            throw new EvaluationException(error);
                        }
                    }
                }
                return Pair.<IOperator, OperatorBase.SafeVariablesGetter>of(innerOperator,
                        new OperatorBase.SafeVariablesGetter.Shifted(1, input.getVariables()));
            });
    public static final IterativeFunction.PrePostBuilder<IOperator, IValue> FUNCTION_ONE_OPERATOR = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> getSafeOperator(input.getValue(0, ValueTypes.OPERATOR), ValueTypes.CATEGORY_ANY));
    public static final IterativeFunction.PrePostBuilder<IOperator, IValue> FUNCTION_ONE_PREDICATE = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> getSafePredictate(input.getValue(0, ValueTypes.OPERATOR)));
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, IOperator>, IValue> FUNCTION_TWO_OPERATORS = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator second = getSafeOperator(input.getValue(1, ValueTypes.OPERATOR), ValueTypes.CATEGORY_ANY);
                IValueType[] secondInputs = second.getInputTypes();
                if(secondInputs.length < 1) {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_OPERATORPARAMWRONGINPUTLENGTH,
                            1, second.getLocalizedNameFull(), secondInputs.length));
                }
                IValueType secondInputType = secondInputs[0];
                if (ValueHelpers.correspondsTo(secondInputType, ValueTypes.OPERATOR)) {
                    secondInputType = ValueTypes.CATEGORY_ANY;
                }
                IOperator first = getSafeOperator(input.getValue(0, ValueTypes.OPERATOR), secondInputType);
                return Pair.of(first, second);
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, IOperator>, IValue> FUNCTION_TWO_PREDICATES = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator first = getSafePredictate(input.getValue(0, ValueTypes.OPERATOR));
                IOperator second = getSafePredictate(input.getValue(1, ValueTypes.OPERATOR));
                return Pair.of(first, second);
            });
    public static final IterativeFunction.PrePostBuilder<Triple<IOperator, IOperator, IOperator>, IValue> FUNCTION_THREE_OPERATORS = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator third = getSafeOperator(input.getValue(2, ValueTypes.OPERATOR), ValueTypes.CATEGORY_ANY);
                IValueType<?>[] types = third.getInputTypes();
                if(types.length < 2) {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_OPERATORPARAMWRONGINPUTLENGTH,
                            2, third.getLocalizedNameFull(), types.length));
                }
                IValueType<?> firstOutputType = types[0];
                IValueType<?> secondOutputType = types[1];
                if (ValueHelpers.correspondsTo(firstOutputType, ValueTypes.OPERATOR)) {
                    firstOutputType = ValueTypes.CATEGORY_ANY;
                }
                if (ValueHelpers.correspondsTo(secondOutputType, ValueTypes.OPERATOR)) {
                    secondOutputType = ValueTypes.CATEGORY_ANY;
                }
                IOperator first = getSafeOperator(input.getValue(0, ValueTypes.OPERATOR), firstOutputType);
                IOperator second = getSafeOperator(input.getValue(1, ValueTypes.OPERATOR), secondOutputType);
                return Triple.of(first, second, third);
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_OPERATOR_TAKE_OPERATOR_LIST = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeOperator.ValueOperator valueOperator = input.getValue(0, ValueTypes.OPERATOR);
                IOperator innerOperator = valueOperator.getRawValue();
                input.getValue(1, ValueTypes.LIST); // To trigger exception on invalid type
                return Pair.<IOperator, OperatorBase.SafeVariablesGetter>of(innerOperator,
                        new OperatorBase.SafeVariablesGetter.Shifted(1, input.getVariables()));
            });
    /**
     * Corresponds to {@link ValueHelpers#evaluateOperator(IOperator, IVariable[])}.
     */
    public static OperatorBuilder.IConditionalOutputTypeDeriver OPERATOR_CONDITIONAL_OUTPUT_DERIVER = (operator, variablesAll) -> {
        try {
            IValue value = variablesAll[0].getValue();
            // In some cases, validation can succeed because of parameters being ANY.
            // In this case, return a dummy type.
            if (!(value instanceof ValueTypeOperator.ValueOperator)) {
                return ValueTypes.CATEGORY_ANY;
            }
            IOperator innerOperator = ((ValueTypeOperator.ValueOperator) value).getRawValue();
            IVariable[] variables = ArrayUtils.subarray(variablesAll, 1, variablesAll.length);
            int requiredLength = innerOperator.getRequiredInputLength();
            if (requiredLength == variables.length) {
                Component error = innerOperator.validateTypes(ValueHelpers.from(variables));
                if (error != null) {
                    return innerOperator.getOutputType();
                }
                return innerOperator.getConditionalOutputType(variables);
            } else {
                if (variables.length > requiredLength) { // We have MORE variables as input than the operator accepts
                    IVariable[] acceptableVariables = ArrayUtils.subarray(variables, 0, requiredLength);
                    IVariable[] remainingVariables = ArrayUtils.subarray(variables, requiredLength, variables.length);

                    // Pass all required variables to the operator, and forward all remaining ones to the resulting operator
                    IValue result = ValueHelpers.evaluateOperator(innerOperator, acceptableVariables);

                    // Error if the result is NOT an operator
                    if (result.getType() != ValueTypes.OPERATOR) {
                        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_CURRYINGOVERFLOW,
                                Component.translatable(innerOperator.getTranslationKey()),
                                requiredLength,
                                variables.length,
                                Component.translatable(result.getType().getTranslationKey())));
                    }

                    // Pass all remaining variables to the resulting operator
                    IOperator nextOperator = ((ValueTypeOperator.ValueOperator) result).getRawValue();
                    Component error = nextOperator.validateTypes(ValueHelpers.from(remainingVariables));
                    if (error != null) {
                        return nextOperator.getOutputType();
                    }
                    return nextOperator.getConditionalOutputType(remainingVariables);
                } else {
                    return ValueTypes.OPERATOR;
                }
            }
        } catch (EvaluationException e) {
            return ValueTypes.CATEGORY_ANY;
        }
    };
    /**
     * Corresponds to {@link ValueHelpers#evaluateOperator(IOperator, IVariable[])}, but with the input variable being a list.
     */
    public static OperatorBuilder.IConditionalOutputTypeDeriver OPERATOR_CONDITIONAL_OUTPUT_DERIVER_LIST = (operator, variablesIn) -> {
        try {
            // Get second param as list
            IValue valueList = variablesIn[1].getValue();
            // In some cases, validation can succeed because of parameters being ANY.
            // In this case, return a dummy type.
            if (!(valueList instanceof ValueTypeList.ValueList)) {
                return ValueTypes.CATEGORY_ANY;
            }
            IValueTypeListProxy listProxy = ((ValueTypeList.ValueList) valueList).getRawValue();

            // Expand the list to a variable array, with variablesIn[0] prepended (the operator to apply).
            IVariable[] variablesExpanded = Stream.concat(
                    Stream.of(variablesIn[0]),
                    Stream.<IValue>of(Iterables.toArray(listProxy, IValue.class))
                        .map(Variable::new)
            ).toArray(IVariable[]::new);

            return OPERATOR_CONDITIONAL_OUTPUT_DERIVER.getConditionalOutputType(operator, variablesExpanded);
        } catch (EvaluationException e) {
            return ValueTypes.CATEGORY_ANY;
        }
    };
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR = OperatorBuilder
            .forType(ValueTypes.OPERATOR).appendKind("operator");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR_2_INFIX_LONG = OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR_1_PREFIX_LONG = OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG);

    // --------------- String builders ---------------

    public static final IterativeFunction.PrePostBuilder<ResourceLocation, IValue> FUNCTION_STRING_TO_RESOURCE_LOCATION = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeString.ValueString a = input.getValue(0, ValueTypes.STRING);
                return ValueHelpers.createResourceLocationInEvaluation(a.getRawValue());
            });

    // --------------- Operator helpers ---------------

    /**
     * Get the operator from a value in a safe manner.
     * @param value The operator value.
     * @param expectedOutput The expected output value type.
     * @return The operator.
     * @throws EvaluationException If the operator is not a predicate.
     */
    public static IOperator getSafeOperator(ValueTypeOperator.ValueOperator value, IValueType expectedOutput) throws EvaluationException {
        IOperator operator = value.getRawValue();
        if (!ValueHelpers.correspondsTo(operator.getOutputType(), expectedOutput)) {
            MutableComponent error = Component.translatable(L10NValues.OPERATOR_ERROR_ILLEGALPROPERY,
                    Component.translatable(expectedOutput.getTranslationKey()),
                    Component.translatable(operator.getOutputType().getTranslationKey()),
                    operator.getLocalizedNameFull());
            throw new EvaluationException(error);
        }
        return operator;
    }

    /**
     * Get the predicate from a value in a safe manner.
     * It is expected that the operator returns a boolean.
     * @param value The operator value.
     * @return The operator.
     * @throws EvaluationException If the operator is not a predicate.
     */
    public static IOperator getSafePredictate(ValueTypeOperator.ValueOperator value) throws EvaluationException {
        return getSafeOperator(value, ValueTypes.BOOLEAN);
    }

    /**
     * Create a type validator for operator operator type validators.
     * @param expectedSubTypes The expected types that must be present in the operator (not including the first
     *                         operator type itself.
     * @return The type validator instance.
     */
    public static OperatorBuilder.ITypeValidator createOperatorTypeValidator(final IValueType<?>... expectedSubTypes) {
        final int subOperatorLength = expectedSubTypes.length;
        final Component expected = Component.translatable(
                org.cyclops.integrateddynamics.core.helper.Helpers.createPatternOfLength(subOperatorLength), (Object[]) ValueHelpers.from(expectedSubTypes));
        return (operator, input) -> {
            if (input.length == 0 || !ValueHelpers.correspondsTo(input[0], ValueTypes.OPERATOR)) {
                String givenName = input.length == 0 ? "null" : input[0].getTranslationKey();
                return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDOPERATOROPERATOR,
                        0, givenName);
            }
            if (input.length != subOperatorLength + 1) {
                IValueType<?>[] operatorInputs = Arrays.copyOfRange(input, 1, input.length);
                Component given = Component.translatable(
                        org.cyclops.integrateddynamics.core.helper.Helpers.createPatternOfLength(operatorInputs.length), (Object[]) ValueHelpers.from(operatorInputs));
                return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDOPERATORSIGNATURE,
                        expected, given);
            }
            return null;
        };
    }

    // --------------- NBT builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT = OperatorBuilder.forType(ValueTypes.NBT).appendKind("nbt");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_1_PREFIX_LONG = NBT.inputTypes(ValueTypes.NBT).renderPattern(IConfigRenderPattern.PREFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_1_SUFFIX_LONG = NBT.inputTypes(ValueTypes.NBT).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_2 = NBT.inputTypes(ValueTypes.NBT, ValueTypes.STRING).renderPattern(IConfigRenderPattern.INFIX_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_2_NBT = NBT.inputTypes(ValueTypes.NBT, ValueTypes.NBT).renderPattern(IConfigRenderPattern.INFIX_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_3 = NBT.inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.STRING).output(ValueTypes.NBT).renderPattern(IConfigRenderPattern.INFIX_2_LONG);

    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, IValue> FUNCTION_NBT = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeNbt.ValueNbt value = input.getValue(0, ValueTypes.NBT);
                return value.getRawValue();
            });
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, IValue> FUNCTION_NBT_COMPOUND_ENTRY = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeNbt.ValueNbt valueNbt = input.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString valueString = input.getValue(1, ValueTypes.STRING);
                return valueNbt.getRawValue()
                        .filter(tag -> tag instanceof CompoundTag)
                        .map(tag -> ((CompoundTag) tag).get(valueString.getRawValue()));
            });
    public static final IterativeFunction.PrePostBuilder<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_NBT_COPY_FOR_VALUE = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeNbt.ValueNbt valueNbt = input.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString valueString = input.getValue(1, ValueTypes.STRING);
                return Triple.of(valueNbt.getRawValue()
                                .filter(t -> t instanceof CompoundTag)
                                .map(t -> (CompoundTag) t)
                                .map(CompoundTag::copy), valueString.getRawValue(),
                        new OperatorBase.SafeVariablesGetter.Shifted(2, input.getVariables()));
            });

    public static final IOperatorValuePropagator<Optional<Tag>, Optional<CompoundTag>> PROPAGATOR_NBT_COMPOUND = opt -> opt
            .filter(t -> t instanceof CompoundTag)
            .map(t -> (CompoundTag) t);

    public static final IterativeFunction.PrePostBuilder<Optional<CompoundTag>, IValue> FUNCTION_NBT_COMPOUND = FUNCTION_NBT.appendPre(PROPAGATOR_NBT_COMPOUND);

    public static final IterativeFunction.PrePostBuilder<Optional<CompoundTag>, Integer> FUNCTION_NBT_COMPOUND_TO_INT =
            FUNCTION_NBT_COMPOUND.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<CompoundTag>, Boolean> FUNCTION_NBT_COMPOUND_TO_BOOLEAN =
            FUNCTION_NBT_COMPOUND.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Integer> FUNCTION_NBT_COMPOUND_ENTRY_TO_INT =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Long> FUNCTION_NBT_COMPOUND_ENTRY_TO_LONG =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_LONG_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Double> FUNCTION_NBT_COMPOUND_ENTRY_TO_DOUBLE =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Boolean> FUNCTION_NBT_COMPOUND_ENTRY_TO_BOOLEAN =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, String> FUNCTION_NBT_COMPOUND_ENTRY_TO_STRING =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_STRING_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Optional<Tag>> FUNCTION_NBT_COMPOUND_ENTRY_TO_NBT =
            FUNCTION_NBT_COMPOUND_ENTRY.appendPost(PROPAGATOR_NBT_VALUE);

    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Integer> FUNCTION_NBT_TO_INT =
            FUNCTION_NBT.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Long> FUNCTION_NBT_TO_LONG =
            FUNCTION_NBT.appendPost(PROPAGATOR_LONG_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Double> FUNCTION_NBT_TO_DOUBLE =
            FUNCTION_NBT.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Boolean> FUNCTION_NBT_TO_BOOLEAN =
            FUNCTION_NBT.appendPost(PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, String> FUNCTION_NBT_TO_STRING =
            FUNCTION_NBT.appendPost(PROPAGATOR_STRING_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<Tag>, Optional<Tag>> FUNCTION_NBT_TO_NBT =
            FUNCTION_NBT.appendPost(PROPAGATOR_NBT_VALUE);

    public static final IterativeFunction.PrePostBuilder<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, Optional<CompoundTag>>
            FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT = FUNCTION_NBT_COPY_FOR_VALUE.appendPost(PROPAGATOR_NBT_COMPOUND_VALUE);

    // --------------- Ingredients builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS = OperatorBuilder.forType(ValueTypes.OBJECT_INGREDIENTS).appendKind("ingredients");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS_1_PREFIX_LONG = INGREDIENTS
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS_3_ITEMSTACK = INGREDIENTS
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS, ValueTypes.INTEGER, ValueTypes.OBJECT_ITEMSTACK)
            .renderPattern(IConfigRenderPattern.INFIX_2_LONG).output(ValueTypes.OBJECT_INGREDIENTS);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS_3_FLUIDSTACK = INGREDIENTS
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS, ValueTypes.INTEGER, ValueTypes.OBJECT_FLUIDSTACK)
            .renderPattern(IConfigRenderPattern.INFIX_2_LONG).output(ValueTypes.OBJECT_INGREDIENTS);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS_3_LONG = INGREDIENTS
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS, ValueTypes.INTEGER, ValueTypes.LONG)
            .renderPattern(IConfigRenderPattern.INFIX_2_LONG).output(ValueTypes.OBJECT_INGREDIENTS);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INGREDIENTS_2_LIST = INGREDIENTS
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS, ValueTypes.LIST)
            .renderPattern(IConfigRenderPattern.INFIX_LONG).output(ValueTypes.OBJECT_INGREDIENTS);

    public static OperatorBase.IFunction createFunctionIngredientsList(Callable<IngredientComponent<?, ?>> componentReference) {
        return variables -> {
            IngredientComponent<?, ?> component = null;
            try {
                component = componentReference.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            IIngredientComponentHandler componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
            ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
            List<?> list = Lists.newArrayList();
            if (value.getRawValue().isPresent()) {
                list = value.getRawValue().get().getInstances(component);
            }
            return ValueTypeList.ValueList.ofList(componentHandler.getValueType(), list.stream()
                    .map(i -> componentHandler.toValue(i)).collect(Collectors.toList()));
        };
    }

    public static <VT extends IValueType<V>, V extends IValue, T, M> List<T> unwrapIngredientComponentList(IngredientComponent<T, M> component,
                                                                                                           ValueTypeList.ValueList<VT, V> list)
            throws EvaluationException {
        IIngredientComponentHandler<VT, V, T, M> componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
        if (list.getRawValue().getValueType() != componentHandler.getValueType()) {
            throw new EvaluationException(Component.translatable(
                    L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                    list.getRawValue().getValueType(), componentHandler.getValueType()));
        }
        List<T> listTransformed = Lists.newArrayListWithExpectedSize(list.getRawValue().getLength());
        for (V value : list.getRawValue()) {
            listTransformed.add(componentHandler.toInstance(value));
        }
        return listTransformed;
    }

    // --------------- Recipe builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RECIPE = OperatorBuilder.forType(ValueTypes.OBJECT_RECIPE).appendKind("recipe");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RECIPE_1_SUFFIX_LONG = RECIPE
            .inputTypes(ValueTypes.OBJECT_RECIPE).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RECIPE_2_INFIX = RECIPE
            .inputTypes(ValueTypes.OBJECT_RECIPE, ValueTypes.OBJECT_INGREDIENTS)
            .renderPattern(IConfigRenderPattern.INFIX_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RECIPE_2_PREFIX = RECIPE
            .inputTypes(ValueTypes.OBJECT_INGREDIENTS, ValueTypes.OBJECT_INGREDIENTS)
            .renderPattern(IConfigRenderPattern.PREFIX_2_LONG);

    // --------------- Capability helpers ---------------

    /**
     * Helper function to create an operator function builder for deriving capabilities from an itemstack.
     * @param capabilityReference The capability instance reference.
     * @param <T> The capability type.
     * @return The builder.
     */
    public static <T> IterativeFunction.PrePostBuilder<T, IValue> getItemCapability(@Nullable final ICapabilityReference<T> capabilityReference) {
        return IterativeFunction.PrePostBuilder.begin()
                .appendPre(input -> {
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                    if(!a.getRawValue().isEmpty()) {
                        return a.getRawValue().getCapability(capabilityReference.getReference(), null).orElse(null);
                    }
                    return null;
                });
    }

    public static interface ICapabilityReference<T> {
        public Capability<T> getReference();
    }

}
