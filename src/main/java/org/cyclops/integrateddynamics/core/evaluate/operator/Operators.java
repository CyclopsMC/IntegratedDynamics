package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.*;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
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
    public static final IOperator RELATIONAL_GT = REGISTRY.register(OperatorBuilders.RELATIONAL_2.symbol(">").operatorName("gt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue() > b.getRawValue());
                }
            }).build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_LT = REGISTRY.register(OperatorBuilders.RELATIONAL_2.symbol("<").operatorName("lt")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0);
                    ValueTypeInteger.ValueInteger b = variables.getValue(1);
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue() < b.getRawValue());
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
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && a.getRawValue().get().getBlock().isOpaqueCube());
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
                            return a.getRawValue().isPresent() ? GameData.getBlockRegistry().getNameForObject(a.getRawValue().get().getBlock()) : null;
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
                    new IOperatorValuePropagator<Optional<Block.SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<Block.SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getBreakSound();
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
                    new IOperatorValuePropagator<Optional<Block.SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<Block.SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getPlaceSound();
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
                    new IOperatorValuePropagator<Optional<Block.SoundType>, String>() {
                        @Override
                        public String getOutput(Optional<Block.SoundType> sound) throws EvaluationException {
                            if (sound.isPresent()) {
                                return sound.get().getStepSound();
                            }
                            return "";
                        }
                    },
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());

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
                    return ValueTypeDouble.ValueDouble.of(a.getRawValue().isPresent() && b.getRawValue().isPresent() ? a.getRawValue().get().getStrVsBlock(b.getRawValue().get().getBlock()) : 0);
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
                    return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && b.getRawValue().isPresent() && a.getRawValue().get().canHarvestBlock(b.getRawValue().get().getBlock()));
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
     * If the NBT tags of the given stacks are equal
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
     * If the raw items of the given stacks are equal
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
                        equal = ItemStack.areItemsEqual(a.get(), b.get());
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
                            return a.getRawValue().isPresent() ? GameData.getItemRegistry().getNameForObject(a.getRawValue().get().getItem()) : null;
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
     * The oredict entries of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("oredict")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0);
                    List<ValueTypeString.ValueString> names = Lists.newArrayList();
                    if(a.getRawValue().isPresent()) {
                        for (int i : OreDictionary.getOreIDs(a.getRawValue().get())) {
                            names.add(ValueTypeString.ValueString.of(OreDictionary.getOreName(i)));
                        }
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, names);
                }
            }).build());

    /**
     * Get a list of items that correspond to the given oredict key.
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT_STACKS = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX.symbolOperator("oredict")
            .inputType(ValueTypes.STRING).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeString.ValueString a = variables.getValue(0);
                    List<ValueObjectTypeItemStack.ValueItemStack> stacks = Lists.newArrayList();
                    if (!StringUtils.isNullOrEmpty(a.getRawValue())) {
                        for (ItemStack itemStack : OreDictionary.getOres(a.getRawValue())) {
                            stacks.add(ValueObjectTypeItemStack.ValueItemStack.of(itemStack));
                        }
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, stacks);
                }
            }).build());

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
                    return entity != null && entity.isEating();
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
                            reachDistance = ((EntityPlayerMP) entity).theItemInWorldManager.getBlockReachDistance();
                        }
                        Vec3 lookVec = entity.getLookVec();
                        Vec3 origin = new Vec3(entity.posX, entity.posY + eyeHeight, entity.posZ);
                        Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

                        MovingObjectPosition mop = entity.worldObj.rayTraceBlocks(origin, direction, true);
                        if(mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
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
                            reachDistance = ((EntityPlayerMP) entity).theItemInWorldManager.getBlockReachDistance();
                        }
                        Vec3 lookVec = entity.getLookVec();
                        Vec3 origin = new Vec3(entity.posX, entity.posY + eyeHeight, entity.posZ);
                        Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

                        float size = entity.getCollisionBorderSize();
                        List<Entity> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity,
                                entity.getEntityBoundingBox().addCoord(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance)
                                        .expand((double) size, (double) size, (double) size));
                        for (Entity e : list) {
                            if (e.canBeCollidedWith()) {
                                float f10 = e.getCollisionBorderSize();
                                AxisAlignedBB axisalignedbb = e.getEntityBoundingBox().expand((double) f10, (double) f10, (double) f10);
                                MovingObjectPosition mop = axisalignedbb.calculateIntercept(origin, direction);

                                if (axisalignedbb.isVecInside(origin)) {
                                    entityOut = e;
                                } else if (mop != null) {
                                    double distance = origin.distanceTo(mop.hitVec);
                                    if (distance < reachDistance || reachDistance == 0.0D) {
                                        if (e == entity.ridingEntity && !entity.canRiderInteract()) {
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
     * The currently held item of the entity.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("helditem")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    ItemStack itemStack = null;
                    if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                        itemStack = ((EntityLivingBase) a.getRawValue().get()).getHeldItem();
                    }
                    return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                }
            }).build());

    /**
     * The entity's mounted entity
     */
    public static final IOperator OBJECT_ENTITY_MOUNTED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ENTITY).symbolOperator("mounted")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = variables.getValue(0);
                    Entity entityOut = null;
                    if(a.getRawValue().isPresent()) {
                        entityOut = a.getRawValue().get().riddenByEntity;
                    }
                    return ValueObjectTypeEntity.ValueEntity.of(entityOut);
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
                        String sound = ObfuscationHelpers.getEntityLivingBaseHurtSound((EntityLivingBase) a.getRawValue().get());
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
                        String sound = ObfuscationHelpers.getEntityLivingBaseDeathSound((EntityLivingBase) a.getRawValue().get());
                        if (sound != null) {
                            hurtSound = sound;
                        }
                    }
                    return ValueTypeString.ValueString.of(hurtSound);
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
                                modDomain = GameData.getBlockRegistry().getNameForObject(fluid.getBlock()).getResourceDomain();
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
