package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeSmartFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

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
    public static final IOperatorValuePropagator<Integer, IValue> PROPAGATOR_INTEGER_VALUE = new IOperatorValuePropagator<Integer, IValue>() {
        @Override
        public IValue getOutput(Integer input) throws EvaluationException {
            return ValueTypeInteger.ValueInteger.of(input);
        }
    };
    public static final IOperatorValuePropagator<Boolean, IValue> PROPAGATOR_BOOLEAN_VALUE = new IOperatorValuePropagator<Boolean, IValue>() {
        @Override
        public IValue getOutput(Boolean input) throws EvaluationException {
            return ValueTypeBoolean.ValueBoolean.of(input);
        }
    };
    public static final IOperatorValuePropagator<Double, IValue> PROPAGATOR_DOUBLE_VALUE = new IOperatorValuePropagator<Double, IValue>() {
        @Override
        public IValue getOutput(Double input) throws EvaluationException {
            return ValueTypeDouble.ValueDouble.of(input);
        }
    };

    // --------------- Arithmetic builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("arithmetic").conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
        @Override
        public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input) {
            IValueType[] original = ValueHelpers.from(input);
            IValueTypeNumber[] types = new IValueTypeNumber[original.length];
            for(int i = 0; i < original.length; i++) {
                types[i] = (IValueTypeNumber) original[i];
            }
            return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
        }
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

    // --------------- List builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST = OperatorBuilder.forType(ValueTypes.LIST).appendKind("double");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST_1_PREFIX = LIST.inputTypes(1, ValueTypes.LIST).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Block builders ---------------
    public static final OperatorBuilder BLOCK = OperatorBuilder.forType(ValueTypes.OBJECT_BLOCK).appendKind("block");
    public static final OperatorBuilder BLOCK_1_SUFFIX_LONG = BLOCK.inputTypes(1, ValueTypes.OBJECT_BLOCK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);

    // --------------- ItemStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_ITEMSTACK).appendKind("itemstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_SUFFIX_LONG = ITEMSTACK.inputTypes(1, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_2 = ITEMSTACK.inputTypes(2, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeSmartFunction.PrePostBuilder<ItemStack, IValue> FUNCTION_ITEMSTACK = IterativeSmartFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, ItemStack>() {
                @Override
                public ItemStack getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeSmartFunction.PrePostBuilder<ItemStack, Integer> FUNCTION_ITEMSTACK_TO_INT =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeSmartFunction.PrePostBuilder<ItemStack, Boolean> FUNCTION_ITEMSTACK_TO_BOOLEAN =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Entity builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY = OperatorBuilder.forType(ValueTypes.OBJECT_ENTITY).appendKind("entity");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY_1_SUFFIX_LONG = ENTITY.inputTypes(1, ValueTypes.OBJECT_ENTITY).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IterativeSmartFunction.PrePostBuilder<Entity, IValue> FUNCTION_ENTITY = IterativeSmartFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Entity>() {
                @Override
                public Entity getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeSmartFunction.PrePostBuilder<Entity, Double> FUNCTION_ENTITY_TO_DOUBLE =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeSmartFunction.PrePostBuilder<Entity, Boolean> FUNCTION_ENTITY_TO_BOOLEAN =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_BOOLEAN_VALUE);

}
