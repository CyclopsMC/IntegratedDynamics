package org.cyclops.integrateddynamics.core.evaluate;

import com.google.common.base.Optional;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.Arrays;

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
    public static final IOperatorValuePropagator<NBTTagCompound, IValue> PROPAGATOR_NBT_VALUE = ValueTypeNbt.ValueNbt::of;
    public static final IOperatorValuePropagator<ResourceLocation, ValueTypeString.ValueString> PROPAGATOR_RESOURCELOCATION_MODNAME = resourceLocation -> {
        String modName;
        try {
            String modId = Helpers.getModId(resourceLocation.getResourceDomain());
            ModContainer mod = Loader.instance().getIndexedModList().get(modId);
            modName = mod == null ? "Minecraft" : mod.getName();
        } catch (NullPointerException e) {
            modName = "";
        }
        return ValueTypeString.ValueString.of(modName);
    };

    // --------------- Arithmetic builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("arithmetic").conditionalOutputTypeDeriver((operator, input) -> {
        IValueType[] original = ValueHelpers.from(input);
        IValueTypeNumber[] types = new IValueTypeNumber[original.length];
        for(int i = 0; i < original.length; i++) {
            if (original[i] == ValueTypes.CATEGORY_ANY) {
                // This avoids a class-cast exception in cases where we don't know the exact type.
                return ValueTypes.CATEGORY_ANY;
            }
            types[i] = (IValueTypeNumber) original[i];
        }
        return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
    });
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

    // --------------- Double builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE = OperatorBuilder.forType(ValueTypes.DOUBLE).appendKind("double");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE_1_PREFIX = DOUBLE.inputTypes(1, ValueTypes.DOUBLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Nullable builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE = OperatorBuilder.forType(ValueTypes.CATEGORY_NULLABLE).appendKind("general");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE_1_PREFIX = NULLABLE.inputTypes(1, ValueTypes.CATEGORY_NULLABLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- List builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST = OperatorBuilder.forType(ValueTypes.LIST).appendKind("list");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST_1_PREFIX = LIST.inputTypes(1, ValueTypes.LIST).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Block builders ---------------
    public static final OperatorBuilder BLOCK = OperatorBuilder.forType(ValueTypes.OBJECT_BLOCK).appendKind("block");
    public static final OperatorBuilder BLOCK_1_SUFFIX_LONG = BLOCK.inputTypes(1, ValueTypes.OBJECT_BLOCK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Optional<SoundType>> BLOCK_SOUND = input -> {
        ValueObjectTypeBlock.ValueBlock block = input.getValue(0);
        if(block.getRawValue().isPresent()) {
            return Optional.of(block.getRawValue().get().getBlock().getSoundType());
        }
        return Optional.absent();
    };

    // --------------- ItemStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_ITEMSTACK).appendKind("itemstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_SUFFIX_LONG = ITEMSTACK.inputTypes(1, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_2 = ITEMSTACK.inputTypes(2, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_INTEGER_1 = ITEMSTACK.inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.INTEGER}).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<ItemStack, IValue> FUNCTION_ITEMSTACK = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> ((ValueObjectTypeItemStack.ValueItemStack) input.getValue(0)).getRawValue());
    public static final IterativeFunction.PrePostBuilder<ItemStack, Integer> FUNCTION_ITEMSTACK_TO_INT =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<ItemStack, Boolean> FUNCTION_ITEMSTACK_TO_BOOLEAN =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, IValue> FUNCTION_ENERGYSTORAGEITEM = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                if(!a.getRawValue().isEmpty() && a.getRawValue().hasCapability(CapabilityEnergy.ENERGY, null)) {
                    return a.getRawValue().getCapability(CapabilityEnergy.ENERGY, null);
                }
                return null;
            });
    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, Integer> FUNCTION_CONTAINERITEM_TO_INT =
            FUNCTION_ENERGYSTORAGEITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<IEnergyStorage, Boolean> FUNCTION_CONTAINERITEM_TO_BOOLEAN =
            FUNCTION_ENERGYSTORAGEITEM.appendPost(org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders.PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Entity builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY = OperatorBuilder.forType(ValueTypes.OBJECT_ENTITY).appendKind("entity");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY_1_SUFFIX_LONG = ENTITY.inputTypes(1, ValueTypes.OBJECT_ENTITY).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IterativeFunction.PrePostBuilder<Entity, IValue> FUNCTION_ENTITY = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueObjectTypeEntity.ValueEntity a = input.getValue(0);
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
                ValueObjectTypeFluidStack.ValueFluidStack a = input.getValue(0);
                return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
            });
    public static final IterativeFunction.PrePostBuilder<FluidStack, Integer> FUNCTION_FLUIDSTACK_TO_INT =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<FluidStack, Boolean> FUNCTION_FLUIDSTACK_TO_BOOLEAN =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Operator builders ---------------
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_OPERATOR_TAKE_OPERATOR = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator innerOperator = ((ValueTypeOperator.ValueOperator) input.getValue(0)).getRawValue();
                if (innerOperator.getRequiredInputLength() == 1) {
                    IValue applyingValue = input.getValue(1);
                    L10NHelpers.UnlocalizedString error = innerOperator.validateTypes(new IValueType[]{applyingValue.getType()});
                    if (error != null) {
                        throw new EvaluationException(error.localize());
                    }
                } else {
                    if (!ValueHelpers.correspondsTo(input.getVariables()[1].getType(), innerOperator.getInputTypes()[0])) {
                        L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGCURRYINGTYPE,
                                new L10NHelpers.UnlocalizedString(innerOperator.getUnlocalizedName()),
                                new L10NHelpers.UnlocalizedString(input.getVariables()[0].getType().getUnlocalizedName()),
                                0,
                                new L10NHelpers.UnlocalizedString(innerOperator.getInputTypes()[0].getUnlocalizedName())
                                );
                        throw new EvaluationException(error.localize());
                    }
                }
                return Pair.<IOperator, OperatorBase.SafeVariablesGetter>of(innerOperator,
                        new OperatorBase.SafeVariablesGetter.Shifted(1, input.getVariables()));
            });
    public static final IterativeFunction.PrePostBuilder<IOperator, IValue> FUNCTION_ONE_OPERATOR = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> getSafeOperator((ValueTypeOperator.ValueOperator) input.getValue(0), ValueTypes.CATEGORY_ANY));
    public static final IterativeFunction.PrePostBuilder<IOperator, IValue> FUNCTION_ONE_PREDICATE = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> getSafePredictate((ValueTypeOperator.ValueOperator) input.getValue(0)));
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, IOperator>, IValue> FUNCTION_TWO_OPERATORS = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator second = getSafeOperator((ValueTypeOperator.ValueOperator) input.getValue(1), ValueTypes.CATEGORY_ANY);
                IValueType secondInputType = second.getInputTypes()[0];
                if (ValueHelpers.correspondsTo(secondInputType, ValueTypes.OPERATOR)) {
                    secondInputType = ValueTypes.CATEGORY_ANY;
                }
                IOperator first = getSafeOperator((ValueTypeOperator.ValueOperator) input.getValue(0), secondInputType);
                return Pair.of(first, second);
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, IOperator>, IValue> FUNCTION_TWO_PREDICATES = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator first = getSafePredictate((ValueTypeOperator.ValueOperator) input.getValue(0));
                IOperator second = getSafePredictate((ValueTypeOperator.ValueOperator) input.getValue(1));
                return Pair.of(first, second);
            });
    public static final IterativeFunction.PrePostBuilder<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_OPERATOR_TAKE_OPERATOR_LIST = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                IOperator innerOperator = ((ValueTypeOperator.ValueOperator) input.getValue(0)).getRawValue();
                IValue applyingValue = input.getValue(1);
                if (!(applyingValue instanceof ValueTypeList.ValueList)) {
                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                            "?",
                            new L10NHelpers.UnlocalizedString(applyingValue.getType().getUnlocalizedName()),
                            0,
                            new L10NHelpers.UnlocalizedString(ValueTypes.LIST.getUnlocalizedName())
                    );
                    throw new EvaluationException(error.localize());
                }
                ValueTypeList.ValueList applyingList = (ValueTypeList.ValueList) applyingValue;
                L10NHelpers.UnlocalizedString error = innerOperator.validateTypes(new IValueType[]{applyingList.getRawValue().getValueType()});
                if (error != null) {
                    throw new EvaluationException(error.localize());
                }
                return Pair.<IOperator, OperatorBase.SafeVariablesGetter>of(innerOperator,
                        new OperatorBase.SafeVariablesGetter.Shifted(1, input.getVariables()));
            });
    public static OperatorBuilder.IConditionalOutputTypeDeriver newOperatorConditionalOutputDeriver(final int consumeArguments) {
        return (operator, input) -> {
            try {
                IOperator innerOperator = ((ValueTypeOperator.ValueOperator) input[0].getValue()).getRawValue();
                if (innerOperator.getRequiredInputLength() == consumeArguments) {
                    IVariable[] innerVariables = Arrays.copyOfRange(input, consumeArguments, input.length);
                    L10NHelpers.UnlocalizedString error = innerOperator.validateTypes(ValueHelpers.from(innerVariables));
                    if (error != null) {
                        return innerOperator.getOutputType();
                    }
                    return innerOperator.getConditionalOutputType(innerVariables);
                } else {
                    return ValueTypes.OPERATOR;
                }
            } catch (EvaluationException e) {
                return ValueTypes.CATEGORY_ANY;
            }
        };
    }
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR = OperatorBuilder
            .forType(ValueTypes.OPERATOR).appendKind("operator");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR_2_INFIX_LONG = OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> OPERATOR_1_PREFIX_LONG = OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG);

    // --------------- String builders ---------------

    public static final IterativeFunction.PrePostBuilder<Pair<ResourceLocation, Integer>, IValue> FUNCTION_STRING_TO_RESOURCE_LOCATION = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> {
                ValueTypeString.ValueString a = input.getValue(0);
                String[] split = a.getRawValue().split(" ");
                if (split.length > 2) {
                    throw new EvaluationException("Invalid name.");
                }
                ResourceLocation resourceLocation = new ResourceLocation(split[0]);
                int meta = 0;
                if (split.length > 1) {
                    try {
                        meta = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        throw new EvaluationException(e.getMessage());
                    }
                }
                return Pair.of(resourceLocation, meta);
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
            L10NHelpers.UnlocalizedString error =
                    new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_ILLEGALPROPERY,
                            expectedOutput, operator.getOutputType(), operator.getLocalizedNameFull());
            throw new EvaluationException(error.localize());
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
    public static OperatorBuilder.ITypeValidator createOperatorTypeValidator(final IValueType... expectedSubTypes) {
        final int subOperatorLength = expectedSubTypes.length;
        final L10NHelpers.UnlocalizedString expected = new L10NHelpers.UnlocalizedString(
                org.cyclops.integrateddynamics.core.helper.Helpers.createPatternOfLength(subOperatorLength), ValueHelpers.from(expectedSubTypes));
        return (operator, input) -> {
            if (input.length == 0 || !ValueHelpers.correspondsTo(input[0], ValueTypes.OPERATOR)) {
                String givenName = input.length == 0 ? "null" : input[0].getUnlocalizedName();
                return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDOPERATOROPERATOR,
                        0, givenName);
            }
            if (input.length != subOperatorLength + 1) {
                IValueType[] operatorInputs = Arrays.copyOfRange(input, 1, input.length);
                L10NHelpers.UnlocalizedString given = new L10NHelpers.UnlocalizedString(
                        org.cyclops.integrateddynamics.core.helper.Helpers.createPatternOfLength(operatorInputs.length), ValueHelpers.from(operatorInputs));
                return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDOPERATORSIGNATURE,
                        expected, given);
            }
            return null;
        };
    }

    // --------------- NBT builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT = OperatorBuilder.forType(ValueTypes.NBT).appendKind("nbt");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_1_SUFFIX_LONG = NBT.inputTypes(ValueTypes.NBT).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_2 = NBT.inputTypes(ValueTypes.NBT, ValueTypes.STRING).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NBT_3 = NBT.inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.STRING).output(ValueTypes.NBT).renderPattern(IConfigRenderPattern.INFIX_2);
    public static final IterativeFunction.PrePostBuilder<NBTTagCompound, IValue> FUNCTION_NBT = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> ((ValueTypeNbt.ValueNbt) input.getValue(0)).getRawValue());
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, IValue> FUNCTION_NBT_ENTRY = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> Optional.fromNullable(((ValueTypeNbt.ValueNbt) input.getValue(0)).getRawValue()
                   .getTag(((ValueTypeString.ValueString) input.getValue(1)).getRawValue())));
    public static final IterativeFunction.PrePostBuilder<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, IValue> FUNCTION_NBT_COPY_FOR_VALUE = IterativeFunction.PrePostBuilder.begin()
            .appendPre(input -> Triple.of(((ValueTypeNbt.ValueNbt) input.getValue(0)).getRawValue().copy(),
                ((ValueTypeString.ValueString) input.getValue(1)).getRawValue(),
                new OperatorBase.SafeVariablesGetter.Shifted(2, input.getVariables())));
    public static final IterativeFunction.PrePostBuilder<NBTTagCompound, Integer> FUNCTION_NBT_TO_INT =
            FUNCTION_NBT.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<NBTTagCompound, Boolean> FUNCTION_NBT_TO_BOOLEAN =
            FUNCTION_NBT.appendPost(PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, Integer> FUNCTION_NBT_ENTRY_TO_INT =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, Long> FUNCTION_NBT_ENTRY_TO_LONG =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_LONG_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, Double> FUNCTION_NBT_ENTRY_TO_DOUBLE =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, Boolean> FUNCTION_NBT_ENTRY_TO_BOOLEAN =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_BOOLEAN_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, String> FUNCTION_NBT_ENTRY_TO_STRING =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_STRING_VALUE);
    public static final IterativeFunction.PrePostBuilder<Optional<NBTBase>, NBTTagCompound> FUNCTION_NBT_ENTRY_TO_NBT =
            FUNCTION_NBT_ENTRY.appendPost(PROPAGATOR_NBT_VALUE);
    public static final IterativeFunction.PrePostBuilder<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, NBTTagCompound>
            FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT = FUNCTION_NBT_COPY_FOR_VALUE.appendPost(PROPAGATOR_NBT_VALUE);

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
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                    if(!a.getRawValue().isEmpty() && a.getRawValue().hasCapability(capabilityReference.getReference(), null)) {
                        return a.getRawValue().getCapability(capabilityReference.getReference(), null);
                    }
                    return null;
                });
    }

    public static interface ICapabilityReference<T> {
        public Capability<T> getReference();
    }

}
