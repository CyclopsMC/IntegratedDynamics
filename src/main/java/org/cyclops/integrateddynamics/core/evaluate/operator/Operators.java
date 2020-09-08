package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import lombok.Lombok;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesList;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
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
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NbtHelpers;
import org.cyclops.integrateddynamics.core.helper.obfuscation.ObfuscationHelpers;
import org.cyclops.integrateddynamics.core.ingredient.ExtendedIngredientsList;
import org.cyclops.integrateddynamics.core.ingredient.ExtendedIngredientsSingle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
            .function(variables -> {
                ValueTypeBoolean.ValueBoolean a = variables.getValue(0, ValueTypes.BOOLEAN);
                if (!a.getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                } else {
                    return variables.getValue(1, ValueTypes.BOOLEAN);
                }
            }).build());

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_OR = REGISTRY.register(OperatorBuilders.LOGICAL_2.symbol("||").operatorName("or")
            .function(variables -> {
                ValueTypeBoolean.ValueBoolean a = variables.getValue(0, ValueTypes.BOOLEAN);
                if (a.getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(true);
                } else {
                    return variables.getValue(1, ValueTypes.BOOLEAN);
                }
            }).build());

    /**
     * Logical NOT operator with one input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_NOT = REGISTRY.register(OperatorBuilders.LOGICAL_1_PREFIX.symbol("!").operatorName("not")
            .function(variables -> {
                        ValueTypeBoolean.ValueBoolean valueBoolean = variables.getValue(0, ValueTypes.BOOLEAN);
                        return ValueTypeBoolean.ValueBoolean.of(!valueBoolean.getRawValue());
                    }
            ).build());

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
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.add(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MINUS operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("-").operatorName("subtraction")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.subtract(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MULTIPLY operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("*").operatorName("multiplication")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.multiply(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic DIVIDE operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_DIVISION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("/").operatorName("division")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.divide(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MAX operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MAXIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("max").operatorName("maximum")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.max(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MIN operator with two input integers and one output integer.
     */
    public static final IOperator ARITHMETIC_MINIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("min").operatorName("minimum")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.min(variables.getVariables()[0], variables.getVariables()[1])
            ).build());



    /**
     * ----------------------------------- INTEGER OPERATORS -----------------------------------
     */

    private static final ValueTypeInteger.ValueInteger ZERO = ValueTypeInteger.ValueInteger.of(0);

    /**
     * Integer MODULO operator with two input integers and one output integer.
     */
    public static final IOperator INTEGER_MODULUS = REGISTRY.register(OperatorBuilders.INTEGER_2.symbol("%").operatorName("modulus")
            .function(variables -> {
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() == 0) { // You can not divide by zero
                    throw new EvaluationException("Division by zero");
                } else if (b.getRawValue() == 1) { // If b is neutral element for division
                    return ZERO;
                } else {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() % b.getRawValue());
                }
            }).build());

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IOperator INTEGER_INCREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("++").operatorName("increment")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() + 1);
            }).build());

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IOperator INTEGER_DECREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("--").operatorName("decrement")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() - 1);
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
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(variables.getValue(0).equals(variables.getValue(1)))
            )
            .typeValidator((operator, input) -> {
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
                        if(!ValueHelpers.correspondsTo(temporarySecondInputType, inputType)) {
                            return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    operator.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getTranslationKey()),
                                    Integer.toString(i), new L10NHelpers.UnlocalizedString(temporarySecondInputType.getTranslationKey()));
                        }
                    }
                }
                return null;
            })
            .build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_GT = REGISTRY.register(OperatorBuilders.RELATIONAL_2
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol(">").operatorName("gt")
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.greaterThan(variables.getVariables()[0], variables.getVariables()[1]))
            ).build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_LT = REGISTRY.register(OperatorBuilders.RELATIONAL_2
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol("<").operatorName("lt")
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.lessThan(variables.getVariables()[0], variables.getVariables()[1]))
            ).build());

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
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() & b.getRawValue());
            }).build());

    /**
     * Binary OR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_OR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("|").operatorName("or")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() | b.getRawValue());
            }).build());

    /**
     * Binary XOR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_XOR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("^").operatorName("xor")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() ^ b.getRawValue());
            }).build());

    /**
     * Binary COMPLEMENT operator with one input integers and one output integers.
     */
    public static final IOperator BINARY_COMPLEMENT = REGISTRY.register(OperatorBuilders.BINARY_1_PREFIX.symbol("~").operatorName("complement")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(~a.getRawValue());
            }).build());

    /**
     * Binary &lt;&lt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_LSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("<<").operatorName("lshift")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() << b.getRawValue());
            }).build());

    /**
     * Binary &gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>").operatorName("rshift")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() >> b.getRawValue());
            }).build());

    /**
     * Binary &gt;&gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RZSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>>").operatorName("rzshift")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() >>> b.getRawValue());
            }).build());

    /**
     * ----------------------------------- STRING OPERATORS -----------------------------------
     */

    /**
     * String length operator with one input string and one output integer.
     */
    public static final IOperator STRING_LENGTH = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX.symbol("len").operatorName("length")
            .output(ValueTypes.INTEGER).function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue().length());
            }).build());

    /**
     * String concat operator with two input strings and one output string.
     */
    public static final IOperator STRING_CONCAT = REGISTRY.register(OperatorBuilders.STRING_2.symbol("+").operatorName("concat")
            .function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString b = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeString.ValueString.of(a.getRawValue() + b.getRawValue());
            }).build());

    /**
     * String contains operator which checks whether a given (literal) string is contained in the given string.
     */
    public static final IOperator STRING_CONTAINS = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("contains")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().contains(search.getRawValue()));
            }).build());

    /**
     * String match operator which checks whether a given regular expression is contained within a string.
     */
    public static final IOperator STRING_CONTAINS_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("contains_regex")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                    return ValueTypeBoolean.ValueBoolean.of(m.find());
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(e.getMessage());
                }
            }).build());

    /**
     * String match operator which checks whether a given regular expression matches a string.
     */
    public static final IOperator STRING_MATCHES_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("matches_regex")
            .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                    return ValueTypeBoolean.ValueBoolean.of(m.matches());
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(e.getMessage());
                }
            }).build());

    /**
     * String operator which returns the integral index of the first position where the search string appears in the given string.
     */
    public static final IOperator STRING_INDEX_OF = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("index_of")
        .output(ValueTypes.INTEGER).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeInteger.ValueInteger.of(str.getRawValue().indexOf(search.getRawValue()));
            }).build());

    /**
     * String operator which returns the integral index where the a substring matching the regular expression appears in the given string.
     */
    public static final IOperator STRING_INDEX_OF_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("index_of_regex")
        .output(ValueTypes.INTEGER).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                    if (m.find()) {
                        return ValueTypeInteger.ValueInteger.of(m.start());
                    } else {
                        return ValueTypeInteger.ValueInteger.of(-1);
                    }
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(e.getMessage());
                }
            }).build());

    /**
     * String match operator which checks whether a given string matches the beginning of the given string.
     */
    public static final IOperator STRING_STARTS_WITH = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("starts_with")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().startsWith(search.getRawValue()));
            }).build());

    /**
     * String match operator which checks whether a given string matches the end of the given string.
     */
    public static final IOperator STRING_ENDS_WITH = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("ends_with")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().endsWith(search.getRawValue()));
            }).build());

    /**
     * String operator which splits on the given (literal) delimiter the input string .
     */
    public static final IOperator STRING_SPLIT_ON = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("split_on")
        .output(ValueTypes.LIST).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                List<String> pieces = Arrays.asList(str.getRawValue().split(java.util.regex.Pattern.quote(search.getRawValue())));
                List<ValueTypeString.ValueString> values = Lists.newArrayList();
                for (String piece : pieces) {
                    values.add(ValueTypeString.ValueString.of(piece));
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
            }).build());

    /**
     * String operator which splits on the given (regular expression) delimiter the input string.
     */
    public static final IOperator STRING_SPLIT_ON_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("split_on_regex")
        .output(ValueTypes.LIST).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    List<String> pieces = Arrays.asList(Pattern.compile(pattern.getRawValue()).split(str.getRawValue()));
                    List<ValueTypeString.ValueString> values = Lists.newArrayList();
                    for (String piece : pieces) {
                        values.add(ValueTypeString.ValueString.of(piece));
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(e.getMessage());
                }
            }).build());

    /**
     * String operator which takes the substring of the given string between the two integer indices.
     */
    public static final IOperator STRING_SUBSTRING = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("substring")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.INTEGER, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeInteger.ValueInteger from = variables.getValue(0, ValueTypes.INTEGER);
            ValueTypeInteger.ValueInteger to = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (from.getRawValue() > to.getRawValue()) {
                throw new EvaluationException("The 'to' value must not be greater than the 'from' value in the substring operator.");
            }
            if (from.getRawValue() < 0 || to.getRawValue() < 0) {
                throw new EvaluationException("The 'from' and 'to' values in the substring operator must not be negative.");
            }
            int stringLength = str.getRawValue().length();
            if (from.getRawValue() > stringLength || to.getRawValue() > stringLength) {
                throw new EvaluationException("The 'from' and 'to' values in the substring operator must not exceed the length of the string.");
            }
            return ValueTypeString.ValueString.of(str.getRawValue().substring(from.getRawValue(), to.getRawValue()));
        }).build());


    /**
     * String operator which matches against a regex and takes the group at the index of the integer given (including zero), in the input string. It is invalid for the pattern to not match.
     */
    public static final IOperator STRING_REGEX_GROUP = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("regex_group")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.STRING, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeInteger.ValueInteger group = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (group.getRawValue() < 0) {
                throw new EvaluationException("The group index specified in the regex_group operator must not be negative.");
            }
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                if (m.find()) {
                    String result = m.group(group.getRawValue());
                    return ValueTypeString.ValueString.of(result);
                } else {
                    throw new EvaluationException("The regular expression in regex_group must match the given string.");
                }
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                throw new EvaluationException("The group index specified in the regex_group operator must not be greater than the number of groups matched in the regular expression.");
            }
        }).build()
    );

    /**
     * String operator which matches against a regex the input string and returns a list containing all groups matched (including zero). An empty list is returned if the regex does not match.
     */
    public static final IOperator STRING_REGEX_GROUPS = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("regex_groups")
        .output(ValueTypes.LIST)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                if (m.find()) {
                    List<ValueTypeString.ValueString> values = Lists.newArrayList();
                    for (int i = 0; i <= m.groupCount(); i++) {
                        values.add(ValueTypeString.ValueString.of(m.group(i)));
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, Collections.<ValueTypeString.ValueString>emptyList());
                }
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(e.getMessage());
            }
        }).build()
    );

    /**
     * String operator which finds all matches of the regular expression in the given string and returns the given group for each match.
     */
    public static final IOperator STRING_REGEX_SCAN = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("regex_scan")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.STRING, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.LIST)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeInteger.ValueInteger group = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (group.getRawValue() < 0) {
                throw new EvaluationException("The group index specified in the regex_scan operator must not be negative.");
            }
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                List<ValueTypeString.ValueString> values = Lists.newArrayList();
                while (m.find()) {
                    values.add(ValueTypeString.ValueString.of(m.group(group.getRawValue())));
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                throw new EvaluationException("The group index specified in the regex_scan operator must not be greater than the number of groups matched in the regular expression.");
            }
        }).build()
    );

    /**
     * String operator which, finds all the matches of the (literal) search and replaces them with the given replacement, in the input string.
     */
    public static final IOperator STRING_REPLACE = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("replace")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(3, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
            ValueTypeString.ValueString replacement = variables.getValue(1, ValueTypes.STRING);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            return ValueTypeString.ValueString.of(str.getRawValue().replaceAll(java.util.regex.Pattern.quote(search.getRawValue()), java.util.regex.Matcher.quoteReplacement(replacement.getRawValue())));
        }).build()
    );

    /**
     * String operator which, finds all the matches of the regular expression pattern and replaces them with the given replacement, in the input string.
     */
    public static final IOperator STRING_REPLACE_REGEX = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("replace_regex")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(3, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeString.ValueString replacement = variables.getValue(1, ValueTypes.STRING);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            try {
                return ValueTypeString.ValueString.of(Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue()).replaceAll(replacement.getRawValue()));
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(e.getMessage());
            }
        }).build()
    );

    /**
     * String operator to join a list using a string delimiter
     */
    public static final IOperator STRING_JOIN = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("join")
            .renderPattern(IConfigRenderPattern.PREFIX_2)
            .inputTypes(ValueTypes.STRING, ValueTypes.LIST)
            .output(ValueTypes.STRING)
            .function(variables -> {
                // Prepare values
                ValueTypeString.ValueString delimiter = variables.getValue(0, ValueTypes.STRING);
                ValueTypeList.ValueList<?, ?> elements = variables.getValue(1, ValueTypes.LIST);
                if (!ValueHelpers.correspondsTo(elements.getRawValue().getValueType(), ValueTypes.STRING)) {
                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            elements.getRawValue().getValueType(), ValueTypes.STRING);
                    throw new EvaluationException(error.localize());
                }

                // Don't allow joining on an infinite list
                if (elements.getRawValue().isInfinite()) {
                    throw new EvaluationException("Joining elements in an infinite list is not allowed");
                }

                // Join in O(n), while type-checking each element, as the list may have been of ANY type.
                StringBuilder sb = new StringBuilder();
                for (IValue value : elements.getRawValue()) {
                    if (value.getType() != ValueTypes.STRING) {
                        L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                value.getType(), ValueTypes.STRING);
                        throw new EvaluationException(error.localize());
                    }
                    if (sb.length() > 0) {
                        sb.append(delimiter.getRawValue());
                    }
                    sb.append(((ValueTypeString.ValueString) value).getRawValue());
                }

                return ValueTypeString.ValueString.of(sb.toString());
            }).build()
    );

    /**
     * Get a name value type name.
     */
    public static final IOperator NAMED_NAME = REGISTRY.register(OperatorBuilders.STRING_2.symbol("name").operatorName("name")
            .inputType(ValueTypes.CATEGORY_NAMED).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(
                variables -> ValueTypeString.ValueString.of(ValueTypes.CATEGORY_NAMED.getName(variables.getVariables()[0]))
            ).build());

    /**
     * Get a unique name value type name.
     */
    public static final IOperator UNIQUELYNAMED_UNIQUENAME = REGISTRY.register(OperatorBuilders.STRING_2.symbol("uname").operatorName("unique_name")
            .inputType(ValueTypes.CATEGORY_UNIQUELY_NAMED).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(
                variables -> ValueTypeString.ValueString.of(ValueTypes.CATEGORY_UNIQUELY_NAMED.getUniqueName(variables.getVariables()[0]))
            ).build());

    /**
     * ----------------------------------- DOUBLE OPERATORS -----------------------------------
     */

    // TODO: Move these double operators to number operators (and rename unique id) in the next breaking update (MC 1.13)

    /**
     * Double round operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_ROUND = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("|| ||").operatorName("round")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.round(variables.getVariables()[0])
            ).build());

    /**
     * Double ceil operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_CEIL = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("⌈ ⌉").operatorName("ceil")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.ceil(variables.getVariables()[0])
            ).build());

    /**
     * Double floor operator with one input double and one output integers.
     */
    public static final IOperator DOUBLE_FLOOR = REGISTRY.register(OperatorBuilders.DOUBLE_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("⌊ ⌋").operatorName("floor")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.floor(variables.getVariables()[0])
            ).build());

    /**
     * ----------------------------------- NULLABLE OPERATORS -----------------------------------
     */

    /**
     * Check if something is null
     */
    public static final IOperator NULLABLE_ISNULL = REGISTRY.register(OperatorBuilders.NULLABLE_1_PREFIX.symbol("o").operatorName("isnull")
            .inputType(ValueTypes.CATEGORY_ANY).output(ValueTypes.BOOLEAN).function(variables -> {
                if(ValueHelpers.correspondsTo(variables.getVariables()[0].getType(), ValueTypes.CATEGORY_NULLABLE)) {
                    return ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NULLABLE.isNull(variables.getVariables()[0]));
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
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
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                return ValueTypeInteger.ValueInteger.of(a.getLength());
            }).build());

    /**
     * Check if a list is empty
     */
    public static final IOperator LIST_EMPTY = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX.output(ValueTypes.BOOLEAN).symbol("∅").operatorName("empty")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                return ValueTypeBoolean.ValueBoolean.of(a.getLength() == 0);
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
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() < a.getLength() && b.getRawValue() >= 0) {
                    return a.get(b.getRawValue());
                } else {
                    throw new EvaluationException("Index out of bounds. Tried to get element " + b.getRawValue()
                            + " of a list of length " + a.getLength()
                            + ". Use the getOrDefault operator to provide a default when an index is out of bounds.");
                }
            }).conditionalOutputTypeDeriver((operator, input) -> {
                try {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) input[0].getValue()).getRawValue();
                    return a.getValueType();
                } catch (ClassCastException | EvaluationException e) {
                    return operator.getOutputType();
                }
            }).build());

    /**
     * List operator with one input list, one output integer, and one default value
     */
    public static final IOperator LIST_ELEMENT_DEFAULT = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.INTEGER, ValueTypes.CATEGORY_ANY}).output(ValueTypes.CATEGORY_ANY)
            .renderPattern(IConfigRenderPattern.INFIX_2_LONG).symbolOperator("get_or_default")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() < a.getLength() && b.getRawValue() >= 0) {
                    return a.get(b.getRawValue());
                } else {
                    if (!ValueHelpers.correspondsTo(a.getValueType(), variables.getVariables()[2].getType())) {
                        L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                a.getValueType(), variables.getVariables()[2].getType());
                        throw new EvaluationException(error.localize());
                    }
                    return variables.getValue(2);
                }
            }).conditionalOutputTypeDeriver(
                (operator, input) -> input[2].getType()
            ).build());

    /**
     * List contains operator that takes a list, a list element to look for and returns a boolean.
     */
    public static final IOperator LIST_CONTAINS = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.PREFIX_2_LONG)
            .output(ValueTypes.BOOLEAN).symbolOperator("contains")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                IValue input = variables.getValue(1);
                for (IValue value : list) {
                    if (value.equals(input)) {
                        return ValueTypeBoolean.ValueBoolean.of(true);
                    }
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
            }).build());

    /**
     * List contains operator that takes a list, a predicate that maps a list element to a boolean, a list element and returns a boolean.
     */
    public static final IOperator LIST_CONTAINS_PREDICATE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX)
            .output(ValueTypes.BOOLEAN).symbolOperator("contains_p")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                IOperator operator = OperatorBuilders.getSafePredictate(variables.getValue(1, ValueTypes.OPERATOR));
                for (IValue value : list) {
                    IValue result = ValueHelpers.evaluateOperator(operator, value);
                    ValueHelpers.validatePredicateOutput(operator, result);
                    if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                        return ValueTypeBoolean.ValueBoolean.of(true);
                    }
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
            }).build());

    /**
     * List operator with one input list, and element and one output integer
     */
    public static final IOperator LIST_COUNT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.INTEGER)
            .symbolOperator("count")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                if (list.isInfinite()) {
                    throw new EvaluationException("Counting elements in an infinite list is not allowed");
                }
                IValue value = variables.getValue(1);
                int count = 0;
                for (IValue listValue : list) {
                    if (listValue.equals(value)) {
                        count++;
                    }
                }
                return ValueTypeInteger.ValueInteger.of(count);
            }).build());

    /**
     * List operator with one input list, a predicate and one output integer
     */
    public static final IOperator LIST_COUNT_PREDICATE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.INTEGER)
            .symbolOperator("count_p")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                if (list.isInfinite()) {
                    throw new EvaluationException("Counting elements in an infinite list is not allowed");
                }
                IOperator operator = OperatorBuilders.getSafePredictate(variables.getValue(1, ValueTypes.OPERATOR));
                int count = 0;
                for (IValue listValue : list) {
                    IValue result = ValueHelpers.evaluateOperator(operator, listValue);
                    ValueHelpers.validatePredicateOutput(operator, result);
                    if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                        count++;
                    }
                }
                return ValueTypeInteger.ValueInteger.of(count);
            }).build());

    /**
     * Append an element to the given list
     */
    public static final IOperator LIST_APPEND = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("append")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                IValue value = variables.getValue(1);
                if (!ValueHelpers.correspondsTo(a.getValueType(), value.getType())) {
                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            a.getValueType(), value.getType());
                    throw new EvaluationException(error.localize());
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyAppend(a, value));
            }).build());

    /**
     * Concatenate two lists
     */
    public static final IOperator LIST_CONCAT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.LIST})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("concat")
            .function(variables -> {
                ValueTypeList.ValueList valueList0 = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList0.getRawValue();
                ValueTypeList.ValueList valueList1 = variables.getValue(1, ValueTypes.LIST);
                IValueTypeListProxy b = valueList1.getRawValue();
                if (!ValueHelpers.correspondsTo(a.getValueType(), b.getValueType())) {
                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            a.getValueType(), b.getValueType());
                    throw new EvaluationException(error.localize());
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyConcat(a, b));
            }).build());

    /**
     * Build a list lazily using a start value and an operator that is applied to the previous element to get a next element.
     */
    public static final IOperator LIST_LAZYBUILT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.CATEGORY_ANY, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("lazybuilt")
            .function(variables -> {
                IValue a = variables.getValue(0);
                IOperator operator = OperatorBuilders.getSafeOperator(variables.getValue(1, ValueTypes.OPERATOR), a.getType());
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyLazyBuilt<>(a, operator));
            }).build());

    /**
     * Get the first element of the given list.
     */
    public static final IOperator LIST_HEAD = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX
            .inputTypes(new IValueType[]{ValueTypes.LIST}).output(ValueTypes.CATEGORY_ANY)
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).symbolOperator("head")
            .function(variables -> {
                ValueTypeList.ValueList list = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = list.getRawValue();
                if (a.getLength() > 0) {
                    return a.get(0);
                } else {
                    throw new EvaluationException("Index out of bounds. Tried to get the head of a list of length "
                            + a.getLength() + ". Use the getOrDefault operator to provide a default when an " +
                            "index is out of bounds.");
                }
            }).conditionalOutputTypeDeriver((operator, input) -> {
                try {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) input[0].getValue()).getRawValue();
                    return a.getValueType();
                } catch (EvaluationException e) {
                    return operator.getOutputType();
                }
            }).build());

    /**
     * Append an element to the given list.
     */
    public static final IOperator LIST_TAIL = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST})
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).output(ValueTypes.LIST)
            .symbolOperator("tail")
            .function(variables -> {
                ValueTypeList.ValueList list = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = list.getRawValue();
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyTail(a));
            }).build());

    /**
     * Deduplicate the given list elements based on the given predicate.
     */
    public static final IOperator LIST_UNIQ_PREDICATE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("uniq_p")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                final IOperator operator = OperatorBuilders.getSafePredictate(variables.getValue(1, ValueTypes.OPERATOR));
                List<IValue> values = new ArrayList<>();
                outerLoop:
                for(IValue value : list) {
                    for(IValue existing : values) {
                        IValue result;
                        try {
                            result = ValueHelpers.evaluateOperator(operator, value, existing);
                            ValueHelpers.validatePredicateOutput(operator, result);
                        } catch (EvaluationException e) {
                            throw Lombok.sneakyThrow(e);
                        }
                        if(((ValueTypeBoolean.ValueBoolean) result).getRawValue()) continue outerLoop;
                    }
                    values.add(value);
                }
                return ValueTypeList.ValueList.ofList(list.getValueType(), values);
            }).build());

    /**
     * Deduplicate the given list elements.
     */
    public static final IOperator LIST_UNIQ = REGISTRY.register(OperatorBuilders.LIST
            .inputType(ValueTypes.LIST)
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).output(ValueTypes.LIST)
            .symbolOperator("uniq")
            .function(variables -> {
                ValueTypeList.ValueList valueList =variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                return ValueTypeList.ValueList.ofList(list.getValueType(), new ArrayList<>(Sets.newLinkedHashSet(list)));
            }).build());

    /**
     * Take a subset of the given list from the given index (inclusive) to the given index (exclusive).
     */
    public static final IOperator LIST_SLICE = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(ValueTypes.LIST, ValueTypes.INTEGER, ValueTypes.INTEGER)
            .renderPattern(IConfigRenderPattern.PREFIX_3).output(ValueTypes.LIST)
            .symbolOperator("slice")
            .function(variables -> {
                ValueTypeList.ValueList valueList =variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                ValueTypeInteger.ValueInteger from = variables.getValue(1, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger to = variables.getValue(2, ValueTypes.INTEGER);
                if (from.getRawValue() >= to.getRawValue()) {
                    throw new EvaluationException("The 'from' value must be strictly smaller than the 'to' value in the slice operator.");
                }
                if (from.getRawValue() < 0 || to.getRawValue() < 0){
                    throw new EvaluationException("The 'from' and 'to' values in the slice operator must not be negative.");
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxySlice<>(list, from.getRawValue(), to.getRawValue()));
            }).build());

    /**
     * ----------------------------------- BLOCK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Block isOpaque operator with one input block and one output boolean.
     */
    public static final IOperator OBJECT_BLOCK_OPAQUE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("opaque")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && a.getRawValue().get().isOpaqueCube());
            }).build());

    /**
     * The itemstack representation of the block
     */
    public static final IOperator OBJECT_BLOCK_ITEMSTACK = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("itemstack")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueObjectTypeItemStack.ValueItemStack.of(a.getRawValue().isPresent() ? BlockHelpers.getItemStackFromBlockState(a.getRawValue().get()) : ItemStack.EMPTY);
            }).build());

    /**
     * The name of the mod owning this block
     */
    public static final IOperator OBJECT_BLOCK_MODNAME = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                        return a.getRawValue().isPresent() ? Block.REGISTRY.getNameForObject(a.getRawValue().get().getBlock()) : null;
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The breaksound of the block
     */
    public static final IOperator OBJECT_BLOCK_BREAKSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("break_sound").operatorName("breaksound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? ObfuscationHelpers.getSoundEventName(sound.get().getBreakSound()).toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The placesound of the block
     */
    public static final IOperator OBJECT_BLOCK_PLACESOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("place_sound").operatorName("placesound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? ObfuscationHelpers.getSoundEventName(sound.get().getPlaceSound()).toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The stepsound of the block
     */
    public static final IOperator OBJECT_BLOCK_STEPSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("step_sound").operatorName("stepsound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? ObfuscationHelpers.getSoundEventName(sound.get().getStepSound()).toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());

    /**
     * If the block is shearable
     */
    public static final IOperator OBJECT_BLOCK_ISSHEARABLE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_shearable").operatorName("isshearable")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get().getBlock() instanceof IShearable
                        && ((IShearable) a.getRawValue().get().getBlock()).isShearable(null, null, null));
            }).build());

    /**
     * If the block is plantable
     */
    public static final IOperator OBJECT_BLOCK_ISPLANTABLE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_plantable").operatorName("isplantable")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get().getBlock() instanceof IPlantable);
            }).build());

    /**
     * The block plant type
     */
    public static final IOperator OBJECT_BLOCK_PLANTTYPE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("plant_type").operatorName("planttype")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                String type = "None";
                if (a.getRawValue().isPresent() && a.getRawValue().get().getBlock() instanceof IPlantable) {
                    type = ((IPlantable) a.getRawValue().get().getBlock()).getPlantType(null, null).name();
                }
                return ValueTypeString.ValueString.of(type);
            }).build());

    /**
     * The block when this block is planted
     */
    public static final IOperator OBJECT_BLOCK_PLANT = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("plant")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                IBlockState plant = null;
                if (a.getRawValue().isPresent() && a.getRawValue().get().getBlock() instanceof IPlantable) {
                    plant = ((IPlantable) a.getRawValue().get().getBlock()).getPlant(null, null);
                }
                return ValueObjectTypeBlock.ValueBlock.of(plant);
            }).build());

    /**
     * The block when this block is planted
     */
    public static final IOperator OBJECT_BLOCK_PLANTAGE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("plant_age").operatorName("plantage")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                int age = 0;
                if (a.getRawValue().isPresent()) {
                    for (IProperty<?> prop : a.getRawValue().get().getProperties().keySet()) {
                        if (prop.getName().equals("age") && prop.getValueClass() == Integer.class) {
                            age = (Integer) a.getRawValue().get().getValue(prop);
                        }
                    }
                }
                return ValueTypeInteger.ValueInteger.of(age);
            }).build());

    /**
     * Get a block by name.
     */
    public static final IOperator OBJECT_BLOCK_BY_NAME = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .inputType(ValueTypes.STRING).output(ValueTypes.OBJECT_BLOCK)
            .symbol("block_by_name").operatorName("blockbyname")
            .function(OperatorBuilders.FUNCTION_STRING_TO_RESOURCE_LOCATION
                    .build(input -> {
                        Block block = Block.REGISTRY.getObject(input.getLeft());
                        IBlockState blockState = null;
                        if (block != null) {
                            blockState = block.getStateFromMeta(input.getRight());
                        }
                        return ValueObjectTypeBlock.ValueBlock.of(blockState);
                    })).build());

    /**
     * ----------------------------------- ITEM STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Item Stack size operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_SIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("size")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getCount() : 0
            )).build());

    /**
     * Item Stack maxsize operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("maxsize")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getMaxStackSize() : 0
            )).build());

    /**
     * Item Stack isstackable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISSTACKABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("stackable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isStackable()
            )).build());

    /**
     * Item Stack isdamageable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISDAMAGEABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("damageable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isItemStackDamageable()
            )).build());

    /**
     * Item Stack damage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_DAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("damage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getItemDamage() : 0
            )).build());

    /**
     * Item Stack maxdamage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXDAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("max_damage").operatorName("maxdamage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getMaxDamage() : 0
            )).build());

    /**
     * Item Stack isenchanted operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTED = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchanted")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isItemEnchanted()
            )).build());

    /**
     * Item Stack isenchantable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchantable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isItemEnchantable()
            )).build());

    /**
     * Item Stack repair cost with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_REPAIRCOST = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("repair_cost").operatorName("repaircost")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getRepairCost() : 0
            )).build());

    /**
     * Get the rarity of an itemstack.
     */
    public static final IOperator OBJECT_ITEMSTACK_RARITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("rarity")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeString.ValueString.of(!a.getRawValue().isEmpty() ? a.getRawValue().getRarity().rarityName : "");
            }).build());

    /**
     * Get the strength of an itemstack against a block as a double.
     */
    public static final IOperator OBJECT_ITEMSTACK_STRENGTH_VS_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}).output(ValueTypes.DOUBLE)
            .symbolOperator("strength")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeBlock.ValueBlock b = variables.getValue(1, ValueTypes.OBJECT_BLOCK);
                return ValueTypeDouble.ValueDouble.of(!a.getRawValue().isEmpty() && b.getRawValue().isPresent() ? a.getRawValue().getDestroySpeed(b.getRawValue().get()) : 0);
            }).build());

    /**
     * If the given itemstack can be used to harvest the given block.
     */
    public static final IOperator OBJECT_ITEMSTACK_CAN_HARVEST_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_2_LONG
            .inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}).output(ValueTypes.BOOLEAN)
            .symbol("can_harvest").operatorName("canharvest")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeBlock.ValueBlock b = variables.getValue(1, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty() && b.getRawValue().isPresent() && a.getRawValue().canHarvestBlock(b.getRawValue().get()));
            }).build());

    /**
     * The block from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("block")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueObjectTypeBlock.ValueBlock.of((!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof ItemBlock) ? BlockHelpers.getBlockStateFromItemStack(a.getRawValue()) : null);
            }).build());

    /**
     * If the given stack has a fluid.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISFLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("is_fluidstack").operatorName("isfluidstack")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && Helpers.getFluidStack(itemStack) != null
            )).build());

    /**
     * The fluidstack from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_FLUIDSTACK).symbolOperator("fluidstack")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueObjectTypeFluidStack.ValueFluidStack.of(!a.getRawValue().isEmpty() ? Helpers.getFluidStack(a.getRawValue()) : null);
            }).build());

    /**
     * The capacity of the fluidstack from the stack.
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACKCAPACITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("fluidstack_capacity").operatorName("fluidstackcapacity")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? Helpers.getFluidStackCapacity(itemStack) : 0
            )).build());

    /**
     * If the NBT tags of the given stacks are equal.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISNBTEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NBT=").operatorName("isnbtequal")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack valueStack0 = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeItemStack.ValueItemStack valueStack1 = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                ItemStack a = valueStack0.getRawValue();
                ItemStack b = valueStack1.getRawValue();
                boolean equal = false;
                if(!a.isEmpty() && !b.isEmpty()) {
                    equal = a.isItemEqual(b) && ItemMatch.areItemStacksEqual(a, b, ItemMatch.NBT);
                } else if(a.isEmpty() && b.isEmpty()) {
                    equal = true;
                }
                return ValueTypeBoolean.ValueBoolean.of(equal);
            }).build());

    /**
     * If the raw items of the given stacks are equal, ignoring NBT but including damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISITEMEQUALNONBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NoNBT=").operatorName("isitemequalnonbt")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack valueStack0 = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeItemStack.ValueItemStack valueStack1 = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                ItemStack a = valueStack0.getRawValue();
                ItemStack b = valueStack1.getRawValue();
                boolean equal = false;
                if(!a.isEmpty() && !b.isEmpty()) {
                    equal = ItemMatch.areItemStacksEqual(a, b, ItemMatch.ITEM | ItemMatch.DAMAGE);
                } else if(a.isEmpty() && b.isEmpty()) {
                    equal = true;
                }
                return ValueTypeBoolean.ValueBoolean.of(equal);
            }).build());

    /**
     * If the raw items of the given stacks are equal, ignoring NBT and damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISRAWITEMEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwitemequal")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack valueStack0 = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeItemStack.ValueItemStack valueStack1 = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                ItemStack a = valueStack0.getRawValue();
                ItemStack b = valueStack1.getRawValue();
                boolean equal = false;
                if(!a.isEmpty() && !b.isEmpty()) {
                    equal = ItemMatch.areItemStacksEqual(a, b, ItemMatch.ITEM);
                } else if(a.isEmpty() && b.isEmpty()) {
                    equal = true;
                }
                return ValueTypeBoolean.ValueBoolean.of(equal);
            }).build());

    /**
     * The name of the mod owning this item
     */
    public static final IOperator OBJECT_ITEMSTACK_MODNAME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                        return !a.getRawValue().isEmpty() ? Item.REGISTRY.getNameForObject(a.getRawValue().getItem()) : null;
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The fuel burn time of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_FUELBURNTIME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("burn_time").operatorName("burntime")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? TileEntityFurnace.getItemBurnTime(itemStack) : 0
            )).build());

    /**
     * If the given item can be used as fuel
     */
    public static final IOperator OBJECT_ITEMSTACK_CANBURN = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("can_burn").operatorName("canburn")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> itemStack != null && TileEntityFurnace.getItemBurnTime(itemStack) > 0
            )).build());

    /**
     * If the given item can be smelted
     */
    public static final IOperator OBJECT_ITEMSTACK_CANSMELT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("can_smelt").operatorName("cansmelt")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> itemStack != null && !FurnaceRecipes.instance().getSmeltingResult(itemStack).isEmpty()
            )).build());

    /**
     * The oredict entries of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST)
            .symbol("oredict_names").operatorName("oredict")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ImmutableList.Builder<ValueTypeString.ValueString> builder = ImmutableList.builder();
                if(!a.getRawValue().isEmpty()) {
                    for (int i : OreDictionary.getOreIDs(a.getRawValue())) {
                        builder.add(ValueTypeString.ValueString.of(OreDictionary.getOreName(i)));
                    }
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.STRING, builder.build());
            }).build());

    /**
     * Get a list of items that correspond to the given oredict key.
     */
    public static final IOperator OBJECT_ITEMSTACK_OREDICT_STACKS = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX
            .output(ValueTypes.LIST)
            .symbol("oredict_values").operatorName("oredict")
            .inputType(ValueTypes.STRING).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                ImmutableList.Builder<ValueObjectTypeItemStack.ValueItemStack> builder = ImmutableList.builder();
                if (!StringUtils.isNullOrEmpty(a.getRawValue())) {
                    Helpers.getOresWildcard(a.getRawValue())
                            .map(ValueObjectTypeItemStack.ValueItemStack::of)
                            .forEach(builder::add);
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, builder.build());
            }).build());

    /**
     * ItemStack operator that applies the given stacksize to the given itemstack and creates a new ItemStack.
     */
    public static final IOperator OBJECT_ITEMSTACK_WITHSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_INTEGER_1
            .output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("with_size").operatorName("withsize")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (!a.getRawValue().isEmpty()) {
                    ItemStack itemStack = a.getRawValue().copy();
                    itemStack.setCount(b.getRawValue());
                    return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                }
                return a;
            }).build());

    /**
     * Check if the item is an RF container item
     */
    public static final IOperator OBJECT_ITEMSTACK_ISFECONTAINER = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("is_fe_container").operatorName("isfecontainer")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_BOOLEAN.build(
                Objects::nonNull
            )).build());

    /**
     * Get the storage energy
     */
    public static final IOperator OBJECT_ITEMSTACK_STOREDFE = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("stored_fe").operatorName("storedfe")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(
                input -> input != null ? input.getEnergyStored() : 0
            )).build());

    /**
     * Get the energy capacity
     */
    public static final IOperator OBJECT_ITEMSTACK_FECAPACITY = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("capacity_fe").operatorName("fecapacity")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(
                input -> input != null ? input.getMaxEnergyStored() : 0
            )).build());


    /**
     * If the given item has an inventory.
     */
    public static final IOperator OBJECT_ITEMSTACK_HASINVENTORY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("has_inventory").operatorName("hasinventory")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty() && a.getRawValue().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
            }).build());

    /**
     * If the item is plantable
     */
    public static final IOperator OBJECT_ITEMSTACK_ISPLANTABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("is_plantable").operatorName("isplantable")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty()
                        && a.getRawValue().getItem() instanceof IPlantable);
            }).build());




    /**
     * Retrieve the inventory size of the given item handler contents.
     */
    public static final IOperator OBJECT_ITEMSTACK_INVENTORYSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("inventory_size").operatorName("inventorysize")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                int size = 0;
                if (!a.getRawValue().isEmpty()
                        && a.getRawValue().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    IItemHandler itemHandler = a.getRawValue().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    size = itemHandler.getSlots();
                }
                return ValueTypeInteger.ValueInteger.of(size);
            }).build());

    /**
     * The item plant type
     */
    public static final IOperator OBJECT_ITEMSTACK_PLANTTYPE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING)
            .symbol("plant_type").operatorName("planttype")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                String type = "None";
                if (!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof IPlantable) {
                    type = ((IPlantable) a.getRawValue().getItem()).getPlantType(null, null).name();
                }
                return ValueTypeString.ValueString.of(type);
            }).build());

    /**
     * Retrieve the inventory of the given item handler contents.
     */
    public static final IOperator OBJECT_ITEMSTACK_INVENTORY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("inventory")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                if (!a.getRawValue().isEmpty()
                        && a.getRawValue().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    IItemHandler itemHandler = a.getRawValue().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    List<ValueObjectTypeItemStack.ValueItemStack> values = Lists.newArrayListWithCapacity(itemHandler.getSlots());
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        values.add(ValueObjectTypeItemStack.ValueItemStack.of(itemHandler.getStackInSlot(i)));
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, values);
                }
                return ValueTypes.LIST.getDefault();
            }).build());

    /**
     * The item when this item is planted
     */
    public static final IOperator OBJECT_ITEMSTACK_PLANT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("plant")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                IBlockState plant = null;
                if (!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof IPlantable) {
                    plant = ((IPlantable) a.getRawValue().getItem()).getPlant(null, null);
                }
                return ValueObjectTypeBlock.ValueBlock.of(plant);
            }).build());

    /**
     * Get an item by name.
     */
    public static final IOperator OBJECT_ITEMSTACK_BY_NAME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_PREFIX_LONG
            .inputType(ValueTypes.STRING).output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("item_by_name").operatorName("itembyname")
            .function(OperatorBuilders.FUNCTION_STRING_TO_RESOURCE_LOCATION
                    .build(input -> {
                        Item item = Item.REGISTRY.getObject(input.getLeft());
                        ItemStack itemStack = ItemStack.EMPTY;
                        if (item != null) {
                            itemStack = new ItemStack(item, 1, input.getRight());
                        }
                        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                    })).build());

    /**
     * Get the total item count of the given item in a list.
     */
    public static final IOperator OBJECT_ITEMSTACK_LIST_COUNT= REGISTRY.register(OperatorBuilders.ITEMSTACK_2_LONG
            .inputTypes(ValueTypes.LIST, ValueTypes.OBJECT_ITEMSTACK)
            .output(ValueTypes.INTEGER)
            .symbol("item_list_count").operatorName("itemlistcount")
            .function(variables -> {
                ValueTypeList.ValueList a = variables.getValue(0, ValueTypes.LIST);
                ValueObjectTypeItemStack.ValueItemStack b = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                if (!ValueHelpers.correspondsTo(a.getRawValue().getValueType(), ValueTypes.OBJECT_ITEMSTACK)) {
                    L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            a.getRawValue().getValueType(), ValueTypes.OBJECT_ITEMSTACK);
                    throw new EvaluationException(error.localize());
                }

                ItemStack itemStack = b.getRawValue();
                int count = 0;
                for (ValueObjectTypeItemStack.ValueItemStack listValue :
                        (IValueTypeListProxy<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack>) a.getRawValue()) {
                    if (!listValue.getRawValue().isEmpty()) {
                        ItemStack listItem = listValue.getRawValue();
                        if (!itemStack.isEmpty()) {
                            if (itemStack.isItemEqual(listItem) && ItemStack.areItemStackTagsEqual(itemStack, listItem)) {
                                count += listItem.getCount();
                            }

                        } else {
                            count += listItem.getCount();
                        }
                    }
                }

                return ValueTypeInteger.ValueInteger.of(count);
            }).build());

    /**
     * Item Stack size operator with one input itemstack and one output NBT tag.
     */
    public static final IOperator OBJECT_ITEMSTACK_NBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorName("nbt")
            .function(input -> {
                ValueObjectTypeItemStack.ValueItemStack itemStack = input.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeNbt.ValueNbt.of(itemStack.getRawValue().getTagCompound());
            }).build());

    /**
     * Item Stack has_nbt operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_HASNBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_PREFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("has_nbt").operatorName("hasnbt")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                    itemStack -> !itemStack.isEmpty() && itemStack.hasTagCompound()
            )).build());

    /**
     * ----------------------------------- ENTITY OBJECT OPERATORS -----------------------------------
     */

    /**
     * If the entity is a mob
     */
    public static final IOperator OBJECT_ENTITY_ISMOB = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_mob").operatorName("ismob")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof IMob
            )).build());

    /**
     * If the entity is an animal
     */
    public static final IOperator OBJECT_ENTITY_ISANIMAL = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_animal").operatorName("isanimal")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof IAnimals && !(entity instanceof IMob)
            )).build());

    /**
     * If the entity is an item
     */
    public static final IOperator OBJECT_ENTITY_ISITEM = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_item").operatorName("isitem")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof EntityItem
            )).build());

    /**
     * If the entity is a player
     */
    public static final IOperator OBJECT_ENTITY_ISPLAYER = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_player").operatorName("isplayer")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof EntityPlayer
            )).build());

    /**
     * If the entity is a minecart
     */
    public static final IOperator OBJECT_ENTITY_ISMINECART = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_minecart").operatorName("isminecart")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof EntityMinecart
            )).build());

    /**
     * The itemstack from the entity
     */
    public static final IOperator OBJECT_ENTITY_ITEMSTACK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX
            .output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("item")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                return ValueObjectTypeItemStack.ValueItemStack.of((a.isPresent() && a.get() instanceof EntityItem) ? ((EntityItem) a.get()).getItem() : ItemStack.EMPTY);
            }).build());

    /**
     * The entity health
     */
    public static final IOperator OBJECT_ENTITY_HEALTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("health")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHealth() : 0.0
            )).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_WIDTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("width")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity != null ? entity.width : 0.0
            )).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_HEIGHT = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperator("height")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity != null ? entity.height : 0.0
            )).build());

    /**
     * If the entity is burning
     */
    public static final IOperator OBJECT_ENTITY_ISBURNING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_burning").operatorName("isburning")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isBurning()
            )).build());

    /**
     * If the entity is wet
     */
    public static final IOperator OBJECT_ENTITY_ISWET = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_wet").operatorName("iswet")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isWet()
            )).build());

    /**
     * If the entity is sneaking
     */
    public static final IOperator OBJECT_ENTITY_ISSNEAKING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_sneaking").operatorName("issneaking")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isSneaking()
            )).build());

    /**
     * If the entity is eating
     */
    public static final IOperator OBJECT_ENTITY_ISEATING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_eating").operatorName("iseating")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getItemInUseCount() > 0
            )).build());

    /**
     * The list of armor itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_ARMORINVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("armor_inventory").operatorName("armorinventory")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityArmorInventory(entity.world, entity));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                }
            }).build());

    /**
     * The list of itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_INVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("inventory")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityInventory(entity.world, entity));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                }
            }).build());

    /**
     * The name of the mod owning this entity
     */
    public static final IOperator OBJECT_ENTITY_MODNAME = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                String modName = "";
                if(a.getRawValue().isPresent()) {
                    Entity entity = a.getRawValue().get();
                    EntityRegistry.EntityRegistration entityRegistration = EntityRegistry.instance().lookupModSpawn(entity.getClass(), true);
                    modName = entityRegistration != null ? entityRegistration.getContainer().getName() : "Minecraft";
                }
                return ValueTypeString.ValueString.of(modName);
            }).build());

    /**
     * The block the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETBLOCK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_BLOCK)
            .symbol("target_block").operatorName("targetblock")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
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
                    Vec3d direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

                    RayTraceResult mop = entity.world.rayTraceBlocks(origin, direction, true);
                    if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                        blockState = entity.world.getBlockState(mop.getBlockPos());
                    }
                }
                return ValueObjectTypeBlock.ValueBlock.of(blockState);
            }).build());

    /**
     * The entity the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETENTITY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ENTITY)
            .symbol("target_entity").operatorName("targetentity")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
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
                    Vec3d direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

                    float size = entity.getCollisionBorderSize();
                    List<Entity> list = entity.world.getEntitiesWithinAABBExcludingEntity(entity,
                            entity.getEntityBoundingBox().expand(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance)
                                    .grow((double) size, (double) size, (double) size));
                    for (Entity e : list) {
                        if (e.canBeCollidedWith()) {
                            float f10 = e.getCollisionBorderSize();
                            AxisAlignedBB axisalignedbb = e.getEntityBoundingBox().expand((double) f10, (double) f10, (double) f10);
                            RayTraceResult mop = axisalignedbb.calculateIntercept(origin, direction);

                            if (axisalignedbb.contains(origin)) {
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
            }).build());

    /**
     * If the given player has an external gui open.
     */
    public static final IOperator OBJECT_PLAYER_HASGUIOPEN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("has_gui_open").operatorName("hasguiopen")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityPlayer) {
                    EntityPlayer entity = (EntityPlayer) a.getRawValue().get();
                    return ValueTypeBoolean.ValueBoolean.of(entity.openContainer != entity.inventoryContainer);
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
            }).build());

    /**
     * The item the given entity is currently holding in its main hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_MAIN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("held_item_1").operatorName("helditem")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    itemStack = ((EntityLivingBase) a.getRawValue().get()).getHeldItemMainhand();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The item the given entity is currently holding in its off hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_OFF = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("held_item_2").operatorName("helditemoffhand")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    itemStack = ((EntityLivingBase) a.getRawValue().get()).getHeldItemOffhand();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The entity's mounted entity
     */
    public static final IOperator OBJECT_ENTITY_MOUNTED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.LIST).symbolOperator("mounted")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                List<ValueObjectTypeEntity.ValueEntity> passengers = Lists.newArrayList();
                if(a.getRawValue().isPresent()) {
                    for (Entity passenger : a.getRawValue().get().getPassengers()) {
                        passengers.add(ValueObjectTypeEntity.ValueEntity.of(passenger));
                    }
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, passengers);
            }).build());

    /**
     * The item frame's contents
     */
    public static final IOperator OBJECT_ITEMFRAME_CONTENTS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("itemframe_contents").operatorName("itemframecontents")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityItemFrame) {
                    itemStack = ((EntityItemFrame) a.getRawValue().get()).getDisplayedItem();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The item frame's rotation
     */
    public static final IOperator OBJECT_ITEMFRAME_ROTATION = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER)
            .symbol("itemframe_rotation").operatorName("itemframerotation")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Integer rotation = 0;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityItemFrame) {
                    rotation = ((EntityItemFrame) a.getRawValue().get()).getRotation();
                }
                return ValueTypeInteger.ValueInteger.of(rotation);
            }).build());

    /**
     * The hurtsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_HURTSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("hurtsound")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                String hurtSound = "";
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    String sound = ObfuscationHelpers.getSoundEventName(ObfuscationHelpers.getEntityLivingBaseHurtSound((EntityLivingBase) a.getRawValue().get(), DamageSource.GENERIC)).toString();
                    if (sound != null) {
                        hurtSound = sound;
                    }
                }
                return ValueTypeString.ValueString.of(hurtSound);
            }).build());

    /**
     * The deathsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_DEATHSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("deathsound")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                String hurtSound = "";
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    String sound = ObfuscationHelpers.getSoundEventName(ObfuscationHelpers.getEntityLivingBaseDeathSound((EntityLivingBase) a.getRawValue().get())).toString();
                    if (sound != null) {
                        hurtSound = sound;
                    }
                }
                return ValueTypeString.ValueString.of(hurtSound);
            }).build());

    /**
     * The age of this entity.
     */
    public static final IOperator OBJECT_ENTITY_AGE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER).symbolOperator("age")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                int age = 0;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    age = ((EntityLivingBase) a.getRawValue().get()).getIdleTime();
                }
                return ValueTypeInteger.ValueInteger.of(age);
            }).build());

    /**
     * If the entity is a child.
     */
    public static final IOperator OBJECT_ENTITY_ISCHILD = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_child").operatorName("ischild")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean child = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityLivingBase) {
                    child = ((EntityLivingBase) a.getRawValue().get()).isChild();
                }
                return ValueTypeBoolean.ValueBoolean.of(child);
            }).build());

    /**
     * If the entity can be bred.
     */
    public static final IOperator OBJECT_ENTITY_CANBREED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("canbreed").operatorName("canbreed")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean canBreed = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityAgeable) {
                    canBreed = ((EntityAgeable) a.getRawValue().get()).getGrowingAge() == 0;
                }
                return ValueTypeBoolean.ValueBoolean.of(canBreed);
            }).build());

    /**
     * If the entity is in love.
     */
    public static final IOperator OBJECT_ENTITY_ISINLOVE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_in_love").operatorName("isinlove")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean inLove = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof EntityAnimal) {
                    inLove = ((EntityAnimal) a.getRawValue().get()).isInLove();
                }
                return ValueTypeBoolean.ValueBoolean.of(inLove);
            }).build());

    /**
     * If the entity can be bred with the given item.
     */
    public static final IOperator OBJECT_ENTITY_CANBREEDWITH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .inputTypes(ValueTypes.OBJECT_ENTITY, ValueTypes.OBJECT_ITEMSTACK)
            .output(ValueTypes.BOOLEAN)
            .symbol("can_breed_with").operatorName("canbreedwith")
            .renderPattern(IConfigRenderPattern.INFIX_LONG)
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ValueObjectTypeItemStack.ValueItemStack b = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                boolean canBreedWith = false;
                if (a.getRawValue().isPresent() && !b.getRawValue().isEmpty() && a.getRawValue().get() instanceof EntityAnimal) {
                    canBreedWith = ((EntityAnimal) a.getRawValue().get()).isBreedingItem(b.getRawValue());
                }
                return ValueTypeBoolean.ValueBoolean.of(canBreedWith);
            }).build());

    /**
     * If the entity is shearable.
     */
    public static final IOperator OBJECT_ENTITY_ISSHEARABLE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_shearable").operatorName("isshearable")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get() instanceof IShearable
                        && ((IShearable) a.getRawValue().get()).isShearable(null, null, null));
            }).build());

    /**
     * The entity serialized to NBT.
     */
    public static final IOperator OBJECT_ENTITY_NBT = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorName("nbt")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity entity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                try {
                    if (entity.getRawValue().isPresent()) {
                        return ValueTypeNbt.ValueNbt.of(entity.getRawValue().get().writeToNBT(new NBTTagCompound()));
                    }
                } catch (Exception e) {
                    // Catch possible errors during NBT writing
                }
                return ValueTypes.NBT.getDefault();
            }).build());

    /**
     * The entity type.
     */
    public static final IOperator OBJECT_ENTITY_TYPE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbol("entity_type").operatorName("entitytype")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity entity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                String entityType = "";
                if (entity.getRawValue().isPresent()) {
                    Entity e = entity.getRawValue().get();
                    entityType = EntityList.getEntityString(e);
                    if (entityType == null) {
                        if (e instanceof EntityPlayer) {
                            entityType = "Player";
                        } else {
                            entityType = e.getClass().getCanonicalName();
                        }
                    }
                }
                return ValueTypeString.ValueString.of(entityType);
            }).build());

    /**
     * The entity items.
     */
    public static final IOperator OBJECT_ENTITY_ITEMS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("entity_items").operatorName("entityitems")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityItems(entity.world, entity, null));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.emptyList());
                }
            }).build());

    /**
     * The entity fluids.
     */
    public static final IOperator OBJECT_ENTITY_FLUIDS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("entity_fluids").operatorName("entityfluids")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityFluids(entity.world, entity, null));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_FLUIDSTACK, Collections.emptyList());
                }
            }).build());

    /**
     * The entity energy stored.
     */
    public static final IOperator OBJECT_ENTITY_ENERGY_STORED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("entity_stored_fe").operatorName("entityenergystored")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    if (entity.hasCapability(CapabilityEnergy.ENERGY, null)) {
                        return ValueTypeInteger.ValueInteger.of(entity.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored());
                    }
                }
                return ValueTypeInteger.ValueInteger.of(0);
            }).build());

    /**
     * The entity energy stored.
     */
    public static final IOperator OBJECT_ENTITY_ENERGY_CAPACITY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("entity_capacity_fe").operatorName("entityenergycapacity")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    if (entity.hasCapability(CapabilityEnergy.ENERGY, null)) {
                        return ValueTypeInteger.ValueInteger.of(entity.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored());
                    }
                }
                return ValueTypeInteger.ValueInteger.of(0);
            }).build());

    /**
     * ----------------------------------- FLUID STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * The amount of fluid in the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_AMOUNT = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("amount")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack != null ? fluidStack.amount : 0
            )).build());

    /**
     * The block from the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_BLOCK = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("block")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                Optional<FluidStack> a = valueFluidStack.getRawValue();
                return ValueObjectTypeBlock.ValueBlock.of(a.isPresent() ? a.get().getFluid().getBlock().getDefaultState() : null);
            }).build());

    /**
     * The fluidstack luminosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_LUMINOSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("luminosity")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack != null ? fluidStack.getFluid().getLuminosity(fluidStack) : 0
            )).build());

    /**
     * The fluidstack density
     */
    public static final IOperator OBJECT_FLUIDSTACK_DENSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("density")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack != null ? fluidStack.getFluid().getDensity(fluidStack) : 0
            )).build());

    /**
     * The fluidstack viscosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_VISCOSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("viscosity")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack != null ? fluidStack.getFluid().getViscosity(fluidStack) : 0
            )).build());

    /**
     * If the fluidstack is gaseous
     */
    public static final IOperator OBJECT_FLUIDSTACK_ISGASEOUS = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_gaseous").operatorName("isgaseous")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_BOOLEAN.build(
                fluidStack -> fluidStack != null && fluidStack.getFluid().isGaseous(fluidStack)
            )).build());

    /**
     * The rarity of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_RARITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("rarity")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                Optional<FluidStack> a = valueFluidStack.getRawValue();
                return ValueTypeString.ValueString.of(a.isPresent() ? a.get().getFluid().getRarity(a.get()).rarityName : "");
            }).build());

    /**
     * If the fluid types of the two given fluidstacks are equal
     */
    public static final IOperator OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL = REGISTRY.register(OperatorBuilders.FLUIDSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwfluidequal")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack0 = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack1 = variables.getValue(1, ValueTypes.OBJECT_FLUIDSTACK);
                Optional<FluidStack> a = valueFluidStack0.getRawValue();
                Optional<FluidStack> b = valueFluidStack1.getRawValue();
                boolean equal = false;
                if(a.isPresent() && b.isPresent()) {
                    equal = a.get().isFluidEqual(b.get());
                } else if(!a.isPresent() && !b.isPresent()) {
                    equal = true;
                }
                return ValueTypeBoolean.ValueBoolean.of(equal);
            }).build());

    /**
     * The name of the mod owning this fluid
     */
    public static final IOperator OBJECT_FLUIDSTACK_MODNAME = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperator("mod")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack a = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                String modName = "";
                if (a.getRawValue().isPresent()) {
                    Fluid fluid = a.getRawValue().get().getFluid();
                    String modDomain = null;
                    if (fluid.getStill() != null) {
                        modDomain = fluid.getStill().getNamespace();
                    } else if (fluid.getFlowing() != null) {
                        modDomain = fluid.getFlowing().getNamespace();
                    } else if (fluid.getBlock() != null) {
                        modDomain = Block.REGISTRY.getNameForObject(fluid.getBlock()).getNamespace();
                    }
                    String modId = org.cyclops.cyclopscore.helper.Helpers.getModId(modDomain);
                    modName = Loader.instance().getIndexedModList().get(modId).getName();
                }
                return ValueTypeString.ValueString.of(modName);
            }).build());

    /**
     * The tag of the given fluidstack.
     */
    public static final IOperator OBJECT_FLUIDSTACK_NBT = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorName("nbt")
            .function(input -> {
                ValueObjectTypeFluidStack.ValueFluidStack fluidStack = input.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                if (fluidStack.getRawValue().isPresent()) {
                    return ValueTypeNbt.ValueNbt.of(fluidStack.getRawValue().get().tag);
                }
                return ValueTypes.NBT.getDefault();
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
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(ValueTypes.CATEGORY_ANY))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR.build(
                    input -> {
                        IOperator innerOperator = input.getLeft();
                        OperatorBase.SafeVariablesGetter variables = input.getRight();
                        IVariable variable = variables.getVariables()[0];
                        return ValueHelpers.evaluateOperator(innerOperator, variable);
                    })).build());
    static {
        REGISTRY.registerSerializer(new CurriedOperator.Serializer());
    }

    /**
     * Apply for a given operator the given 2 values.
     */
    public static final IOperator OPERATOR_APPLY_2 = REGISTRY.register(OperatorBuilders.OPERATOR
            .renderPattern(IConfigRenderPattern.INFIX_2)
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER)
            .inputTypes(ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY)
            .output(ValueTypes.CATEGORY_ANY).symbolOperator("apply2")
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR.build(
                    input -> {
                        IOperator innerOperator = input.getLeft();
                        OperatorBase.SafeVariablesGetter variables = input.getRight();
                        IVariable variable0 = variables.getVariables()[0];
                        IVariable variable1 = variables.getVariables()[1];
                        return ValueHelpers.evaluateOperator(innerOperator, variable0, variable1);
                    })).build());

    /**
     * Apply for a given operator the given 3 values.
     */
    public static final IOperator OPERATOR_APPLY_3 = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .renderPattern(IConfigRenderPattern.INFIX_3)
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER)
            .inputTypes(ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY)
            .output(ValueTypes.CATEGORY_ANY).symbolOperator("apply3")
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR.build(
                    input -> {
                        IOperator innerOperator = input.getLeft();
                        OperatorBase.SafeVariablesGetter variables = input.getRight();
                        IVariable variable0 = variables.getVariables()[0];
                        IVariable variable1 = variables.getVariables()[1];
                        IVariable variable2 = variables.getVariables()[2];
                        return ValueHelpers.evaluateOperator(innerOperator, variable0, variable1, variable2);
                    })).build());

    /**
     * Apply the given operator on all elements of a list, resulting in a new list of mapped values.
     */
    public static final IOperator OPERATOR_MAP = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST})
            .output(ValueTypes.LIST).symbolOperator("map")
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR_LIST.build(
                    input -> {
                        final IOperator innerOperator = input.getLeft();
                        OperatorBase.SafeVariablesGetter variables = input.getRight();
                        ValueTypeList.ValueList inputList = variables.getValue(0, ValueTypes.LIST);
                        return ValueTypeList.ValueList.ofFactory(
                                new ValueTypeListProxyOperatorMapped(innerOperator, inputList.getRawValue()));
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
                            ValueTypeList.ValueList<?, ?> inputList = variables.getValue(0, ValueTypes.LIST);
                            List<IValue> filtered = Lists.newArrayList();
                            for (IValue value : inputList.getRawValue()) {
                                IValue result = ValueHelpers.evaluateOperator(innerOperator, value);
                                ValueHelpers.validatePredicateOutput(innerOperator, result);
                                if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
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
            .function(OperatorBuilders.FUNCTION_TWO_PREDICATES.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Conjunction.asOperator(input.getLeft(), input.getRight()))
            )).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Conjunction.Serializer());
    }

    /**
     * Takes the disjunction of two predicates.
     */
    public static final IOperator OPERATOR_DISJUNCTION = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol(".||.").operatorName("disjunction")
            .function(OperatorBuilders.FUNCTION_TWO_PREDICATES.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Disjunction.asOperator(input.getLeft(), input.getRight()))
            )).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Disjunction.Serializer());
    }

    /**
     * Takes the negation of a predicate.
     */
    public static final IOperator OPERATOR_NEGATION = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .renderPattern(IConfigRenderPattern.PREFIX_1)
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol("!.").operatorName("negation")
            .function(OperatorBuilders.FUNCTION_ONE_PREDICATE.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Negation.asOperator(input))
            )).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Negation.Serializer());
    }

    /**
     * Create a new operator that pipes the output from the first operator to the second operator.
     */
    public static final IOperator OPERATOR_PIPE = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbol(".").operatorName("pipe")
            .function(OperatorBuilders.FUNCTION_TWO_OPERATORS.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Pipe.asOperator(input.getLeft(), input.getRight()))
            )).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Pipe.Serializer());
    }

    /**
     * Create a new operator that gives its input to the first and second operators, and pipes the outputs from both of them to the third operator.
     */
    public static final IOperator OPERATOR_PIPE2 = REGISTRY.register(OperatorBuilders.OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX_2_LATE)
            .output(ValueTypes.OPERATOR).symbol(".2").operatorName("pipe2")
            .function(OperatorBuilders.FUNCTION_THREE_OPERATORS.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Pipe2.asOperator(input.getLeft(), input.getMiddle(), input.getRight()))
            )).build());
    static {
        REGISTRY.registerSerializer(new CombinedOperator.Pipe2.Serializer());
    }

    /**
     * Flip the input parameters of an operator with two inputs.
     */
    public static final IOperator OPERATOR_FLIP = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .renderPattern(IConfigRenderPattern.PREFIX_1)
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR})
            .output(ValueTypes.OPERATOR).symbolOperator("flip")
            .function(OperatorBuilders.FUNCTION_ONE_OPERATOR.build(
                input -> ValueTypeOperator.ValueOperator.of(CombinedOperator.Flip.asOperator(input))
            )).build());
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
            .conditionalOutputTypeDeriver((operator, input) -> input[2].getType())
            .function(variables -> {
                IValue accumulator = variables.getValue(2);
                final IOperator innerOperator = OperatorBuilders.getSafeOperator(
                        variables.getValue(0, ValueTypes.OPERATOR), accumulator.getType());
                ValueTypeList.ValueList<IValueType<IValue>, IValue> inputList = variables.getValue(1, ValueTypes.LIST);
                for (IValue listValue : inputList.getRawValue()) {
                    accumulator = ValueHelpers.evaluateOperator(innerOperator, accumulator, listValue);
                }
                return accumulator;
            }).build());

    /**
     * Apply the given operator on all elements of a list to reduce the list to one value.
     */
    public static final IOperator OPERATOR_REDUCE1 = REGISTRY.register(OperatorBuilders.OPERATOR
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST})
            .renderPattern(IConfigRenderPattern.PREFIX_2_LONG)
            .output(ValueTypes.CATEGORY_ANY).symbolOperator("reduce1")
            .conditionalOutputTypeDeriver((operator, input) -> {
                try {
                    IValueTypeListProxy a = ((ValueTypeList.ValueList) input[1].getValue()).getRawValue();
                    return a.getValueType();
                } catch (EvaluationException e) {
                    return operator.getOutputType();
                }
            })
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(1, ValueTypes.LIST);
                Iterator<IValue> iter = valueList.getRawValue().iterator();
                if (!iter.hasNext()) {
                    throw new EvaluationException("The reduce1 operator tried to get the head of an empty list. " +
                            "Use the reduce operator instead to provide a base value to support empty lists.");
                }

                IValue accumulator = iter.next();
                final IOperator innerOperator = OperatorBuilders.getSafeOperator(
                        variables.getValue(0, ValueTypes.OPERATOR), accumulator.getType());

                while (iter.hasNext()) {
                    IValue listValue = iter.next();
                    accumulator = ValueHelpers.evaluateOperator(innerOperator, accumulator, listValue);
                }
                return accumulator;
            }).build());

    /**
     * Apply for a given operator a given value.
     */
    public static final IOperator OPERATOR_BY_NAME = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .inputType(ValueTypes.STRING).output(ValueTypes.OPERATOR)
            .symbol("op_by_name").operatorName("byName")
            .function(input -> {
                ValueTypeString.ValueString name = input.getValue(0, ValueTypes.STRING);
                IOperator operator = Operators.REGISTRY.getOperator(name.getRawValue());
                if (operator == null) {
                    throw new EvaluationException("Could not find the operator with name " + name.getRawValue());
                }
                return ValueTypeOperator.ValueOperator.of(operator);
            }).build());

    /**
     * ----------------------------------- NBT OPERATORS -----------------------------------
     */

    /**
     * The number of entries in an NBT tag
     */
    public static final IOperator NBT_SIZE = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).operatorName("size").symbol("NBT.size")
            .function(OperatorBuilders.FUNCTION_NBT_TO_INT.build(
                NBTTagCompound::getSize
            )).build());

    /**
     * The list of keys in an NBT tag
     */
    public static final IOperator NBT_KEYS = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("keys").symbol("NBT.keys")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtKeys(value.getRawValue()));
            }).build());

    /**
     * If an NBT tag has the given key
     */
    public static final IOperator NBT_HASKEY = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.BOOLEAN).operatorName("haskey").symbol("NBT.has_key")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_BOOLEAN.build(
                Optional::isPresent
            )).build());

    /**
     * The NBT value type of an entry
     */
    public static final IOperator NBT_VALUE_TYPE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.STRING).operatorName("type").symbol("NBT.type")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_STRING.build(tag -> {
                if (tag.isPresent()) {
                    try {
                        return NBTBase.NBT_TYPES[tag.get().getId()];
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
                return "null";
            })).build());

    /**
     * The NBT boolean value
     */
    public static final IOperator NBT_VALUE_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.BOOLEAN).operatorName("valueBoolean").symbol("NBT.boolean")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_BOOLEAN.build(
                tag -> tag.orNull() instanceof NBTPrimitive && ((NBTPrimitive) tag.orNull()).getByte() != 0
            )).build());

    /**
     * The NBT integer value
     */
    public static final IOperator NBT_VALUE_INTEGER = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.INTEGER).operatorName("valueInteger").symbol("NBT.integer")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_INT.build(
                tag -> tag.orNull() instanceof NBTPrimitive ? ((NBTPrimitive) tag.orNull()).getInt() : 0
            )).build());

    /**
     * The NBT long value
     */
    public static final IOperator NBT_VALUE_LONG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LONG).operatorName("valueLong").symbol("NBT.long")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_LONG.build(
                tag -> tag.orNull() instanceof NBTPrimitive ? ((NBTPrimitive) tag.orNull()).getLong() : 0L
            )).build());

    /**
     * The NBT double value
     */
    public static final IOperator NBT_VALUE_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.DOUBLE).operatorName("valueDouble").symbol("NBT.double")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_DOUBLE.build(
                tag -> tag.orNull() instanceof NBTPrimitive ? ((NBTPrimitive) tag.orNull()).getDouble() : 0D
            )).build());

    /**
     * The NBT string value
     */
    public static final IOperator NBT_VALUE_STRING = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.STRING).operatorName("valueString").symbol("NBT.string")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_STRING.build(
                tag -> tag.orNull() instanceof NBTTagString ? ((NBTTagString) tag.orNull()).getString() : ""
            )).build());

    /**
     * The NBT tag value
     */
    public static final IOperator NBT_VALUE_TAG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.NBT).operatorName("valueTag").symbol("NBT.tag")
            .function(OperatorBuilders.FUNCTION_NBT_ENTRY_TO_NBT.build(
                tag -> tag.orNull() instanceof NBTTagCompound ? (NBTTagCompound) tag.orNull() : new NBTTagCompound()
            )).build());

    /**
     * The NBT tag list value
     */
    public static final IOperator NBT_VALUE_LIST_TAG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("valueListTag").symbol("NBT.list_tag")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListTag(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * The NBT boolean list value
     */
    public static final IOperator NBT_VALUE_LIST_BYTE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("valueListByte").symbol("NBT.list_byte")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListByte(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * The NBT int list value
     */
    public static final IOperator NBT_VALUE_LIST_INT = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("valueListInt").symbol("NBT.list_int")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListInt(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * Remove an entry from an NBT tag
     */
    public static final IOperator NBT_WITHOUT = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.NBT).operatorName("without").symbol("NBT.without")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt = variables.getValue(0, ValueTypes.NBT);
                NBTTagCompound tag = valueNbt.getRawValue();
                ValueTypeString.ValueString valueString = variables.getValue(1, ValueTypes.STRING);
                String key = valueString.getRawValue();
                if (tag.hasKey(key)) {
                    // Copy the tag to ensure immutability
                    tag = tag.copy();
                    tag.removeTag(key);
                }
                return ValueTypeNbt.ValueNbt.of(tag);
            }).build());



    /**
     * Set an NBT boolean value
     */
    public static final IOperator NBT_WITH_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.BOOLEAN)
            .operatorName("withBoolean").symbol("NBT.with_boolean")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeBoolean.ValueBoolean value = input.getRight().getValue(0, ValueTypes.BOOLEAN);
                NBTTagCompound tag = input.getLeft();
                tag.setBoolean(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT short value
     */
    public static final IOperator NBT_WITH_SHORT = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.INTEGER)
            .operatorName("withShort").symbol("NBT.with_short")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeInteger.ValueInteger value = input.getRight().getValue(0, ValueTypes.INTEGER);
                NBTTagCompound tag = input.getLeft();
                tag.setShort(input.getMiddle(), (short) value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT integer value
     */
    public static final IOperator NBT_WITH_INTEGER = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.INTEGER)
            .operatorName("withInteger").symbol("NBT.with_integer")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeInteger.ValueInteger value = input.getRight().getValue(0, ValueTypes.INTEGER);
                NBTTagCompound tag = input.getLeft();
                tag.setInteger(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT long value
     */
    public static final IOperator NBT_WITH_LONG = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LONG)
            .operatorName("withLong").symbol("NBT.with_long")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeLong.ValueLong value = input.getRight().getValue(0, ValueTypes.LONG);
                NBTTagCompound tag = input.getLeft();
                tag.setLong(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT double value
     */
    public static final IOperator NBT_WITH_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.DOUBLE)
            .operatorName("withDouble").symbol("NBT.with_double")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeDouble.ValueDouble value = input.getRight().getValue(0, ValueTypes.DOUBLE);
                NBTTagCompound tag = input.getLeft();
                tag.setDouble(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT float value
     */
    public static final IOperator NBT_WITH_FLOAT = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.DOUBLE)
            .operatorName("withFloat").symbol("NBT.with_float")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeDouble.ValueDouble value = input.getRight().getValue(0, ValueTypes.DOUBLE);
                NBTTagCompound tag = input.getLeft();
                tag.setFloat(input.getMiddle(), (float) value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT string value
     */
    public static final IOperator NBT_WITH_STRING = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.STRING)
            .operatorName("withString").symbol("NBT.with_string")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeString.ValueString value = input.getRight().getValue(0, ValueTypes.STRING);
                NBTTagCompound tag = input.getLeft();
                tag.setString(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT tag value
     */
    public static final IOperator NBT_WITH_TAG = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.NBT)
            .operatorName("withTag").symbol("NBT.with_tag")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeNbt.ValueNbt value = input.getRight().getValue(0, ValueTypes.NBT);
                NBTTagCompound tag = input.getLeft();
                tag.setTag(input.getMiddle(), value.getRawValue());
                return tag;
            })).build());

    /**
     * Set an NBT tag list value
     */
    public static final IOperator NBT_WITH_LIST_TAG = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("withListTag").symbol("NBT.with_tag_list")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, NBTTagCompound>() {
                @Override
                public NBTTagCompound getOutput(Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    NBTTagCompound tag = input.getLeft();
                    NBTTagList list = new NBTTagList();
                    for (IValue valueNbt : value.getRawValue()) {
                        if (value.getRawValue().getValueType() != ValueTypes.NBT) {
                            L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                    L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    NBT_WITH_LIST_TAG.getLocalizedNameFull(),
                                    value.getType(), 1, ValueTypes.NBT);
                            throw new EvaluationException(error.localize());
                        }
                        list.appendTag(((ValueTypeNbt.ValueNbt) valueNbt).getRawValue());
                    }
                    tag.setTag(input.getMiddle(), list);
                    return tag;
                }
            })).build());

    /**
     * Set an NBT byte list value
     */
    public static final IOperator NBT_WITH_LIST_BYTE = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("withListByte").symbol("NBT.with_byte_list")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, NBTTagCompound>() {
                @Override
                public NBTTagCompound getOutput(Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    NBTTagCompound tag = input.getLeft();
                    NBTTagList list = new NBTTagList();
                    for (IValue valueNbt : value.getRawValue()) {
                        if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                            L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                    L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    NBT_WITH_LIST_BYTE.getLocalizedNameFull(),
                                    value.getType(), 1, ValueTypes.INTEGER);
                            throw new EvaluationException(error.localize());
                        }
                        list.appendTag(new NBTTagByte((byte) ((ValueTypeInteger.ValueInteger) valueNbt).getRawValue()));
                    }
                    tag.setTag(input.getMiddle(), list);
                    return tag;
                }
            })).build());

    /**
     * Set an NBT int list value
     */
    public static final IOperator NBT_WITH_LIST_INT = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("withListInt").symbol("NBT.with_int_list")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, NBTTagCompound>() {
                @Override
                public NBTTagCompound getOutput(Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    NBTTagCompound tag = input.getLeft();
                    NBTTagList list = new NBTTagList();
                    for (IValue valueNbt : value.getRawValue()) {
                        if (value.getRawValue().getValueType() != ValueTypes.INTEGER) {
                            L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                    L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    NBT_WITH_LIST_INT.getLocalizedNameFull(),
                                    value.getType(), 1, ValueTypes.INTEGER);
                            throw new EvaluationException(error.localize());
                        }
                        list.appendTag(new NBTTagInt(((ValueTypeInteger.ValueInteger) valueNbt).getRawValue()));
                    }
                    tag.setTag(input.getMiddle(), list);
                    return tag;
                }
            })).build());

    /**
     * Set an NBT long list value
     */
    public static final IOperator NBT_WITH_LIST_LONG = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("withListLong").symbol("NBT.with_list_long")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter>, NBTTagCompound>() {
                @Override
                public NBTTagCompound getOutput(Triple<NBTTagCompound, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    NBTTagCompound tag = input.getLeft();
                    NBTTagList list = new NBTTagList();
                    for (IValue valueNbt : value.getRawValue()) {
                        if (value.getRawValue().getValueType() != ValueTypes.LONG) {
                            L10NHelpers.UnlocalizedString error = new L10NHelpers.UnlocalizedString(
                                    L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    NBT_WITH_LIST_LONG.getLocalizedNameFull(),
                                    value.getType(), 1, ValueTypes.LONG);
                            throw new EvaluationException(error.localize());
                        }
                        list.appendTag(new NBTTagLong(((ValueTypeLong.ValueLong) valueNbt).getRawValue()));
                    }
                    tag.setTag(input.getMiddle(), list);
                    return tag;
                }
            })).build());

    /**
     * Check if the first NBT tag is a subset of the second NBT tag.
     */
    public static final IOperator NBT_SUBSET = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.BOOLEAN).operatorName("subset").symbol("NBT.⊆")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                NBTTagCompound a = valueNbt0.getRawValue();
                NBTTagCompound b = valueNbt1.getRawValue();
                return ValueTypeBoolean.ValueBoolean.of(NbtHelpers.nbtMatchesSubset(a, b, true));
            }).build());

    /**
     * The union of the given NBT tags. Nested tags will be joined recusively.
     */
    public static final IOperator NBT_UNION = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("union").symbol("NBT.∪")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                NBTTagCompound a = valueNbt0.getRawValue();
                NBTTagCompound b = valueNbt1.getRawValue();
                return ValueTypeNbt.ValueNbt.of(NbtHelpers.union(a, b));
            }).build());

    /**
     * The intersection of the given NBT tags. Nested tags will be intersected recusively.
     */
    public static final IOperator NBT_INTERSECTION = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("intersection").symbol("NBT.∩")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                NBTTagCompound a = valueNbt0.getRawValue();
                NBTTagCompound b = valueNbt1.getRawValue();
                return ValueTypeNbt.ValueNbt.of(NbtHelpers.intersection(a, b));
            }).build());

    /**
     * The difference of the given NBT tags. Nested tags will be subtracted recusively.
     */
    public static final IOperator NBT_MINUS = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("minus").symbol("NBT.∖")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                NBTTagCompound a = valueNbt0.getRawValue();
                NBTTagCompound b = valueNbt1.getRawValue();
                return ValueTypeNbt.ValueNbt.of(NbtHelpers.minus(a, b));
            }).build());
    /**
     * ----------------------------------- INGREDIENTS OPERATORS -----------------------------------
     */

    /**
     * The list of items.
     */
    public static final IOperator INGREDIENTS_ITEMS = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorName("items").symbol("Ingr.items")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.ITEMSTACK))
            .build());

    /**
     * The list of fluids
     */
    public static final IOperator INGREDIENTS_FLUIDS = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorName("fluids").symbol("Ingr.fluids")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.FLUIDSTACK))
            .build());

    /**
     * The list of fluids
     */
    public static final IOperator INGREDIENTS_ENERGIES = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorName("energies").symbol("Ingr.energies")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.ENERGY))
            .build());

    /**
     * Set an ingredient item
     */
    public static final IOperator INGREDIENTS_WITH_ITEM = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_ITEMSTACK
            .operatorName("withItem").symbol("Ingr.with_item")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueObjectTypeItemStack.ValueItemStack itemStack = variables.getValue(2, ValueTypes.OBJECT_ITEMSTACK);
                if (!value.getRawValue().isPresent()) {
                    return value;
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.ITEMSTACK, itemStack.getRawValue()));
            }).build());

    /**
     * Set an ingredient fluid
     */
    public static final IOperator INGREDIENTS_WITH_FLUID = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_FLUIDSTACK
            .operatorName("withFluid").symbol("Ingr.with_fluid")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueObjectTypeFluidStack.ValueFluidStack fluidStack = variables.getValue(2, ValueTypes.OBJECT_FLUIDSTACK);
                if (!value.getRawValue().isPresent()) {
                    return value;
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.FLUIDSTACK, fluidStack.getRawValue().orNull()));
            }).build());

    /**
     * Set an ingredient energy
     */
    public static final IOperator INGREDIENTS_WITH_ENERGY = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_INTEGER
            .operatorName("withEnergy").symbol("Ingr.with_energy")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger energy = variables.getValue(2, ValueTypes.INTEGER);
                if (!value.getRawValue().isPresent()) {
                    return value;
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.ENERGY, energy.getRawValue()));
            }).build());

    /**
     * Set the list of items
     */
    public static final IOperator INGREDIENTS_WITH_ITEMS = REGISTRY.register(OperatorBuilders.INGREDIENTS_2_LIST
            .operatorName("withItems").symbol("Ingr.with_items")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> list = variables.getValue(1, ValueTypes.LIST);
                if (!valueIngredients.getRawValue().isPresent()) {
                    return valueIngredients;
                }
                IMixedIngredients baseIngredients = valueIngredients.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsList<>(baseIngredients,
                        IngredientComponent.ITEMSTACK, OperatorBuilders.unwrapIngredientComponentList(IngredientComponent.ITEMSTACK, list)));
            }).build());

    /**
     * Set the list of fluids
     */
    public static final IOperator INGREDIENTS_WITH_FLUIDS = REGISTRY.register(OperatorBuilders.INGREDIENTS_2_LIST
            .operatorName("withFluids").symbol("Ingr.with_fluids")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> list = variables.getValue(1, ValueTypes.LIST);
                if (!valueIngredients.getRawValue().isPresent()) {
                    return valueIngredients;
                }
                IMixedIngredients baseIngredients = valueIngredients.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsList<>(baseIngredients,
                        IngredientComponent.FLUIDSTACK, OperatorBuilders.unwrapIngredientComponentList(IngredientComponent.FLUIDSTACK, list)));
            }).build());

    /**
     * Set the list of energies
     */
    public static final IOperator INGREDIENTS_WITH_ENERGIES = REGISTRY.register(OperatorBuilders.INGREDIENTS_2_LIST
            .renderPattern(IConfigRenderPattern.INFIX_VERYLONG)
            .operatorName("withEnergies").symbol("Ingr.with_energies")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = variables.getValue(1, ValueTypes.LIST);
                if (!valueIngredients.getRawValue().isPresent()) {
                    return valueIngredients;
                }
                IMixedIngredients baseIngredients = valueIngredients.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsList<>(baseIngredients,
                        IngredientComponent.ENERGY, OperatorBuilders.unwrapIngredientComponentList(IngredientComponent.ENERGY, list)));
            }).build());

    /**
     * ----------------------------------- RECIPE OPERATORS -----------------------------------
     */

    /**
     * The input ingredients of a recipe
     */
    public static final IOperator RECIPE_INPUT = REGISTRY.register(OperatorBuilders.RECIPE_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_INGREDIENTS)
            .operatorName("input").symbol("recipe_in")
            .function(variables -> {
                ValueObjectTypeRecipe.ValueRecipe value = variables.getValue(0, ValueTypes.OBJECT_RECIPE);
                if (value.getRawValue().isPresent()) {
                    return ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.fromRecipeInput(value.getRawValue().get()));
                }
                return ValueObjectTypeIngredients.ValueIngredients.of(null);
            }).build());

    /**
     * The output ingredients of a recipe
     */
    public static final IOperator RECIPE_OUTPUT = REGISTRY.register(OperatorBuilders.RECIPE_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_INGREDIENTS)
            .operatorName("output").symbol("recipe_out")
            .function(variables -> {
                ValueObjectTypeRecipe.ValueRecipe value = variables.getValue(0, ValueTypes.OBJECT_RECIPE);
                if (value.getRawValue().isPresent()) {
                    return ValueObjectTypeIngredients.ValueIngredients.of(value.getRawValue().get().getOutput());
                }
                return ValueObjectTypeIngredients.ValueIngredients.of(null);
            }).build());

    /**
     * Set the input ingredients of a recipe
     */
    public static final IOperator RECIPE_WITH_INPUT = REGISTRY.register(OperatorBuilders.RECIPE_2_INFIX
            .output(ValueTypes.OBJECT_RECIPE)
            .operatorName("withInput").symbol("Recipe.with_in")
            .function(variables -> {
                ValueObjectTypeRecipe.ValueRecipe valueRecipe = variables.getValue(0, ValueTypes.OBJECT_RECIPE);
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(1, ValueTypes.OBJECT_INGREDIENTS);
                if (valueRecipe.getRawValue().isPresent() && valueIngredients.getRawValue().isPresent()) {
                    IMixedIngredients ingredients = valueIngredients.getRawValue().get();
                    Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
                    for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
                        IIngredientMatcher matcher = component.getMatcher();
                        inputs.put(component, (List) ingredients.getInstances(component)
                                .stream()
                                .map(instance -> new PrototypedIngredientAlternativesList(Collections.singletonList(new PrototypedIngredient(component, instance, matcher.getExactMatchCondition()))))
                                .collect(Collectors.toList()));
                    }
                    return ValueObjectTypeRecipe.ValueRecipe.of(new RecipeDefinition(
                            inputs,
                            valueRecipe.getRawValue().get().getOutput()
                    ));
                }
                return ValueObjectTypeRecipe.ValueRecipe.of(null);
            }).build());

    /**
     * Set the output ingredients of a recipe
     */
    public static final IOperator RECIPE_WITH_OUTPUT = REGISTRY.register(OperatorBuilders.RECIPE_2_INFIX
            .output(ValueTypes.OBJECT_RECIPE)
            .operatorName("withOutput").symbol("Recipe.with_out")
            .function(variables -> {
                ValueObjectTypeRecipe.ValueRecipe valueRecipe = variables.getValue(0, ValueTypes.OBJECT_RECIPE);
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(1, ValueTypes.OBJECT_INGREDIENTS);
                if (valueRecipe.getRawValue().isPresent() && valueIngredients.getRawValue().isPresent()) {
                    IRecipeDefinition recipe = valueRecipe.getRawValue().get();
                    Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
                    for (IngredientComponent<?, ?> component : recipe.getInputComponents()) {
                        inputs.put(component, (List) recipe.getInputs(component));
                    }
                    return ValueObjectTypeRecipe.ValueRecipe.of(new RecipeDefinition(
                            inputs,
                            valueIngredients.getRawValue().get()
                    ));
                }
                return ValueObjectTypeRecipe.ValueRecipe.of(null);
            }).build());

    /**
     * Create a recipe from two the given I/O ingredients
     */
    public static final IOperator RECIPE_WITH_INPUT_OUTPUT = REGISTRY.register(OperatorBuilders.RECIPE_2_PREFIX
            .output(ValueTypes.OBJECT_RECIPE)
            .operatorName("withInputOutput").symbol("Recipe.with_io")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIn = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueObjectTypeIngredients.ValueIngredients valueOut = variables.getValue(1, ValueTypes.OBJECT_INGREDIENTS);
                if (valueIn.getRawValue().isPresent() && valueOut.getRawValue().isPresent()) {
                    IMixedIngredients ingredients = valueIn.getRawValue().get();
                    Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
                    for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
                        IIngredientMatcher matcher = component.getMatcher();
                        inputs.put(component, (List) ingredients.getInstances(component)
                                .stream()
                                .map(instance -> new PrototypedIngredientAlternativesList(Collections.singletonList(new PrototypedIngredient(component, instance, matcher.getExactMatchCondition()))))
                                .collect(Collectors.toList()));
                    }
                    return ValueObjectTypeRecipe.ValueRecipe.of(new RecipeDefinition(
                            inputs,
                            valueOut.getRawValue().get()
                    ));
                }
                return ValueObjectTypeRecipe.ValueRecipe.of(null);
            }).build());

    /**
     * ------------------------------------ PARSE OPERATORS ------------------------------------
     */

    /**
     * Boolean Parse operator which takes a string of form `/(F(alse)?|[+-]?(0x|#)?0+|)/i`.
     */
    public static final IOperator PARSE_BOOLEAN = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.BOOLEAN, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      Pattern p = Pattern.compile("\\A(F(alse)?|[+-]?(0x|#)?0+|)\\z", Pattern.CASE_INSENSITIVE);
      return ValueTypeBoolean.ValueBoolean.of(!p.matcher(value.getRawValue().trim()).matches());
    }));

    /**
     * Double Parse operator which takes a string of a form Double.parseDouble(),
     * `/([+-]?)(Inf(inity)?|\u221E)/i`, or Long.decode() can consume.
     */
    public static final IOperator PARSE_DOUBLE = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.DOUBLE, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      try {
        return ValueTypeDouble.ValueDouble.of(Double.parseDouble(value.getRawValue()));
      } catch (NumberFormatException e) {
        try {
          // \u221E = infinity symbol
          Pattern p = Pattern.compile("\\A([+-]?)(Inf(inity)?|\u221E)\\z", Pattern.CASE_INSENSITIVE);
          Matcher m = p.matcher(value.getRawValue().trim());
          if (m.matches()){
            if (m.group(1).equals("-")){
              return ValueTypeDouble.ValueDouble.of(Double.NEGATIVE_INFINITY);
            }
            return ValueTypeDouble.ValueDouble.of(Double.POSITIVE_INFINITY);
          }
          // Try as a long
          return ValueTypeDouble.ValueDouble.of((double) Long.decode(value.getRawValue()));
        } catch (NumberFormatException e2) {
          throw new EvaluationException("'" + value.getRawValue() + "' is not parsable as a 'DOUBLE'");
        }
      }
    }));

    /**
     * Integer Parse operator which takes a string of a form Integer.decode() can consume.
     */
    public static final IOperator PARSE_INTEGER = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.INTEGER, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      try{
        return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
      } catch (NumberFormatException e) {
        throw new EvaluationException("'" + value.getRawValue() + "' is not parsable as a 'INTEGER'");
      }
    }));

    /**
     * Long Parse operator which takes a string of a form Long.decode() can consume.
     */
    public static final IOperator PARSE_LONG = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.LONG, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      try {
        return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
      } catch (NumberFormatException e) {
        throw new EvaluationException("'" + value.getRawValue() + "' is not parsable as a 'LONG'");
      }
    }));

    /**
     * NBT Parse operator which takes a string of a form ValueTypeNbt().deserialize() can consume.
     */
    public static final IOperator PARSE_NBT = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.NBT, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      try {
        return new ValueTypeNbt().deserialize(value.getRawValue());
      } catch (IllegalArgumentException e) {
        throw new EvaluationException("'" + value.getRawValue() + "' is not parsable as a 'NBT'");
      }
    }));

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

    /**
     * Constant operator with two any inputs and one any output
     */
    public static final GeneralOperator GENERAL_CONSTANT = REGISTRY.register(new GeneralConstantOperator("K", "constant"));

}
