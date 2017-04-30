package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.*;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.obfuscation.ObfuscationHelpers;

import java.util.Collections;
import java.util.List;

/**
 * Collection of available operators.
 *
 * @author rubensworks
 */
public final class Operators {

    public static final IOperatorRegistry REGISTRY = constructRegistry();

    private static IOperatorRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IOperatorRegistry.class);
        } else {
            return OperatorRegistry.getInstance();
        }
    }

    public static void load() {}

    /**
     * ----------------------------------- LOGICAL OPERATORS -----------------------------------
     */

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_AND = REGISTRY.register(OperatorBuilders.LOGICAL_2.symbol("&&").operatorName("and")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeBoolean.ValueBoolean a = variables.getValue(0);
                    if (!a.getRawValue()) {
                        return ValueTypeBoolean.ValueBoolean.of(false);
                    } else {
                        return variables.getValue(1);
                    }
                }
            }).build());

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_OR = REGISTRY.register(OperatorBuilders.LOGICAL_2.symbol("||").operatorName("or")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeBoolean.ValueBoolean a = variables.getValue(0);
                    if (a.getRawValue()) {
                        return ValueTypeBoolean.ValueBoolean.of(true);
                    } else {
                        return variables.getValue(1);
                    }
                }
            }).build());

    /**
     * Logical NOT operator with one input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_NOT = REGISTRY.register(OperatorBuilders.LOGICAL_1_PREFIX.symbol("!").operatorName("not")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypeBoolean.ValueBoolean.of(!((ValueTypeBoolean.ValueBoolean) variables.getValue(0)).getRawValue());
                }
            }).build());

    /**
     * Short-circuit logical NAND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_NAND = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(LOGICAL_AND).build(
                    "!&&", "nand", IConfigRenderPattern.INFIX, "logical"));

    /**
     * Short-circuit logical NAND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_NOR = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(LOGICAL_OR).build(
                    "!||", "nor", IConfigRenderPattern.INFIX, "logical"));

    /**
     * ----------------------------------- ARITHMETIC OPERATORS -----------------------------------
     */

    /**
     * Arithmetic ADD operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_ADDITION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("+").operatorName("addition")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.add(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());

    /**
     * Arithmetic MINUS operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("-").operatorName("subtraction")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.subtract(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());

    /**
     * Arithmetic MULTIPLY operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("*").operatorName("multiplication")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.multiply(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());

    /**
     * Arithmetic DIVIDE operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_DIVISION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("/").operatorName("division")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.divide(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());

    /**
     * Arithmetic MAX operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MAXIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("max").operatorName("maximum")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.max(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());

    /**
     * Arithmetic MIN operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MINIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("min").operatorName("minimum")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypes.CATEGORY_NUMBER.min(variables.getVariables()[0], variables.getVariables()[1]);
                }
            }).build());



    /**
     * ----------------------------------- INTEGER OPERATORS -----------------------------------
     */

    private static final ValueTypeInteger.ValueInteger ZERO = ValueTypeInteger.ValueInteger.of(0);

    /**
     * Integer MODULO operator with two input integers and one output integer.
     */
    public static final IOperator INTEGER_MODULUS = REGISTRY.register(OperatorBuilders.INTEGER_2.symbol("%").operatorName("modulus")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    if (b.getRawValue() == 0) { // You can not divide by zero
                        throw new EvaluationException("Division by zero");
                    } else if (b.getRawValue() == 1) { // If b is neutral element for division
                        return ZERO;
                    } else {
                        ValueTypeInteger.ValueInteger a = variables.getValue(0);
                        return ValueTypeInteger.ValueInteger.of(a.getRawValue() % b.getRawValue());
                    }
                }
            }).build());

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IOperator INTEGER_INCREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("++").operatorName("increment")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() + 1);
                }
            }).build());

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IOperator INTEGER_DECREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("--").operatorName("decrement")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() - 1);
                }
            }).build());

    /**
     * ----------------------------------- RELATIONAL OPERATORS -----------------------------------
     */

    /**
     * Relational == operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_EQUALS = REGISTRY.register(OperatorBuilders.RELATIONAL
            .inputTypes(2, ValueTypes.CATEGORY_ANY).renderPattern(IConfigRenderPattern.INFIX)
            .symbol("==").operatorName("equals")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypeBoolean.ValueBoolean.of(variables.getValue(0).equals(variables.getValue(1)));
                }
            })
            .typeValidator(new OperatorBuilder.ITypeValidator() {
                @Override
                public L10NHelpers.UnlocalizedString validateTypes(OperatorBase operator, IValueType[] input) {
                    // Input size checking
                    int requiredInputLength = operator.getRequiredInputLength();
                    if(input.length != requiredInputLength) {
                        return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                                operator.getOperatorName(), input.length, requiredInputLength);
                    }
                    // Input types checking
                    IValueType temporarySecondInputType = null;
                    for(int i = 0; i < requiredInputLength; i++) {
                        IValueType inputType = input[i];
                        if (inputType instanceof IValueTypeNumber) {
                            inputType = ValueTypes.CATEGORY_NUMBER;
                        }
                        if(inputType == null) {
                            return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_NULLTYPE, operator.getOperatorName(), Integer.toString(i));
                        }
                        if(i == 0) {
                            temporarySecondInputType = inputType;
                        } else if(i == 1) {
                            if(temporarySecondInputType != inputType) {
                                return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                        operator.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                                        Integer.toString(i), new L10NHelpers.UnlocalizedString(temporarySecondInputType.getUnlocalizedName()));
                            }
                        }
                    }
                    return null;
                }
            })
            .build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_GT = REGISTRY.register(OperatorBuilders.RELATIONAL_2
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol(">").operatorName("gt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.greaterThan(variables.getVariables()[0], variables.getVariables()[1]));
                }
            }).build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_LT = REGISTRY.register(OperatorBuilders.RELATIONAL_2
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol("<").operatorName("lt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.lessThan(variables.getVariables()[0], variables.getVariables()[1]));
                }
            }).build());

    /**
     * Relational != operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_NOTEQUALS = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(RELATIONAL_EQUALS).build(
                    "!=", "notequals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &gt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_GE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_GT).build(
                    ">=", "ge", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &lt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_LE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_LT).build(
                    "<=", "le", IConfigRenderPattern.INFIX, "relational"));

    /**
     * ----------------------------------- BINARY OPERATORS -----------------------------------
     */

    /**
     * Binary AND operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_AND = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("&").operatorName("and")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() & b.getRawValue());
                }
            }).build());

    /**
     * Binary OR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_OR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("|").operatorName("or")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() | b.getRawValue());
                }
            }).build());

    /**
     * Binary XOR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_XOR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("^").operatorName("xor")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() ^ b.getRawValue());
                }
            }).build());

    /**
     * Binary COMPLEMENT operator with one input integers and one output integers.
     */
    public static final IOperator BINARY_COMPLEMENT = REGISTRY.register(OperatorBuilders.BINARY_1_PREFIX.symbol("~").operatorName("complement")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of(~a.getRawValue());
                }
            }).build());

    /**
     * Binary &lt;&lt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_LSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("<<").operatorName("lshift")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() << b.getRawValue());
                }
            }).build());

    /**
     * Binary &gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>").operatorName("rshift")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() >> b.getRawValue());
                }
            }).build());

    /**
     * Binary &gt;&gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RZSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>>").operatorName("rzshift")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() >>> b.getRawValue());
                }
            }).build());

    /**
     * ----------------------------------- STRING OPERATORS -----------------------------------
     */

    /**
     * String length operator with one input string and one output integer.
     */
    public static final IOperator STRING_LENGTH = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX.symbol("len").operatorName("length")
            .output(ValueTypes.INTEGER).function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeString.ValueString a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue().length());
                }
            }).build());

    /**
     * String concat operator with two input strings and one output string.
     */
    public static final IOperator STRING_CONCAT = REGISTRY.register(OperatorBuilders.STRING_2.symbol("+").operatorName("concat")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeString.ValueString a = variables.getValue(0);
                    ValueTypeString.ValueString b = variables.getValue(1);
                    return ValueTypeString.ValueString.of(a.getRawValue() + b.getRawValue());
                }
            }).build());

    /**
     * Get a name value type name.
     */
    public static final IOperator NAMED_NAME = REGISTRY.register(OperatorBuilders.STRING_2.symbol("name").operatorName("name")
            .inputType(ValueTypes.CATEGORY_NAMED).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    return ValueTypeString.ValueString.of(ValueTypes.CATEGORY_NAMED.getName(variables.getVariables()[0]));
                }
            }).build());

    /**
     * ----------------------------------- DOUBLE OPERATORS -----------------------------------
     */

    /**
     * Double round operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_ROUND = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX.output(ValueTypes.INTEGER).symbol("|| ||").operatorName("round")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeDouble.ValueDouble a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of((int) Math.round(a.getRawValue()));
                }
            }).build());

    /**
     * Double ceil operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_CEIL = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX.output(ValueTypes.INTEGER).symbol("⌈ ⌉").operatorName("ceil")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeDouble.ValueDouble a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of((int) Math.ceil(a.getRawValue()));
                }
            }).build());

    /**
     * Double floor operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_FLOOR = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX.output(ValueTypes.INTEGER).symbol("⌊ ⌋").operatorName("floor")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeDouble.ValueDouble a = variables.getValue(0);
                    return ValueTypeInteger.ValueInteger.of((int) Math.floor(a.getRawValue()));
                }
            }).build());

    /**
     * ----------------------------------- NULLABLE OPERATORS -----------------------------------
     */

    /**
     * Check if something is null
     */
    public static final IOperator NULLABLE_ISNULL = REGISTRY.register(OperatorBuilders.NULLABLE_1_PREFIX.symbol("o").operatorName("isnull")
            .inputType(ValueTypes.CATEGORY_ANY).output(ValueTypes.BOOLEAN).function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    if(ValueHelpers.correspondsTo(variables.getVariables()[0].getType(), ValueTypes.CATEGORY_NULLABLE)) {
                        return ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NULLABLE.isNull(variables.getVariables()[0]));
                    }
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }).build());

    /**
     * Check if something is not null
     */
    public static final IOperator NULLABLE_ISNOTNULL = REGISTRY.register(new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT)
            .apply(NULLABLE_ISNULL).build("∅", "isnotnull", IConfigRenderPattern.PREFIX_1, "general"));

    /**
     * ----------------------------------- LIST OPERATORS -----------------------------------
     */

    /**
     * List operator with one input list and one output integer
     */
    public static final IOperator LIST_LENGTH = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX.output(ValueTypes.INTEGER).symbol("| |").operatorName("length")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    return ValueTypeInteger.ValueInteger.of(a.getLength());
                }
            }).build());

    /**
     * Check if a list is empty
     */
    public static final IOperator LIST_EMPTY = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX.output(ValueTypes.BOOLEAN).symbol("∅").operatorName("empty")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    return ValueTypeBoolean.ValueBoolean.of(a.getLength() == 0);
                }
            }).build());

    /**
     * Check if a list is not empty
     */
    public static final IOperator LIST_NOT_EMPTY = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(LIST_EMPTY).build(
                    "o", "notempty", IConfigRenderPattern.PREFIX_1, "list"));

    /**
     * List operator with one input list and one output integer
     */
    public static final IOperator LIST_ELEMENT = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.INTEGER}).output(ValueTypes.CATEGORY_ANY)
            .renderPattern(IConfigRenderPattern.INFIX).symbolOperator("get")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    if (b.getRawValue() < a.getLength()) {
                        return a.get(b.getRawValue());
                    } else {
                        return a.getValueType().getDefault();
                    }
                }
            }).conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
                @Override
                public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input) {
                    try {
                        IValueTypeListProxy a = ((ValueTypeList.ValueList) input[0].getValue()).getRawValue();
                        return a.getValueType();
                    } catch (EvaluationException e) {
                        return operator.getConditionalOutputType(input);
                    }
                }
            }).build());

    /**
     * List contains operator that takes a list, a list element to look for and returns a boolean.
     */
    public static final IOperator LIST_CONTAINS = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.PREFIX_2_LONG)
            .output(ValueTypes.BOOLEAN).symbolOperator("contains")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    IValue input = variables.getValue(1);
                    for (IValue value : list) {
                        if (value.equals(input)) {
                            return ValueTypeBoolean.ValueBoolean.of(true);
                        }
                    }
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }).build());

    /**
     * List contains operator that takes a list, a predicate that maps a list element to a boolean, a list element and returns a boolean.
     */
    public static final IOperator LIST_CONTAINS_PREDICATE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX)
            .output(ValueTypes.BOOLEAN).symbolOperator("contains_p")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    IOperator operator = OperatorBuilders.getSafePredictate((ValueTypeOperator.ValueOperator) variables.getValue(1));
                    for (IValue value : list) {
                        IValue result = operator.evaluate(new IVariable[]{new Variable<>(value.getType(), value)});
                        if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                            return ValueTypeBoolean.ValueBoolean.of(true);
                        }
                    }
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }).build());

    /**
     * List operator with one input list, and element and one output integer
     */
    public static final IOperator LIST_COUNT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.INTEGER)
            .symbolOperator("count")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    IValue value = variables.getValue(1);
                    int count = 0;
                    for (IValue listValue : list) {
                        if (listValue.equals(value)) {
                            count++;
                        }
                    }
                    return ValueTypeInteger.ValueInteger.of(count);
                }
            }).build());

    /**
     * List operator with one input list, a predicate and one output integer
     */
    public static final IOperator LIST_COUNT_PREDICATE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.INTEGER)
            .symbolOperator("count_p")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    IOperator operator = OperatorBuilders.getSafePredictate((ValueTypeOperator.ValueOperator) variables.getValue(1));
                    int count = 0;
                    for (IValue listValue : list) {
                        IValue result = operator.evaluate(new IVariable[]{new Variable<>(listValue.getType(), listValue)});
                        if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                            count++;
                        }
                    }
                    return ValueTypeInteger.ValueInteger.of(count);
                }
            }).build());

    /**
     * Append an element to the given list
     */
    public static final IOperator LIST_APPEND = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("append")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    IValue value = variables.getValue(1);
                    if (!ValueHelpers.correspondsTo(a.getValueType(), value.getType())) {
                        L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                a.getValueType(), value.getType());
                        throw new EvaluationException(error.localize());
                    }
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyAppend(a, value));
                }
            }).build());

    /**
     * Build a list lazily using a start value and an operator that is applied to the previous element to get a next element.
     */
    public static final IOperator LIST_LAZYBUILT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.CATEGORY_ANY, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("lazybuilt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValue a = variables.getValue(0);
                    IOperator operator = OperatorBuilders.getSafeOperator((ValueTypeOperator.ValueOperator) variables.getValue(1), a.getType());
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyLazyBuilt<>(a, operator));
                }
            }).build());

    /**
     * Get the first element of the given list.
     */
    public static final IOperator LIST_HEAD = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX
            .inputTypes(new IValueType[]{ValueTypes.LIST}).output(ValueTypes.CATEGORY_ANY)
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).symbolOperator("head")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    if (a.getLength() > 0) {
                        return a.get(0);
                    } else {
                        return a.getValueType().getDefault();
                    }
                }
            }).conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
                @Override
                public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input) {
                    try {
                        IValueTypeListProxy a = ((ValueTypeList.ValueList) input[0].getValue()).getRawValue();
                        return a.getValueType();
                    } catch (EvaluationException e) {
                        return operator.getConditionalOutputType(input);
                    }
                }
            }).build());

    /**
     * Append an element to the given list.
     */
    public static final IOperator LIST_TAIL = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST})
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).output(ValueTypes.LIST)
            .symbolOperator("tail")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) variables.getValue(0)).getRawValue();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyTail(a));
                }
            }).build());

    /**
     * ----------------------------------- BLOCK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Block isOpaque operator with one input block and one output boolean.
     */
    public static final IOperator OBJECT_BLOCK_OPAQUE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("opaque")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeBlock.ValueBlock a = variables.getValue(0);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && a.getRawValue().get().isOpaqueCube());
                }
            }).build());

    /**
     * The itemstack representation of the block
     */
    public static final IOperator OBJECT_BLOCK_ITEMSTACK = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("itemstack")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeBlock.ValueBlock a = variables.getValue(0);
                    return ValueObjectTypeItemStack.ValueItemStack.of(a.getRawValue().isPresent() ? BlockHelpers.getItemStackFromBlockState(a.getRawValue().get()) : null);
                }
            }).build());

    /**
     * The name of the mod owning this block
     */
    public static final IOperator OBJECT_BLOCK_MODNAME = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, ResourceLocation>() {
                        @Override
                        public ResourceLocation getOutput(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                            ValueObjectTypeBlock.ValueBlock a = variables.getValue(0);
                            return a.getRawValue().isPresent() ? Block.REGISTRY.getNameForObject(a.getRawValue().get().getBlock()) : null;
                        }
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The breaksound of the block
     */
    public static final IOperator OBJECT_BLOCK_BREAKSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("breaksound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    new IOperatorValuePropagator<Optional<SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getBreakSound().getSoundName().toString();
                            }
                            return "";
                        }
                    },
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The placesound of the block
     */
    public static final IOperator OBJECT_BLOCK_PLACESOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("placesound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    new IOperatorValuePropagator<Optional<SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getPlaceSound().getSoundName().toString();
                            }
                            return "";
                        }
                    },
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The stepsound of the block
     */
    public static final IOperator OBJECT_BLOCK_STEPSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("stepsound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    new IOperatorValuePropagator<Optional<SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getStepSound().getSoundName().toString();
                            }
                            return "";
                        }
                    },
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());

    /**
     * If the block is shearable
     */
    public static final IOperator OBJECT_BLOCK_ISSHEARABLE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("isshearable")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeBlock.ValueBlock a = variables.getValue(0);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                            && a.getRawValue().get().getBlock() instanceof IShearable
                            && ((IShearable) a.getRawValue().get().getBlock()).isShearable(null, null, null));
                }
            }).build());

    /**
     * ----------------------------------- ITEM STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Item Stack size operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_SIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("size")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? itemStack.stackSize : 0;
                }
            })).build());

    /**
     * Item Stack maxsize operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("maxsize")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? itemStack.getMaxStackSize() : 0;
                }
            })).build());

    /**
     * Item Stack isstackable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISSTACKABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("stackable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && itemStack.isStackable();
                }
            })).build());

    /**
     * Item Stack isdamageable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISDAMAGEABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("damageable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && itemStack.isItemStackDamageable();
                }
            })).build());

    /**
     * Item Stack damage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_DAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("damage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? itemStack.getItemDamage() : 0;
                }
            })).build());

    /**
     * Item Stack maxdamage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXDAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("maxdamage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? itemStack.getMaxDamage() : 0;
                }
            })).build());

    /**
     * Item Stack isenchanted operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTED = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchanted")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && itemStack.isItemEnchanted();
                }
            })).build());

    /**
     * Item Stack isenchantable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchantable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && itemStack.isItemEnchantable();
                }
            })).build());

    /**
     * Item Stack repair cost with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_REPAIRCOST = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("repaircost")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? itemStack.getRepairCost() : 0;
                }
            })).build());

    /**
     * Get the rarity of an itemstack.
     */
    public static final IOperator OBJECT_ITEMSTACK_RARITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("rarity")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    return ValueTypeString.ValueString.of(a.getRawValue().isPresent() ? a.getRawValue().get().getRarity().rarityName : "");
                }
            }).build());

    /**
     * Get the strength of an itemstack against a block as a double.
     */
    public static final IOperator OBJECT_ITEMSTACK_STRENGTH_VS_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}).output(ValueTypes.DOUBLE)
            .symbolOperator("strength")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    ValueObjectTypeBlock.ValueBlock b = variables.getValue(1);
                    return ValueTypeDouble.ValueDouble.of(a.getRawValue().isPresent() && b.getRawValue().isPresent() ? a.getRawValue().get().getStrVsBlock(b.getRawValue().get()) : 0);
                }
            }).build());

    /**
     * If the given itemstack can be used to harvest the given block.
     */
    public static final IOperator OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}).output(ValueTypes.BOOLEAN)
            .symbolOperator("canharvest")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    ValueObjectTypeBlock.ValueBlock b = variables.getValue(1);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && b.getRawValue().isPresent() && a.getRawValue().get().canHarvestBlock(b.getRawValue().get()));
                }
            }).build());

    /**
     * The block from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("block")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    return ValueObjectTypeBlock.ValueBlock.of((a.getRawValue().isPresent() && a.getRawValue().get().getItem() instanceof ItemBlock) ? BlockHelpers.getBlockStateFromItemStack(a.getRawValue().get()) : null);
                }
            }).build());

    /**
     * If the given stack has a fluid.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISFLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isfluidstack")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && Helpers.getFluidStack(itemStack) != null;
                }
            })).build());

    /**
     * The fluidstack from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_FLUIDSTACK).symbolOperator("fluidstack")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    return ValueObjectTypeFluidStack.ValueFluidStack.of(a.getRawValue().isPresent() ? Helpers.getFluidStack(a.getRawValue().get()) : null);
                }
            }).build());

    /**
     * The capacity of the fluidstack from the stack.
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACKCAPACITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("fluidstackcapacity")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? Helpers.getFluidStackCapacity(itemStack) : 0;
                }
            })).build());

    /**
     * If the NBT tags of the given stacks are equal.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISNBTEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NBT=").operatorName("isnbtequal")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(0)).getRawValue();
                    Optional<ItemStack> b = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(1)).getRawValue();
                    boolean equal = false;
                    if(a.isPresent() && b.isPresent()) {
                        equal = a.get().isItemEqual(b.get()) && ItemStack.areItemStackTagsEqual(a.get(), b.get());
                    } else if(!a.isPresent() && !b.isPresent()) {
                        equal = true;
                    }
                    return ValueTypeBoolean.ValueBoolean.of(equal);
                }
            }).build());

    /**
     * If the raw items of the given stacks are equal, ignoring NBT but including damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISITEMEQUALNONBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NoNBT=").operatorName("isitemequalnonbt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(0)).getRawValue();
                    Optional<ItemStack> b = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(1)).getRawValue();
                    boolean equal = false;
                    if(a.isPresent() && b.isPresent()) {
                        equal = ItemStack.areItemsEqual(a.get(), b.get());
                    } else if(!a.isPresent() && !b.isPresent()) {
                        equal = true;
                    }
                    return ValueTypeBoolean.ValueBoolean.of(equal);
                }
            }).build());

    /**
     * If the raw items of the given stacks are equal, ignoring NBT and damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISRAWITEMEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwitemequal")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(0)).getRawValue();
                    Optional<ItemStack> b = ((ValueObjectTypeItemStack.ValueItemStack) variables.getValue(1)).getRawValue();
                    boolean equal = false;
                    if(a.isPresent() && b.isPresent()) {
                        equal = ItemStack.areItemsEqualIgnoreDurability(a.get(), b.get());
                    } else if(!a.isPresent() && !b.isPresent()) {
                        equal = true;
                    }
                    return ValueTypeBoolean.ValueBoolean.of(equal);
                }
            }).build());

    /**
     * The name of the mod owning this item
     */
    public static final IOperator OBJECT_ITEMSTACK_MODNAME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, ResourceLocation>() {
                        @Override
                        public ResourceLocation getOutput(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                            ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                            return a.getRawValue().isPresent() ? Item.REGISTRY.getNameForObject(a.getRawValue().get().getItem()) : null;
                        }
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The fuel burn time of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_FUELBURNTIME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("burntime")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(new IOperatorValuePropagator<ItemStack, Integer>() {
                @Override
                public Integer getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null ? TileEntityFurnace.getItemBurnTime(itemStack) : 0;
                }
            })).build());

    /**
     * If the given item can be used as fuel
     */
    public static final IOperator OBJECT_ITEMSTACK_CANBURN = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("canburn")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && TileEntityFurnace.getItemBurnTime(itemStack) > 0;
                }
            })).build());

    /**
     * If the given item can be smelted
     */
    public static final IOperator OBJECT_ITEMSTACK_CANSMELT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("cansmelt")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<ItemStack, Boolean>() {
                @Override
                public Boolean getOutput(ItemStack itemStack) throws EvaluationException {
                    return itemStack != null && FurnaceRecipes.instance().getSmeltingResult(itemStack) != null;
                }
            })).build());

    /**
     * The oredict entries of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("oredict")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    ImmutableList.Builder<ValueTypeString.ValueString> builder = ImmutableList.builder();
                    if(a.getRawValue().isPresent()) {
                        for (int i : OreDictionary.getOreIDs(a.getRawValue().get())) {
                            builder.add(ValueTypeString.ValueString.of(OreDictionary.getOreName(i)));
                        }
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, builder.build());
                }
            }).build());

    /**
     * Get a list of items that correspond to the given oredict key.
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT_STACKS = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX
            .output(ValueTypes.LIST).symbolOperator("oredict")
            .inputType(ValueTypes.STRING).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeString.ValueString a = variables.getValue(0);
                    ImmutableList.Builder<ValueObjectTypeItemStack.ValueItemStack> builder = ImmutableList.builder();
                    if (!StringUtils.isNullOrEmpty(a.getRawValue())) {
                        for (ItemStack itemStack : OreDictionary.getOres(a.getRawValue())) {
                            builder.add(ValueObjectTypeItemStack.ValueItemStack.of(itemStack));
                        }
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, builder.build());
                }
            }).build());

    /**
     * ItemStack operator that applies the given stacksize to the given itemstack and creates a new ItemStack.
     */
    public static final IOperator OBJECT_ITEMSTACK_WITHSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_INTEGER_1
            .output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("withsize")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    if (a.getRawValue().isPresent()) {
                        ItemStack itemStack = a.getRawValue().get().copy();
                        itemStack.stackSize = b.getRawValue();
                        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                    }
                    return a;
                }
            }).build());

    /**
     * Check if the item is an RF container item
     */
    public static final IOperator OBJECT_ITEMSTACK_ISFECONTAINER = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isfecontainer")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_BOOLEAN.build(new IOperatorValuePropagator<IEnergyStorage, Boolean>() {
                @Override
                public Boolean getOutput(IEnergyStorage input) throws EvaluationException {
                    return input != null;
                }
            })).build());

    /**
     * Get the storage energy
     */
    public static final IOperator OBJECT_ITEMSTACK_STOREDFE = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("storedfe")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<IEnergyStorage, Integer>() {
                @Override
                public Integer getOutput(IEnergyStorage input) throws EvaluationException {
                    return input != null ? input.getEnergyStored() : 0;
                }
            })).build());

    /**
     * Get the energy capacity
     */
    public static final IOperator OBJECT_ITEMSTACK_FECAPACITY = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("fecapacity")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(new IOperatorValuePropagator<IEnergyStorage, Integer>() {
                @Override
                public Integer getOutput(IEnergyStorage input) throws EvaluationException {
                    return input != null ? input.getMaxEnergyStored() : 0;
                }
            })).build());

    /**
     * ----------------------------------- ENTITY OBJECT OPERATORS -----------------------------------
     */

    /**
     * If the entity is a mob
     */
    public static final IOperator OBJECT_ENTITY_ISMOB = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("ismob")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity instanceof IMob;
                }
            })).build());

    /**
     * If the entity is an animal
     */
    public static final IOperator OBJECT_ENTITY_ISANIMAL = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isanimal")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity instanceof IAnimals && !(entity instanceof IMob);
                }
            })).build());

    /**
     * If the entity is an item
     */
    public static final IOperator OBJECT_ENTITY_ISITEM = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isitem")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity instanceof EntityItem;
                }
            })).build());

    /**
     * If the entity is a player
     */
    public static final IOperator OBJECT_ENTITY_ISPLAYER = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isplayer")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity instanceof EntityPlayer;
                }
            })).build());

    /**
     * The itemstack from the entity
     */
    public static final IOperator OBJECT_ENTITY_ITEMSTACK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("item")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables.getValue(0)).getRawValue();
                    return ValueObjectTypeItemStack.ValueItemStack.of((a.isPresent() && a.get() instanceof EntityItem) ? ((EntityItem) a.get()).getEntityItem() : null);
                }
            }).build());

    /**
     * The entity health
     */
    public static final IOperator OBJECT_ENTITY_HEALTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("health")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(new IOperatorValuePropagator<Entity, Double>() {
                @Override
                public Double getOutput(Entity entity) throws EvaluationException {
                    return (entity instanceof EntityLivingBase) ? (double) ((EntityLivingBase) entity).getHealth() : 0;
                }
            })).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_WIDTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("width")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(new IOperatorValuePropagator<Entity, Double>() {
                @Override
                public Double getOutput(Entity entity) throws EvaluationException {
                    return (entity != null) ? (double) entity.width : 0;
                }
            })).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_HEIGHT = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("height")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(new IOperatorValuePropagator<Entity, Double>() {
                @Override
                public Double getOutput(Entity entity) throws EvaluationException {
                    return (entity != null) ? (double) entity.height : 0;
                }
            })).build());

    /**
     * If the entity is burning
     */
    public static final IOperator OBJECT_ENTITY_ISBURNING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isburning")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity != null && entity.isBurning();
                }
            })).build());

    /**
     * If the entity is wet
     */
    public static final IOperator OBJECT_ENTITY_ISWET = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("iswet")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity != null && entity.isWet();
                }
            })).build());

    /**
     * If the entity is sneaking
     */
    public static final IOperator OBJECT_ENTITY_ISSNEAKING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("issneaking")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity != null && entity.isSneaking();
                }
            })).build());

    /**
     * If the entity is eating
     */
    public static final IOperator OBJECT_ENTITY_ISEATING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("iseating")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(new IOperatorValuePropagator<Entity, Boolean>() {
                @Override
                public Boolean getOutput(Entity entity) throws EvaluationException {
                    return entity != null && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getItemInUseCount() > 0;
                }
            })).build());

    /**
     * The list of armor itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_ARMORINVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("armorinventory")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables.getValue(0)).getRawValue();
                    if(a.isPresent()) {
                        Entity entity = a.get();
                        return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityArmorInventory(entity.worldObj, entity));
                    } else {
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Collections.<ValueObjectTypeEntity.ValueEntity>emptyList());
                    }
                }
            }).build());

    /**
     * The list of itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_INVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("inventory")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<Entity> a = ((ValueObjectTypeEntity.ValueEntity) variables.getValue(0)).getRawValue();
                    if(a.isPresent()) {
                        Entity entity = a.get();
                        return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityInventory(entity.worldObj, entity));
                    } else {
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Collections.<ValueObjectTypeEntity.ValueEntity>emptyList());
                    }
                }
            }).build());

    /**
     * The name of the mod owning this entity
     */
    public static final IOperator OBJECT_ENTITY_MODNAME = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    String modName = "";
                    if(a.getRawValue().isPresent()) {
                        try {
                            Entity entity = a.getRawValue().get();
                            EntityRegistry.EntityRegistration entityRegistration = EntityRegistry.instance().lookupModSpawn(entity.getClass(), true);
                            modName = entityRegistration.getContainer().getName();
                        } catch (NullPointerException e) {
                            modName = "Minecraft";
                        }
                    }
                    return ValueTypeString.ValueString.of(modName);
                }
            }).build());

    /**
     * The block the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETBLOCK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_BLOCK).symbolOperator("targetblock")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    IBlockState blockState = null;
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        EntityLivingBase entity = (EntityLivingBase) a.getRawValue().get();
                        double reachDistance = 5;
                        double eyeHeight = entity.getEyeHeight();
                        if(entity instanceof EntityPlayerMP) {
                            reachDistance = ((EntityPlayerMP) entity).interactionManager.getBlockReachDistance();
                        }
                        Vec3d lookVec = entity.getLookVec();
                        Vec3d origin = new Vec3d(entity.posX, entity.posY + eyeHeight, entity.posZ);
                        Vec3d direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

                        RayTraceResult mop = entity.worldObj.rayTraceBlocks(origin, direction, true);
                        if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                            blockState = entity.worldObj.getBlockState(mop.getBlockPos());
                        }
                    }
                    return ValueObjectTypeBlock.ValueBlock.of(blockState);
                }
            }).build());

    /**
     * The entity the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETENTITY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ENTITY).symbolOperator("targetentity")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    Entity entityOut = null;
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        EntityLivingBase entity = (EntityLivingBase) a.getRawValue().get();
                        double reachDistance = 5;
                        double eyeHeight = entity.getEyeHeight();
                        if(entity instanceof EntityPlayerMP) {
                            reachDistance = ((EntityPlayerMP) entity).interactionManager.getBlockReachDistance();
                        }
                        Vec3d lookVec = entity.getLookVec();
                        Vec3d origin = new Vec3d(entity.posX, entity.posY + eyeHeight, entity.posZ);
                        Vec3d direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

                        float size = entity.getCollisionBorderSize();
                        List<Entity> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity,
                                entity.getEntityBoundingBox().addCoord(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance)
                                        .expand((double) size, (double) size, (double) size));
                        for (Entity e : list) {
                            if (e.canBeCollidedWith()) {
                                float f10 = e.getCollisionBorderSize();
                                AxisAlignedBB axisalignedbb = e.getEntityBoundingBox().expand((double) f10, (double) f10, (double) f10);
                                RayTraceResult mop = axisalignedbb.calculateIntercept(origin, direction);

                                if (axisalignedbb.isVecInside(origin)) {
                                    entityOut = e;
                                } else if (mop != null) {
                                    double distance = origin.distanceTo(mop.hitVec);
                                    if (distance < reachDistance || reachDistance == 0.0D) {
                                        if (e == entity.getRidingEntity() && !entity.canRiderInteract()) {
                                            if (reachDistance == 0.0D) {
                                                entityOut = e;
                                            }
                                        } else {
                                            entityOut = e;
                                            reachDistance = distance;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return ValueObjectTypeEntity.ValueEntity.of(entityOut);
                }
            }).build());

    /**
     * If the given player has an external gui open.
     */
    public static final IOperator OBJECT_PLAYER_HASGUIOPEN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("hasguiopen")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityPlayer) {
                        EntityPlayer entity = (EntityPlayer) a.getRawValue().get();
                        return ValueTypeBoolean.ValueBoolean.of(entity.openContainer != entity.inventoryContainer);
                    }
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }).build());

    /**
     * The item the given entity is currently holding in its main hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_MAIN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbol("helditem1").operatorName("helditem")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    ItemStack itemStack = null;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        itemStack = ((EntityLivingBase) a.getRawValue().get()).getHeldItemMainhand();
                    }
                    return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                }
            }).build());

    /**
     * The item the given entity is currently holding in its off hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_OFF = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbol("helditem2").operatorName("helditemoffhand")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    ItemStack itemStack = null;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        itemStack = ((EntityLivingBase) a.getRawValue().get()).getHeldItemOffhand();
                    }
                    return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                }
            }).build());

    /**
     * The entity's mounted entity
     */
    public static final IOperator OBJECT_ENTITY_MOUNTED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.LIST).symbolOperator("mounted")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    List<ValueObjectTypeEntity.ValueEntity> passengers = Lists.newArrayList();
                    if(a.getRawValue().isPresent()) {
                        for (Entity passenger : a.getRawValue().get().getPassengers()) {
                            passengers.add(ValueObjectTypeEntity.ValueEntity.of(passenger));
                        }
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, passengers);
                }
            }).build());

    /**
     * The item frame's contents
     */
    public static final IOperator OBJECT_ITEMFRAME_CONTENTS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("itemframecontents")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    ItemStack itemStack = null;
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityItemFrame) {
                        itemStack = ((EntityItemFrame) a.getRawValue().get()).getDisplayedItem();
                    }
                    return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                }
            }).build());

    /**
     * The item frame's rotation
     */
    public static final IOperator OBJECT_ITEMFRAME_ROTATION = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER).symbolOperator("itemframerotation")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    Integer rotation = 0;
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityItemFrame) {
                        rotation = ((EntityItemFrame) a.getRawValue().get()).getRotation();
                    }
                    return ValueTypeInteger.ValueInteger.of(rotation);
                }
            }).build());

    /**
     * The hurtsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_HURTSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("hurtsound")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    String hurtSound = "";
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        String sound = ObfuscationHelpers.getEntityLivingBaseHurtSound((EntityLivingBase) a.getRawValue().get()).getSoundName().toString();
                        if (sound != null) {
                            hurtSound = sound;
                        }
                    }
                    return ValueTypeString.ValueString.of(hurtSound);
                }
            }).build());

    /**
     * The deathsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_DEATHSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("deathsound")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    String hurtSound = "";
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        String sound = ObfuscationHelpers.getEntityLivingBaseDeathSound((EntityLivingBase) a.getRawValue().get()).getSoundName().toString();
                        if (sound != null) {
                            hurtSound = sound;
                        }
                    }
                    return ValueTypeString.ValueString.of(hurtSound);
                }
            }).build());

    /**
     * The age of this entity.
     */
    public static final IOperator OBJECT_ENTITY_AGE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER).symbolOperator("age")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    int age = 0;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        age = ((EntityLivingBase) a.getRawValue().get()).getAge();
                    }
                    return ValueTypeInteger.ValueInteger.of(age);
                }
            }).build());

    /**
     * If the entity is a child.
     */
    public static final IOperator OBJECT_ENTITY_ISCHILD = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("ischild")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    boolean child = false;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        child = ((EntityLivingBase) a.getRawValue().get()).isChild();
                    }
                    return ValueTypeBoolean.ValueBoolean.of(child);
                }
            }).build());

    /**
     * If the entity can be bred.
     */
    public static final IOperator OBJECT_ENTITY_CANBREED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("canbreed")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    boolean canBreed = false;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityAgeable) {
                        canBreed = ((EntityAgeable) a.getRawValue().get()).getGrowingAge() == 0;
                    }
                    return ValueTypeBoolean.ValueBoolean.of(canBreed);
                }
            }).build());

    /**
     * If the entity is in love.
     */
    public static final IOperator OBJECT_ENTITY_ISINLOVE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("isinlove")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    boolean inLove = false;
                    if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityAnimal) {
                        inLove = ((EntityAnimal) a.getRawValue().get()).isInLove();
                    }
                    return ValueTypeBoolean.ValueBoolean.of(inLove);
                }
            }).build());

    /**
     * If the entity can be bred with the given item.
     */
    public static final IOperator OBJECT_ENTITY_CANBREEDWITH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .inputTypes(ValueTypes.OBJECT_ENTITY, ValueTypes.OBJECT_ITEMSTACK)
            .output(ValueTypes.BOOLEAN).symbolOperator("canbreedwith")
            .renderPattern(IConfigRenderPattern.INFIX)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    ValueObjectTypeItemStack.ValueItemStack b = variables.getValue(1);
                    boolean canBreedWith = false;
                    if (a.getRawValue().isPresent() && b.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityAnimal) {
                        canBreedWith = ((EntityAnimal) a.getRawValue().get()).isBreedingItem(b.getRawValue().get());
                    }
                    return ValueTypeBoolean.ValueBoolean.of(canBreedWith);
                }
            }).build());

    /**
     * If the entity is shearable.
     */
    public static final IOperator OBJECT_ENTITY_ISSHEARABLE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("isshearable")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                            && a.getRawValue().get() instanceof IShearable
                            && ((IShearable) a.getRawValue().get()).isShearable(null, null, null));
                }
            }).build());

    /**
     * ----------------------------------- FLUID STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * The amount of fluid in the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_AMOUNT = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("amount")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(new IOperatorValuePropagator<FluidStack, Integer>() {
                @Override
                public Integer getOutput(FluidStack fluidStack) throws EvaluationException {
                    return fluidStack != null ? fluidStack.amount : 0;
                }
            })).build());

    /**
     * The block from the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_BLOCK = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("block")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables.getValue(0)).getRawValue();
                    return ValueObjectTypeBlock.ValueBlock.of(a.isPresent() ? a.get().getFluid().getBlock().getDefaultState() : null);
                }
            }).build());

    /**
     * The fluidstack luminosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_LUMINOSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("luminosity")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(new IOperatorValuePropagator<FluidStack, Integer>() {
                @Override
                public Integer getOutput(FluidStack fluidStack) throws EvaluationException {
                    return fluidStack != null ? fluidStack.getFluid().getLuminosity(fluidStack) : 0;
                }
            })).build());

    /**
     * The fluidstack density
     */
    public static final IOperator OBJECT_FLUIDSTACK_DENSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("density")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(new IOperatorValuePropagator<FluidStack, Integer>() {
                @Override
                public Integer getOutput(FluidStack fluidStack) throws EvaluationException {
                    return fluidStack != null ? fluidStack.getFluid().getDensity(fluidStack) : 0;
                }
            })).build());

    /**
     * The fluidstack viscosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_VISCOSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("viscosity")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(new IOperatorValuePropagator<FluidStack, Integer>() {
                @Override
                public Integer getOutput(FluidStack fluidStack) throws EvaluationException {
                    return fluidStack != null ? fluidStack.getFluid().getViscosity(fluidStack) : 0;
                }
            })).build());

    /**
     * If the fluidstack is gaseous
     */
    public static final IOperator OBJECT_FLUIDSTACK_ISGASEOUS = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("isgaseous")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_BOOLEAN.build(new IOperatorValuePropagator<FluidStack, Boolean>() {
                @Override
                public Boolean getOutput(FluidStack fluidStack) throws EvaluationException {
                    return fluidStack != null && fluidStack.getFluid().isGaseous(fluidStack);
                }
            })).build());

    /**
     * The rarity of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_RARITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("rarity")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables.getValue(0)).getRawValue();
                    return ValueTypeString.ValueString.of(a.isPresent() ? a.get().getFluid().getRarity(a.get()).rarityName : "");
                }
            }).build());

    /**
     * If the fluid types of the two given fluidstacks are equal
     */
    public static final IOperator OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL = REGISTRY.register(OperatorBuilders.FLUIDSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwfluidequal")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    Optional<FluidStack> a = ((ValueObjectTypeFluidStack.ValueFluidStack) variables.getValue(0)).getRawValue();
                    Optional<FluidStack> b = ((ValueObjectTypeFluidStack.ValueFluidStack) variables.getValue(1)).getRawValue();
                    boolean equal = false;
                    if(a.isPresent() && b.isPresent()) {
                        equal = a.get().isFluidEqual(b.get());
                    } else if(!a.isPresent() && !b.isPresent()) {
                        equal = true;
                    }
                    return ValueTypeBoolean.ValueBoolean.of(equal);
                }
            }).build());

    /**
     * The name of the mod owning this fluid
     */
    public static final IOperator OBJECT_FLUIDSTACK_MODNAME = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeFluidStack.ValueFluidStack a = variables.getValue(0);
                    String modName = "";
                    if (a.getRawValue().isPresent()) {
                        try {
                            Fluid fluid = a.getRawValue().get().getFluid();
                            String modDomain = null;
                            if (fluid.getStill() != null) {
                                modDomain = fluid.getStill().getResourceDomain();
                            } else if (fluid.getFlowing() != null) {
                                modDomain = fluid.getFlowing().getResourceDomain();
                            } else if (fluid.getBlock() != null) {
                                modDomain = Block.REGISTRY.getNameForObject(fluid.getBlock()).getResourceDomain();
                            }
                            String modId = org.cyclops.cyclopscore.helper.Helpers.getModId(modDomain);
                            modName = Loader.instance().getIndexedModList().get(modId).getName();
                        } catch (NullPointerException e) {
                            modName = "Minecraft";
                        }
                    }
                    return ValueTypeString.ValueString.of(modName);
                }
            }).build());

    /**
     * ----------------------------------- OPERATOR OPERATORS -----------------------------------
     */

    /**
     * Apply for a given operator a given value.
     */
    public static final IOperator OPERATOR_APPLY = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER)
            .output(ValueTypes.CATEGORY_ANY).symbolOperator("apply")
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(ValueTypes.LIST))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR.build(
                    new IOperatorValuePropagator<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue>() {
                        @Override
                        public IValue getOutput(Pair<IOperator, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                            IOperator innerOperator = input.getLeft();
                            OperatorBase.SafeVariablesGetter variables = input.getRight();
                            IVariable variable = variables.getVariables()[0];
                            if (innerOperator.getRequiredInputLength() == 1) {
                                return innerOperator.evaluate(new IVariable[]{variable});
                            } else {
                                return ValueTypeOperator.ValueOperator.of(new CurriedOperator(innerOperator, variable));
                            }
                        }
                    })).build());
    static {
        REGISTRY.registerSerializer(new CurriedOperator.Serializer());
    }

    /**
     * Apply the given operator on all elements of a list, resulting in a new list of mapped values.
     */
    public static final IOperator OPERATOR_MAP = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST})
            .output(ValueTypes.LIST).symbolOperator("map")
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR_LIST.build(
                    new IOperatorValuePropagator<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue>() {
                        @Override
                        public IValue getOutput(Pair<IOperator, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                            final IOperator innerOperator = input.getLeft();
                            OperatorBase.SafeVariablesGetter variables = input.getRight();
                            ValueTypeList.ValueList inputList = variables.getValue(0);
                            return ValueTypeList.ValueList.ofFactory(
                                    new ValueTypeListProxyOperatorMapped(innerOperator, inputList.getRawValue()));
                        }
                    })).build());

    /**
     * Filter a list of elements by matching them all with the given predicate.
     */
    public static final IOperator OPERATOR_FILTER = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST})
            .output(ValueTypes.LIST).symbolOperator("filter")
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR_LIST.build(
                    new IOperatorValuePropagator<Pair<IOperator, OperatorBase.SafeVariablesGetter>, IValue>() {
                        @Override
                        public IValue getOutput(Pair<IOperator, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                            final IOperator innerOperator = input.getLeft();
                            OperatorBase.SafeVariablesGetter variables = input.getRight();
                            ValueTypeList.ValueList<?, ?> inputList = variables.getValue(0);
                            List<IValue> filtered = Lists.newArrayList();
                            for (IValue value : inputList.getRawValue()) {
                                IValue result = ValueHelpers.evaluateOperator(innerOperator, value);
                                if (result.getType() != ValueTypes.BOOLEAN) {
                                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                            L10NValues.VALUETYPE_ERROR_WRONGPREDICATE,
                                            OPERATOR_FILTER.getLocalizedNameFull(),
                                            result.getType(), ValueTypes.BOOLEAN);
                                    throw new EvaluationException(error.localize());
                                } else if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                                    filtered.add(value);
                                }
                            }
                            IValueType valueType = inputList.getRawValue().getValueType();
                            return ValueTypeList.ValueList.ofList(valueType, filtered);
                        }
                    })).build());

    /**
     * Takes the conjunction of two predicates.
     */
    public static final IOperator OPERATOR_CONJUNCTION = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol(".&&.").operatorName("conjunction")
            .function(OperatorBuilders.FUNCTION_TWO_PREDICATES.build(new IOperatorValuePropagator<Pair<IOperator, IOperator>, IValue>() {
                @Override
                public IValue getOutput(Pair<IOperator, IOperator> input) throws EvaluationException {
                    return ValueTypeOperator.ValueOperator.of(CombinedOperator.Conjunction.asOperator(input.getLeft(), input.getRight()));
                }
            })).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Conjunction.Serializer());
    }

    /**
     * Takes the disjunction of two predicates.
     */
    public static final IOperator OPERATOR_DISJUNCTION = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol(".||.").operatorName("disjunction")
            .function(OperatorBuilders.FUNCTION_TWO_PREDICATES.build(new IOperatorValuePropagator<Pair<IOperator, IOperator>, IValue>() {
                @Override
                public IValue getOutput(Pair<IOperator, IOperator> input) throws EvaluationException {
                    return ValueTypeOperator.ValueOperator.of(CombinedOperator.Disjunction.asOperator(input.getLeft(), input.getRight()));
                }
            })).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Disjunction.Serializer());
    }

    /**
     * Takes the negation of a predicate.
     */
    public static final IOperator OPERATOR_NEGATION = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol("!.").operatorName("negation")
            .function(OperatorBuilders.FUNCTION_ONE_PREDICATE.build(new IOperatorValuePropagator<IOperator, IValue>() {
                @Override
                public IValue getOutput(IOperator input) throws EvaluationException {
                    return ValueTypeOperator.ValueOperator.of(CombinedOperator.Negation.asOperator(input));
                }
            })).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Negation.Serializer());
    }

    /**
     * Create a new operator that pipes the output from the first operator to the second operator.
     */
    public static final IOperator OPERATOR_PIPE = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol(".").operatorName("pipe")
            .function(OperatorBuilders.FUNCTION_TWO_OPERATORS.build(new IOperatorValuePropagator<Pair<IOperator, IOperator>, IValue>() {
                @Override
                public IValue getOutput(Pair<IOperator, IOperator> input) throws EvaluationException {
                    return ValueTypeOperator.ValueOperator.of(CombinedOperator.Pipe.asOperator(input.getLeft(), input.getRight()));
                }
            })).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Pipe.Serializer());
    }

    /**
     * Flip the input parameters of an operator with two inputs.
     */
    public static final IOperator OPERATOR_FLIP = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbolOperator("flip")
            .function(OperatorBuilders.FUNCTION_ONE_OPERATOR.build(new IOperatorValuePropagator<IOperator, IValue>() {
                @Override
                public IValue getOutput(IOperator input) throws EvaluationException {
                    return ValueTypeOperator.ValueOperator.of(CombinedOperator.Flip.asOperator(input));
                }
            })).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Flip.Serializer());
    }

    /**
     * Apply the given operator on all elements of a list to reduce the list to one value.
     */
    public static final IOperator OPERATOR_REDUCE = REGISTRY.register(OperatorBuilders.OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
            .output(ValueTypes.CATEGORY_ANY).symbolOperator("reduce")
            .conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
                @Override
                public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input) {
                    return input[2].getType();
                }
            })
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValue accumulator = variables.getValue(2);
                    final IOperator innerOperator = OperatorBuilders.getSafeOperator((ValueTypeOperator.ValueOperator)
                            variables.getValue(0), accumulator.getType());
                    ValueTypeList.ValueList<IValueType<IValue>, IValue> inputList = variables.getValue(1);
                    for (IValue listValue : inputList.getRawValue()) {
                        accumulator = innerOperator.evaluate(new IVariable[]{
                                new Variable<>(accumulator.getType(), accumulator),
                                new Variable<>(listValue.getType(), listValue)});
                    }
                    return accumulator;
                }
            }).build());

    /**
     * ----------------------------------- GENERAL OPERATORS -----------------------------------
     */

    /**
     * Choice operator with one boolean input, two any inputs and one output any.
     */
    public static final GeneralOperator GENERAL_CHOICE = REGISTRY.register(new GeneralChoiceOperator("?", "choice"));

    /**
     * Identity operator with one any input and one any output
     */
    public static final GeneralOperator GENERAL_IDENTITY = REGISTRY.register(new GeneralIdentityOperator("id", "identity"));

}
