package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Lombok;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
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
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.nbt.path.INbtPathExpression;
import org.cyclops.cyclopscore.nbt.path.NbtParseException;
import org.cyclops.cyclopscore.nbt.path.NbtPath;
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
import org.cyclops.integrateddynamics.core.ingredient.ExtendedIngredientsList;
import org.cyclops.integrateddynamics.core.ingredient.ExtendedIngredientsSingle;

import java.util.*;
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
    public static final IOperator LOGICAL_AND = REGISTRY.register(OperatorBuilders.LOGICAL_2.symbol("&&").operatorInteract("and")
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
    public static final IOperator LOGICAL_OR = REGISTRY.register(OperatorBuilders.LOGICAL_2.symbol("||").operatorInteract("or")
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
    public static final IOperator LOGICAL_NOT = REGISTRY.register(OperatorBuilders.LOGICAL_1_PREFIX.symbol("!").operatorInteract("not")
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
                    "!&&", "nand", "nand", IConfigRenderPattern.INFIX, "logical"));

    /**
     * Short-circuit logical NAND operator with two input booleans and one output boolean.
     */
    public static final IOperator LOGICAL_NOR = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(LOGICAL_OR).build(
                    "!||", "nor", "nor", IConfigRenderPattern.INFIX, "logical"));

    /**
     * ----------------------------------- ARITHMETIC OPERATORS -----------------------------------
     */

    /**
     * Arithmetic ADD operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_ADDITION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("+").operatorName("addition").interactName("add")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.add(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MINUS operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("-").operatorName("subtraction").interactName("subtract")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.subtract(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MULTIPLY operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("*").operatorName("multiplication").interactName("multiply")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.multiply(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic DIVIDE operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_DIVISION = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("/").operatorName("division").interactName("divide")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.divide(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MAX operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_MAXIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("max").operatorName("maximum").interactName("max")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.max(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic MIN operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_MINIMUM = REGISTRY.register(OperatorBuilders.ARITHMETIC_2_PREFIX.symbol("min").operatorName("minimum").interactName("min")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.min(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * Arithmetic INCREMENT operator with one input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_INCREMENT = REGISTRY.register(OperatorBuilders.ARITHMETIC_1_SUFFIX.symbol("++").operatorInteract("increment")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.increment(variables.getVariables()[0])
            ).build());

    /**
     * Arithmetic DECREMENT operator with one input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_DECREMENT = REGISTRY.register(OperatorBuilders.ARITHMETIC_1_SUFFIX.symbol("--").operatorInteract("decrement")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.decrement(variables.getVariables()[0])
            ).build());

    /**
     * Arithmetic MODULO operator with two input numbers and one output number.
     */
    public static final IOperator ARITHMETIC_MODULUS = REGISTRY.register(OperatorBuilders.ARITHMETIC_2.symbol("%").operatorInteract("modulus")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.modulus(variables.getVariables()[0], variables.getVariables()[1])
            ).build());

    /**
     * ----------------------------------- INTEGER OPERATORS -----------------------------------
     */

     private static final ValueTypeInteger.ValueInteger ZERO = ValueTypeInteger.ValueInteger.of(0);

    /**
     * Integer MODULO operator with two input integers and one output integer.
     *
     * @deprecated by {@link #ARITHMETIC_MODULUS} - TODO remove in next major update
     */
    @Deprecated
    public static final IOperator INTEGER_MODULUS = REGISTRY.register(OperatorBuilders.INTEGER_2.symbol("%").operatorName("modulus").interactName("modulus_old")
            .function(variables -> {
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() == 0) { // You can not divide by zero
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_DIVIDEBYZERO));
                } else if (b.getRawValue() == 1) { // If b is neutral element for division
                    return ZERO;
                } else {
                    ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                    return ValueTypeInteger.ValueInteger.of(a.getRawValue() % b.getRawValue());
                }
            }).build());

    /**
     * Integer INCREMENT operator with one input integer and one output integer.
     *
     * @deprecated by {@link #ARITHMETIC_INCREMENT} - TODO remove in next major update
     */
    @Deprecated
    public static final IOperator INTEGER_INCREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("++").operatorName("increment").interactName("increment_old")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() + 1);
            }).build());

    /**
     * Integer DECREMENT operator with one input integer and one output integer.
     *
     * @deprecated by {@link #ARITHMETIC_DECREMENT} - TODO remove in next major update
     */
    @Deprecated
    public static final IOperator INTEGER_DECREMENT = REGISTRY.register(OperatorBuilders.INTEGER_1_SUFFIX.symbol("--").operatorName("decrement").interactName("decrement_old")
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
            .symbol("==").operatorInteract("equals")
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(variables.getValue(0).equals(variables.getValue(1)))
            )
            .typeValidator((operator, input) -> {
                // Input size checking
                int requiredInputLength = operator.getRequiredInputLength();
                if(input.length != requiredInputLength) {
                    return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
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
                        return Component.translatable(L10NValues.OPERATOR_ERROR_NULLTYPE, operator.getOperatorName(), Integer.toString(i));
                    }
                    if(i == 0) {
                        temporarySecondInputType = inputType;
                    } else if(i == 1) {
                        if(!ValueHelpers.correspondsTo(temporarySecondInputType, inputType)) {
                            return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    operator.getOperatorName(), Component.translatable(inputType.getTranslationKey()),
                                    Integer.toString(i), Component.translatable(temporarySecondInputType.getTranslationKey()));
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
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol(">").operatorName("gt").interactName("greaterThan")
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.greaterThan(variables.getVariables()[0], variables.getVariables()[1]))
            ).build());

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final IOperator RELATIONAL_LT = REGISTRY.register(OperatorBuilders.RELATIONAL_2
            .inputTypes(2, ValueTypes.CATEGORY_NUMBER).symbol("<").operatorName("lt").interactName("lessThan")
            .function(
                variables -> ValueTypeBoolean.ValueBoolean.of(ValueTypes.CATEGORY_NUMBER.lessThan(variables.getVariables()[0], variables.getVariables()[1]))
            ).build());

    /**
     * Relational != operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_NOTEQUALS = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(RELATIONAL_EQUALS).build(
                    "!=", "notequals", "notEquals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &gt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_GE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_GT).build(
                    ">=", "ge", "greaterThanOrEquals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &lt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_LE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_LT).build(
                    "<=", "le", "lessThanOrEquals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * ----------------------------------- BINARY OPERATORS -----------------------------------
     */

    /**
     * Binary AND operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_AND = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("&").operatorName("and").interactName("binaryAnd")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() & b.getRawValue());
            }).build());

    /**
     * Binary OR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_OR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("|").operatorName("or").interactName("binaryOr")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() | b.getRawValue());
            }).build());

    /**
     * Binary XOR operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_XOR = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("^").operatorInteract("xor")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() ^ b.getRawValue());
            }).build());

    /**
     * Binary COMPLEMENT operator with one input integers and one output integers.
     */
    public static final IOperator BINARY_COMPLEMENT = REGISTRY.register(OperatorBuilders.BINARY_1_PREFIX.symbol("~").operatorInteract("complement")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(~a.getRawValue());
            }).build());

    /**
     * Binary &lt;&lt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_LSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol("<<").operatorName("lshift").interactName("leftShift")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() << b.getRawValue());
            }).build());

    /**
     * Binary &gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>").operatorName("rshift").interactName("rightShift")
            .function(variables -> {
                ValueTypeInteger.ValueInteger a = variables.getValue(0, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue() >> b.getRawValue());
            }).build());

    /**
     * Binary &gt;&gt;&gt; operator with two input integers and one output integers.
     */
    public static final IOperator BINARY_RZSHIFT = REGISTRY.register(OperatorBuilders.BINARY_2.symbol(">>>").operatorName("rzshift").interactName("unsignedRightShift")
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
    public static final IOperator STRING_LENGTH = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX.symbol("len").operatorInteract("length")
            .output(ValueTypes.INTEGER).function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue().length());
            }).build());

    /**
     * String concat operator with two input strings and one output string.
     */
    public static final IOperator STRING_CONCAT = REGISTRY.register(OperatorBuilders.STRING_2.symbol("+").operatorInteract("concat")
            .function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString b = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeString.ValueString.of(a.getRawValue() + b.getRawValue());
            }).build());

    /**
     * String contains operator which checks whether a given (literal) string is contained in the given string.
     */
    public static final IOperator STRING_CONTAINS = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperatorInteract("contains")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().contains(search.getRawValue()));
            }).build());

    /**
     * String match operator which checks whether a given regular expression is contained within a string.
     */
    public static final IOperator STRING_CONTAINS_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("contains_regex").interactName("containsRegex")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                    return ValueTypeBoolean.ValueBoolean.of(m.find());
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                            pattern.getRawValue()));
                }
            }).build());

    /**
     * String match operator which checks whether a given regular expression matches a string.
     */
    public static final IOperator STRING_MATCHES_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("matches_regex").interactName("matchesRegex")
            .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                try {
                    Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                    return ValueTypeBoolean.ValueBoolean.of(m.matches());
                } catch (PatternSyntaxException e) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                            pattern.getRawValue()));
                }
            }).build());

    /**
     * String operator which returns the integral index of the first position where the search string appears in the given string.
     */
    public static final IOperator STRING_INDEX_OF = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("index_of").interactName("indexOf")
        .output(ValueTypes.INTEGER).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeInteger.ValueInteger.of(str.getRawValue().indexOf(search.getRawValue()));
            }).build());

    /**
     * String operator which returns the integral index where the a substring matching the regular expression appears in the given string.
     */
    public static final IOperator STRING_INDEX_OF_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("index_of_regex").interactName("indexOfRegex")
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
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                            pattern.getRawValue()));
                }
            }).build());

    /**
     * String match operator which checks whether a given string matches the beginning of the given string.
     */
    public static final IOperator STRING_STARTS_WITH = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("starts_with").interactName("startsWith")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().startsWith(search.getRawValue()));
            }).build());

    /**
     * String match operator which checks whether a given string matches the end of the given string.
     */
    public static final IOperator STRING_ENDS_WITH = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("ends_with").interactName("endsWith")
        .output(ValueTypes.BOOLEAN).function(variables -> {
                ValueTypeString.ValueString search = variables.getValue(0, ValueTypes.STRING);
                ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeBoolean.ValueBoolean.of(str.getRawValue().endsWith(search.getRawValue()));
            }).build());

    /**
     * String operator which splits on the given (literal) delimiter the input string .
     */
    public static final IOperator STRING_SPLIT_ON = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperator("split_on").interactName("splitOn")
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
    public static final IOperator STRING_SPLIT_ON_REGEX = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("split_on_regex").interactName("splitOnRegex")
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
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                            pattern.getRawValue()));
                }
            }).build());

    /**
     * String operator which takes the substring of the given string between the two integer indices.
     */
    public static final IOperator STRING_SUBSTRING = REGISTRY.register(OperatorBuilders.STRING.symbolOperatorInteract("substring")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.INTEGER, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeInteger.ValueInteger from = variables.getValue(0, ValueTypes.INTEGER);
            ValueTypeInteger.ValueInteger to = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (from.getRawValue() > to.getRawValue()) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_SUBSTRING_TOGREATERTHANFROM));
            }
            if (from.getRawValue() < 0 || to.getRawValue() < 0) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_SUBSTRING_INDEXNEGATIVE));
            }
            int stringLength = str.getRawValue().length();
            if (from.getRawValue() > stringLength || to.getRawValue() > stringLength) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_SUBSTRING_LONGERTHANSTRING));
            }
            return ValueTypeString.ValueString.of(str.getRawValue().substring(from.getRawValue(), to.getRawValue()));
        }).build());


    /**
     * String operator which matches against a regex and takes the group at the index of the integer given (including zero), in the input string. It is invalid for the pattern to not match.
     */
    public static final IOperator STRING_REGEX_GROUP = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("regex_group").interactName("regexGroup")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.STRING, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.STRING)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeInteger.ValueInteger group = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (group.getRawValue() < 0) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_GROUP_INDEXNEGATIVE));
            }
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                if (m.find()) {
                    String result = m.group(group.getRawValue());
                    return ValueTypeString.ValueString.of(result == null ? "" : result);
                } else {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_GROUP_NOMATCH,
                            str.getRawValue(), pattern.getRawValue()));
                }
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                        pattern.getRawValue()));
            } catch (IndexOutOfBoundsException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_GROUP_NOMATCHGROUP,
                        str.getRawValue(), pattern.getRawValue(), group.getRawValue()));
            }
        }).build()
    );

    /**
     * String operator which matches against a regex the input string and returns a list containing all groups matched (including zero). An empty list is returned if the regex does not match.
     */
    public static final IOperator STRING_REGEX_GROUPS = REGISTRY.register(OperatorBuilders.STRING_2_LONG.symbolOperator("regex_groups").interactName("regexGroups")
        .output(ValueTypes.LIST)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeString.ValueString str = variables.getValue(1, ValueTypes.STRING);
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                if (m.find()) {
                    List<ValueTypeString.ValueString> values = Lists.newArrayList();
                    for (int i = 0; i <= m.groupCount(); i++) {
                        String result = m.group(i);
                        values.add(ValueTypeString.ValueString.of(result == null ? "" : result));
                    }
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.STRING, Collections.<ValueTypeString.ValueString>emptyList());
                }
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                        pattern.getRawValue()));
            }
        }).build()
    );

    /**
     * String operator which finds all matches of the regular expression in the given string and returns the given group for each match.
     */
    public static final IOperator STRING_REGEX_SCAN = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("regex_scan").interactName("regexScan")
        .renderPattern(IConfigRenderPattern.PREFIX_3_LONG)
        .inputTypes(ValueTypes.STRING, ValueTypes.INTEGER, ValueTypes.STRING)
        .output(ValueTypes.LIST)
        .function(variables -> {
            ValueTypeString.ValueString pattern = variables.getValue(0, ValueTypes.STRING);
            ValueTypeInteger.ValueInteger group = variables.getValue(1, ValueTypes.INTEGER);
            ValueTypeString.ValueString str = variables.getValue(2, ValueTypes.STRING);
            if (group.getRawValue() < 0) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEXSCAN_INDEXNEGATIVE));
            }
            try {
                Matcher m = Pattern.compile(pattern.getRawValue()).matcher(str.getRawValue());
                List<ValueTypeString.ValueString> values = Lists.newArrayList();
                while (m.find()) {
                    String match = m.group(group.getRawValue());
                    if (match != null) {
                        values.add(ValueTypeString.ValueString.of(match));
                    }
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.STRING, values);
            } catch (PatternSyntaxException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                        pattern.getRawValue()));
            } catch (IndexOutOfBoundsException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEXSCAN_NOMATCHGROUP,
                        str.getRawValue(), pattern.getRawValue(), group.getRawValue()));
            }
        }).build()
    );

    /**
     * String operator which, finds all the matches of the (literal) search and replaces them with the given replacement, in the input string.
     */
    public static final IOperator STRING_REPLACE = REGISTRY.register(OperatorBuilders.STRING.symbolOperatorInteract("replace")
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
    public static final IOperator STRING_REPLACE_REGEX = REGISTRY.register(OperatorBuilders.STRING.symbolOperator("replace_regex").interactName("replaceRegex")
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
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REGEX_INVALID,
                        pattern.getRawValue()));
            } catch (IndexOutOfBoundsException e) {
                throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REPLACEREGEX_INVALIDGROUP,
                        replacement.getRawValue(), e.getMessage()));
            }
        }).build()
    );

    /**
     * String operator to join a list using a string delimiter
     */
    public static final IOperator STRING_JOIN = REGISTRY.register(OperatorBuilders.STRING.symbolOperatorInteract("join")
            .renderPattern(IConfigRenderPattern.PREFIX_2)
            .inputTypes(ValueTypes.STRING, ValueTypes.LIST)
            .output(ValueTypes.STRING)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    // Prepare values
                    ValueTypeString.ValueString delimiter = variables.getValue(0, ValueTypes.STRING);
                    ValueTypeList.ValueList<?, ?> elements = variables.getValue(1, ValueTypes.LIST);
                    if (!ValueHelpers.correspondsTo(elements.getRawValue().getValueType(), ValueTypes.STRING)) {
                        throw new EvaluationException(Component.translatable(
                                L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                Component.translatable(elements.getRawValue().getValueType().getTranslationKey()),
                                Component.translatable(ValueTypes.STRING.getTranslationKey())));
                    }

                    // Don't allow joining on an infinite list
                    if (elements.getRawValue().isInfinite()) {
                        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_INFINITELIST_ILLEGAL,
                                STRING_JOIN.getLocalizedNameFull()));
                    }

                    // Join in O(n), while type-checking each element, as the list may have been of ANY type.
                    StringBuilder sb = new StringBuilder();
                    for (IValue value : elements.getRawValue()) {
                        if (value.getType() != ValueTypes.STRING) {
                            throw new EvaluationException(Component.translatable(
                                    L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                    Component.translatable(value.getType().getTranslationKey()),
                                    Component.translatable(ValueTypes.STRING.getTranslationKey())));
                        }
                        if (sb.length() > 0) {
                            sb.append(delimiter.getRawValue());
                        }
                        sb.append(((ValueTypeString.ValueString) value).getRawValue());
                    }

                    return ValueTypeString.ValueString.of(sb.toString());
                }
            }).build()
    );

    /**
     * Get a name value type name.
     */
    public static final IOperator NAMED_NAME = REGISTRY.register(OperatorBuilders.STRING_2.symbolOperatorInteract("name")
            .inputType(ValueTypes.CATEGORY_NAMED).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(
                variables -> ValueTypeString.ValueString.of(ValueTypes.CATEGORY_NAMED.getName(variables.getVariables()[0]))
            ).build());

    /**
     * Get a unique name value type name.
     */
    public static final IOperator UNIQUELYNAMED_UNIQUENAME = REGISTRY.register(OperatorBuilders.STRING_2.symbol("uname").operatorName("unique_name").interactName("uniqueName")
            .inputType(ValueTypes.CATEGORY_UNIQUELY_NAMED).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(
                variables -> ValueTypeString.ValueString.of(ValueTypes.CATEGORY_UNIQUELY_NAMED.getUniqueName(variables.getVariables()[0]))
            ).build());

    /**
     * Throw a custom error
     */
    public static final IOperator STRING_ERROR  = REGISTRY.register(OperatorBuilders.STRING_2.symbol("error").operatorName("string_error").interactName("stringError")
            .inputType(ValueTypes.STRING).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(
                (variables) -> {
                    throw new EvaluationException(Component.translatable(variables.getValue(0, ValueTypes.STRING).getRawValue()));
                }
            ).build());

    /**
     * ----------------------------------- NUMBER OPERATORS -----------------------------------
     */

    /**
     * Number round operator with one input double and one output integers.
     */
    public static final IOperator NUMBER_ROUND = REGISTRY.register(OperatorBuilders.NUMBER_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("|| ||").operatorInteract("round")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.round(variables.getVariables()[0])
            ).build());

    /**
     * Number ceil operator with one input double and one output integers.
     */
    public static final IOperator NUMBER_CEIL = REGISTRY.register(OperatorBuilders.NUMBER_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("⌈ ⌉").operatorInteract("ceil")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.ceil(variables.getVariables()[0])
            ).build());

    /**
     * Number floor operator with one input double and one output integers.
     */
    public static final IOperator NUMBER_FLOOR = REGISTRY.register(OperatorBuilders.NUMBER_1_PREFIX
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.INTEGER).symbol("⌊ ⌋").operatorInteract("floor")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.floor(variables.getVariables()[0])
            ).build());

    /**
     * Accepts a number, and returns a string roughly representing that number
     */
    public static final IOperator NUMBER_COMPACT = REGISTRY.register(OperatorBuilders.NUMBER_1_LONG
            .inputType(ValueTypes.CATEGORY_NUMBER).output(ValueTypes.STRING).symbolOperatorInteract("compact")
            .function(
                variables -> ValueTypes.CATEGORY_NUMBER.compact(variables.getVariables()[0])
            ).build());

    /**
     * ----------------------------------- NULLABLE OPERATORS -----------------------------------
     */

    /**
     * Check if something is null
     */
    public static final IOperator NULLABLE_ISNULL = REGISTRY.register(OperatorBuilders.NULLABLE_1_PREFIX.symbol("o").operatorName("isnull").interactName("isNull")
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
            .apply(NULLABLE_ISNULL).build("∅", "isnotnull", "isNotNull", IConfigRenderPattern.PREFIX_1, "general"));

    /**
     * ----------------------------------- LIST OPERATORS -----------------------------------
     */

    /**
     * List operator with one input list and one output integer
     */
    public static final IOperator LIST_LENGTH = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX.output(ValueTypes.INTEGER).symbol("| |").operatorInteract("length")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                return ValueTypeInteger.ValueInteger.of(a.getLength());
            }).build());

    /**
     * Check if a list is empty
     */
    public static final IOperator LIST_EMPTY = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX.output(ValueTypes.BOOLEAN).symbol("∅").operatorName("empty").interactName("isEmpty")
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
                    "o", "notempty", "isNotEmpty", IConfigRenderPattern.PREFIX_1, "list"));

    /**
     * List operator with one input list and one output integer
     */
    public static final IOperator LIST_ELEMENT = REGISTRY.register(OperatorBuilders.LIST_1_PREFIX
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.INTEGER}).output(ValueTypes.CATEGORY_ANY)
            .renderPattern(IConfigRenderPattern.INFIX).symbolOperatorInteract("get")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() < a.getLength() && b.getRawValue() >= 0) {
                    return a.get(b.getRawValue());
                } else {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_INDEXOUTOFBOUNDS, b.getRawValue(), a.getLength()));
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
            .renderPattern(IConfigRenderPattern.INFIX_2_LONG).symbolOperator("get_or_default").interactName("getOrDefault")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                ValueTypeInteger.ValueInteger b = variables.getValue(1, ValueTypes.INTEGER);
                if (b.getRawValue() < a.getLength() && b.getRawValue() >= 0) {
                    return a.get(b.getRawValue());
                } else {
                    if (!ValueHelpers.correspondsTo(a.getValueType(), variables.getVariables()[2].getType())) {
                        throw new EvaluationException(Component.translatable(
                                L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                                Component.translatable(a.getValueType().getTranslationKey()),
                                Component.translatable(variables.getVariables()[2].getType().getTranslationKey())));
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
            .output(ValueTypes.BOOLEAN).symbolOperatorInteract("contains")
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
            .output(ValueTypes.BOOLEAN).symbolOperator("contains_p").interactName("containsPredicate")
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
            .symbolOperatorInteract("count")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                    if (list.isInfinite()) {
                        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_INFINITELIST_ILLEGAL,
                                LIST_COUNT.getLocalizedNameFull()));
                    }
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
            .symbolOperator("count_p").interactName("countPredicate")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                    IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                    if (list.isInfinite()) {
                        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_INFINITELIST_ILLEGAL,
                                LIST_COUNT_PREDICATE.getLocalizedNameFull()));
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
                }
            }).build());

    /**
     * Append an element to the given list
     */
    public static final IOperator LIST_APPEND = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.CATEGORY_ANY})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperatorInteract("append")
            .function(variables -> {
                ValueTypeList.ValueList valueList = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList.getRawValue();
                IValue value = variables.getValue(1);
                if (!ValueHelpers.correspondsTo(a.getValueType(), value.getType())) {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            Component.translatable(a.getValueType().getTranslationKey()),
                            Component.translatable(value.getType().getTranslationKey())));
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyAppend(a, value));
            }).build());

    /**
     * Concatenate two lists
     */
    public static final IOperator LIST_CONCAT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.LIST, ValueTypes.LIST})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperatorInteract("concat")
            .function(variables -> {
                ValueTypeList.ValueList valueList0 = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = valueList0.getRawValue();
                ValueTypeList.ValueList valueList1 = variables.getValue(1, ValueTypes.LIST);
                IValueTypeListProxy b = valueList1.getRawValue();
                if (!ValueHelpers.correspondsTo(a.getValueType(), b.getValueType())) {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            Component.translatable(a.getValueType().getTranslationKey()),
                            Component.translatable(b.getValueType().getTranslationKey())));
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyConcat(a, b));
            }).build());

    /**
     * Build a list lazily using a start value and an operator that is applied to the previous element to get a next element.
     */
    public static final IOperator LIST_LAZYBUILT = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(new IValueType[]{ValueTypes.CATEGORY_ANY, ValueTypes.OPERATOR})
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbolOperator("lazybuilt").interactName("lazyBuilt")
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
            .renderPattern(IConfigRenderPattern.PREFIX_1_LONG).symbolOperatorInteract("head")
            .function(variables -> {
                ValueTypeList.ValueList list = variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy a = list.getRawValue();
                if (a.getLength() > 0) {
                    return a.get(0);
                } else {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_INDEXOUTOFBOUNDS, 0, a.getLength()));
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
            .symbolOperatorInteract("tail")
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
            .symbolOperator("uniq_p").interactName("uniquePredicate")
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
            .symbolOperator("uniq").interactName("unique")
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
            .symbolOperatorInteract("slice")
            .function(variables -> {
                ValueTypeList.ValueList valueList =variables.getValue(0, ValueTypes.LIST);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = valueList.getRawValue();
                ValueTypeInteger.ValueInteger from = variables.getValue(1, ValueTypes.INTEGER);
                ValueTypeInteger.ValueInteger to = variables.getValue(2, ValueTypes.INTEGER);
                if (from.getRawValue() >= to.getRawValue()) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_SLICE_TOGREATERTHANFROM));
                }
                if (from.getRawValue() < 0 || to.getRawValue() < 0){
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_SLICE_INDEXNEGATIVE));
                }
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxySlice<>(list, from.getRawValue(), to.getRawValue()));
            }).build());

    public static final IOperator LIST_INTERSECTION = REGISTRY.register(OperatorBuilders.LIST
            .inputTypes(ValueTypes.LIST, ValueTypes.LIST)
            .renderPattern(IConfigRenderPattern.INFIX).output(ValueTypes.LIST)
            .symbol("∩").operatorInteract("intersection")
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    IValueTypeListProxy<IValueType<IValue>, IValue> rawList1 = variables.getValue(0, ValueTypes.LIST).getRawValue();
                    IValueTypeListProxy<IValueType<IValue>, IValue> rawList2 = variables.getValue(1, ValueTypes.LIST).getRawValue();
                    if (rawList1.isInfinite() || rawList2.isInfinite()) {
                        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_INFINITELIST_ILLEGAL,
                                LIST_INTERSECTION.getLocalizedNameFull()));
                    }
                    LinkedHashSet<IValue> result = Sets.newLinkedHashSet(rawList1);
                    result.retainAll(Sets.newLinkedHashSet(rawList2));

                    return ValueTypeList.ValueList.ofList(rawList1.getValueType(), result.stream().toList());
                }
            }).build());

    /**
     * ----------------------------------- BLOCK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Block isOpaque operator with one input block and one output boolean.
     */
    public static final IOperator OBJECT_BLOCK_OPAQUE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN).symbolOperator("opaque").interactName("isOpaque")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent() && a.getRawValue().get().isSolidRender(null, null));
            }).build());

    /**
     * The itemstack representation of the block
     */
    public static final IOperator OBJECT_BLOCK_ITEMSTACK = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK).symbolOperator("itemstack").interactName("itemStack")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueObjectTypeItemStack.ValueItemStack.of(a.getRawValue().isPresent() ? BlockHelpers.getItemStackFromBlockState(a.getRawValue().get()) : ItemStack.EMPTY);
            }).build());

    /**
     * The name of the mod owning this block
     */
    public static final IOperator OBJECT_BLOCK_MODNAME = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING).symbolOperatorInteract("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                        return a.getRawValue().isPresent() ? ForgeRegistries.BLOCKS.getKey(a.getRawValue().get().getBlock()) : new ResourceLocation("");
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The breaksound of the block
     */
    public static final IOperator OBJECT_BLOCK_BREAKSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("break_sound").operatorName("breaksound").interactName("breakSound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? sound.get().getBreakSound().location.toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The placesound of the block
     */
    public static final IOperator OBJECT_BLOCK_PLACESOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("place_sound").operatorName("placesound").interactName("placeSound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? sound.get().getPlaceSound().location.toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());
    /**
     * The stepsound of the block
     */
    public static final IOperator OBJECT_BLOCK_STEPSOUND = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("step_sound").operatorName("stepsound").interactName("stepSound")
            .function(new IterativeFunction(Lists.newArrayList(
                    OperatorBuilders.BLOCK_SOUND,
                    (Optional<SoundType> sound) -> sound.isPresent() ? sound.get().getStepSound().location.toString() : "",
                    OperatorBuilders.PROPAGATOR_STRING_VALUE
            ))).build());

    /**
     * If the block is shearable
     */
    public static final IOperator OBJECT_BLOCK_ISSHEARABLE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_shearable").operatorName("isshearable").interactName("isShearable")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get().getBlock() instanceof IForgeShearable
                        && ((IForgeShearable) a.getRawValue().get().getBlock()).isShearable(ItemStack.EMPTY, null, null));
            }).build());

    /**
     * If the block is plantable
     */
    public static final IOperator OBJECT_BLOCK_ISPLANTABLE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_plantable").operatorName("isplantable").interactName("isPlantable")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get().getBlock() instanceof IPlantable);
            }).build());

    /**
     * The block plant type
     */
    public static final IOperator OBJECT_BLOCK_PLANTTYPE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbol("plant_type").operatorName("planttype").interactName("plantType")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                String type = "none";
                if (a.getRawValue().isPresent() && a.getRawValue().get().getBlock() instanceof IPlantable) {
                    type = ((IPlantable) a.getRawValue().get().getBlock()).getPlantType(null, null).getName();
                }
                return ValueTypeString.ValueString.of(type);
            }).build());

    /**
     * The block when this block is planted
     */
    public static final IOperator OBJECT_BLOCK_PLANT = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperator("plant").interactName("plant")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                BlockState plant = null;
                if (a.getRawValue().isPresent() && a.getRawValue().get().getBlock() instanceof IPlantable) {
                    plant = ((IPlantable) a.getRawValue().get().getBlock()).getPlant(null, null);
                }
                return ValueObjectTypeBlock.ValueBlock.of(plant);
            }).build());

    /**
     * The block when this block is planted
     */
    public static final IOperator OBJECT_BLOCK_PLANTAGE = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("plant_age").operatorName("plantage").interactName("plantAge")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                int age = 0;
                if (a.getRawValue().isPresent()) {
                    for (Property<?> prop : a.getRawValue().get().getProperties()) {
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
            .symbol("block_by_name").operatorName("blockbyname").interactName("blockByName")
            .function(OperatorBuilders.FUNCTION_STRING_TO_RESOURCE_LOCATION
                    .build(input -> {
                        Block block = ForgeRegistries.BLOCKS.getValue(input);
                        return ValueObjectTypeBlock.ValueBlock.of(block.defaultBlockState());
                    })).build());

    /**
     * Get the block properties as NBT compound tag.
     */
    public static final IOperator OBJECT_BLOCK_PROPERTIES = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.NBT)
            .symbol("block_props").operatorName("blockproperties").interactName("properties")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeNbt.ValueNbt.of(a.getRawValue().map(blockState -> {
                    CompoundTag tag = new CompoundTag();
                    for (Property property : blockState.getProperties()) {
                        Comparable<?> value = blockState.getValue(property);
                        tag.putString(property.getName(), property.getName(value));
                    }
                    return tag;
                }));
            }).build());

    /**
     * Get the given block applied with the given properties.
     */
    public static final IOperator OBJECT_BLOCK_WITH_PROPERTIES = REGISTRY.register(OperatorBuilders.BLOCK_INFIX_VERYLONG
            .inputTypes(ValueTypes.OBJECT_BLOCK, ValueTypes.NBT).output(ValueTypes.OBJECT_BLOCK)
            .symbol("block_with_props").operatorName("blockfromproperties").interactName("withProperties")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                ValueTypeNbt.ValueNbt b = variables.getValue(1, ValueTypes.NBT);
                if (a.getRawValue().isPresent() && a.getRawValue().isPresent()) {
                    BlockState blockState = a.getRawValue().get();
                    Tag tagRaw = b.getRawValue().get();
                    if (tagRaw instanceof CompoundTag) {
                        CompoundTag tag = (CompoundTag) tagRaw;
                        for (Property property : blockState.getProperties()) {
                            if (tag.contains(property.getName(), Tag.TAG_STRING)) {
                                Optional<Comparable> valueOptional = property.getValue(tag.getString(property.getName()));
                                if (valueOptional.isPresent()) {
                                    blockState = blockState.setValue(property, valueOptional.get());
                                }
                            }
                        }
                        return ValueObjectTypeBlock.ValueBlock.of(blockState);
                    }
                }
                return a;
            }).build());

    /**
     * Get all possible block properties as NBT compound tag with list values.
     */
    public static final IOperator OBJECT_BLOCK_POSSIBLE_PROPERTIES = REGISTRY.register(OperatorBuilders.BLOCK_1_SUFFIX_LONG
            .output(ValueTypes.NBT)
            .symbol("block_all_props").operatorName("blockpossibleproperties").interactName("possibleProperties")
            .function(variables -> {
                ValueObjectTypeBlock.ValueBlock a = variables.getValue(0, ValueTypes.OBJECT_BLOCK);
                return ValueTypeNbt.ValueNbt.of(a.getRawValue().map(blockState -> {
                    CompoundTag tag = new CompoundTag();
                    for (Property property : blockState.getProperties()) {
                        ListTag list = new ListTag();
                        for (Comparable value : (Collection<Comparable>) property.getPossibleValues()) {
                            list.add(StringTag.valueOf(property.getName(value)));
                        }
                        tag.put(property.getName(), list);
                    }
                    return tag;
                }));
            }).build());

    /**
     * ----------------------------------- ITEM STACK OBJECT OPERATORS -----------------------------------
     */

    /**
     * Item Stack size operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_SIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("size").interactName("size")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getCount() : 0
            )).build());

    /**
     * Item Stack maxsize operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("maxsize").interactName("maxSize")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getMaxStackSize() : 0
            )).build());

    /**
     * Item Stack isstackable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISSTACKABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("stackable").interactName("isStackable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isStackable()
            )).build());

    /**
     * Item Stack isdamageable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISDAMAGEABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("damageable").interactName("isDamageable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isDamageableItem()
            )).build());

    /**
     * Item Stack damage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_DAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("damage").interactName("damage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getDamageValue() : 0
            )).build());

    /**
     * Item Stack maxdamage operator with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_MAXDAMAGE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("max_damage").operatorName("maxdamage").interactName("maxDamage")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getMaxDamage() : 0
            )).build());

    /**
     * Item Stack isenchanted operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTED = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchanted").interactName("isEnchanted")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isEnchanted()
            )).build());

    /**
     * Item Stack isenchantable operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISENCHANTABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("enchantable").interactName("isEnchantable")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && itemStack.isEnchantable()
            )).build());

    /**
     * Item Stack repair cost with one input itemstack and one output integer.
     */
    public static final IOperator OBJECT_ITEMSTACK_REPAIRCOST = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("repair_cost").operatorName("repaircost").interactName("repairCost")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? itemStack.getBaseRepairCost() : 0
            )).build());

    /**
     * Get the rarity of an itemstack.
     */
    public static final IOperator OBJECT_ITEMSTACK_RARITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperatorInteract("rarity")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeString.ValueString.of(!a.getRawValue().isEmpty() ? a.getRawValue().getRarity().name() : "");
            }).build());

    /**
     * Get the strength of an itemstack against a block as a double.
     */
    public static final IOperator OBJECT_ITEMSTACK_STRENGTH_VS_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .inputTypes(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK, ValueTypes.OBJECT_BLOCK}).output(ValueTypes.DOUBLE)
            .symbolOperatorInteract("strength")
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
            .symbol("can_harvest").operatorName("canharvest").interactName("canHarvest")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeBlock.ValueBlock b = variables.getValue(1, ValueTypes.OBJECT_BLOCK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty() && b.getRawValue().isPresent() && a.getRawValue().isCorrectToolForDrops(b.getRawValue().get()));
            }).build());

    /**
     * The block from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_BLOCK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperatorInteract("block")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueObjectTypeBlock.ValueBlock.of((!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof BlockItem) ? BlockHelpers.getBlockStateFromItemStack(a.getRawValue()) : null);
            }).build());

    /**
     * If the given stack has a fluid.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISFLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("is_fluidstack").operatorName("isfluidstack").interactName("isFluidStack")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                itemStack -> !itemStack.isEmpty() && !Helpers.getFluidStack(itemStack).isEmpty()
            )).build());

    /**
     * The fluidstack from the stack
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACK = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_FLUIDSTACK).symbolOperator("fluidstack").interactName("fluidStack")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueObjectTypeFluidStack.ValueFluidStack.of(!a.getRawValue().isEmpty() ? Helpers.getFluidStack(a.getRawValue()) : null);
            }).build());

    /**
     * The capacity of the fluidstack from the stack.
     */
    public static final IOperator OBJECT_ITEMSTACK_FLUIDSTACKCAPACITY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("fluidstack_capacity").operatorName("fluidstackcapacity").interactName("fluidCapacity")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(
                itemStack -> !itemStack.isEmpty() ? Helpers.getFluidStackCapacity(itemStack) : 0
            )).build());

    /**
     * If the NBT tags of the given stacks are equal.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISNBTEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NBT=").operatorName("isnbtequal").interactName("isNbtEqual")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack valueStack0 = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ValueObjectTypeItemStack.ValueItemStack valueStack1 = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                ItemStack a = valueStack0.getRawValue();
                ItemStack b = valueStack1.getRawValue();
                boolean equal = false;
                if(!a.isEmpty() && !b.isEmpty()) {
                    equal = a.sameItem(b) && ItemMatch.areItemStacksEqual(a, b, ItemMatch.TAG);
                } else if(a.isEmpty() && b.isEmpty()) {
                    equal = true;
                }
                return ValueTypeBoolean.ValueBoolean.of(equal);
            }).build());

    /**
     * If the raw items of the given stacks are equal, ignoring NBT but including damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISITEMEQUALNONBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=NoNBT=").operatorName("isitemequalnonbt").interactName("isEqualNonNbt")
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
     * If the raw items of the given stacks are equal, ignoring NBT and damage value.
     */
    public static final IOperator OBJECT_ITEMSTACK_ISRAWITEMEQUAL = REGISTRY.register(OperatorBuilders.ITEMSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwitemequal").interactName("isEqualRaw")
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
    public static final IOperator OBJECT_ITEMSTACK_MODNAME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbolOperatorInteract("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                        return !a.getRawValue().isEmpty() ? ForgeRegistries.ITEMS.getKey(a.getRawValue().getItem()) : new ResourceLocation("");
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The fuel burn time of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_FUELBURNTIME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("burn_time").operatorName("burntime").interactName("burnTime")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_INT.build(itemStack -> {
                if (!itemStack.isEmpty()) {
                    int burnTime = itemStack.getBurnTime(null);
                    return ForgeEventFactory.getItemBurnTime(itemStack, burnTime == -1
                            ? ForgeHooks.getBurnTime(itemStack, null)
                            : burnTime, null);
                }
                return 0;
            })).build());

    /**
     * If the given item can be used as fuel
     */
    public static final IOperator OBJECT_ITEMSTACK_CANBURN = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("can_burn").operatorName("canburn").interactName("canBurn")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN
                    .build(AbstractFurnaceBlockEntity::isFuel))
            .build());

    /**
     * The tag entries of the given item
     */
    public static final IOperator OBJECT_ITEMSTACK_TAG = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST)
            .symbol("tag_names").operatorName("tag").interactName("tags")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                ImmutableList.Builder<ValueTypeString.ValueString> builder = ImmutableList.builder();
                if(!a.getRawValue().isEmpty()) {
                    Optional<IReverseTag<Item>> optionalReverseTag = ForgeRegistries.ITEMS.tags().getReverseTag(a.getRawValue().getItem());
                    optionalReverseTag
                            .ifPresent(reverseTag -> reverseTag.getTagKeys()
                                    .forEach(owningTag -> builder.add(ValueTypeString.ValueString
                                            .of(owningTag.location().toString()))));
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.STRING, builder.build());
            }).build());

    /**
     * Get a list of items that correspond to the given tag key.
     */
    public static final IOperator OBJECT_ITEMSTACK_TAG_STACKS = REGISTRY.register(OperatorBuilders.STRING_1_PREFIX
            .output(ValueTypes.LIST)
            .symbol("tag_values").operatorName("tag").interactName("itemsByTag")
            .inputType(ValueTypes.STRING).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG)
            .function(variables -> {
                ValueTypeString.ValueString a = variables.getValue(0, ValueTypes.STRING);
                ImmutableList.Builder<ValueObjectTypeItemStack.ValueItemStack> builder = ImmutableList.builder();
                if (!StringUtil.isNullOrEmpty(a.getRawValue())) {
                    try {
                        Helpers.getTagValues(a.getRawValue())
                                .map(ValueObjectTypeItemStack.ValueItemStack::of)
                                .forEach(builder::add);
                    } catch (ResourceLocationException e) {
                        throw new EvaluationException(Component.translatable(e.getMessage()));
                    }
                }
                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, builder.build());
            }).build());

    /**
     * ItemStack operator that applies the given stacksize to the given itemstack and creates a new ItemStack.
     */
    public static final IOperator OBJECT_ITEMSTACK_WITHSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_INTEGER_1
            .output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("with_size").operatorName("withsize").interactName("withSize")
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
            .symbol("is_fe_container").operatorName("isfecontainer").interactName("isFeContainer")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_BOOLEAN.build(
                Objects::nonNull
            )).build());

    /**
     * Get the storage energy
     */
    public static final IOperator OBJECT_ITEMSTACK_STOREDFE = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("stored_fe").operatorName("storedfe").interactName("feStored")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(
                input -> input != null ? input.getEnergyStored() : 0
            )).build());

    /**
     * Get the energy capacity
     */
    public static final IOperator OBJECT_ITEMSTACK_FECAPACITY = Operators.REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("capacity_fe").operatorName("fecapacity").interactName("feCapacity")
            .function(OperatorBuilders.FUNCTION_CONTAINERITEM_TO_INT.build(
                input -> input != null ? input.getMaxEnergyStored() : 0
            )).build());


    /**
     * If the given item has an inventory.
     */
    public static final IOperator OBJECT_ITEMSTACK_HASINVENTORY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("has_inventory").operatorName("hasinventory").interactName("hasInventory")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty() && a.getRawValue().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent());
            }).build());

    /**
     * If the item is plantable
     */
    public static final IOperator OBJECT_ITEMSTACK_ISPLANTABLE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN)
            .symbol("is_plantable").operatorName("isplantable").interactName("isPlantable")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeBoolean.ValueBoolean.of(!a.getRawValue().isEmpty()
                        && a.getRawValue().getItem() instanceof BlockItem
                        && ((BlockItem) a.getRawValue().getItem()).getBlock() instanceof IPlantable);
            }).build());




    /**
     * Retrieve the inventory size of the given item handler contents.
     */
    public static final IOperator OBJECT_ITEMSTACK_INVENTORYSIZE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER)
            .symbol("inventory_size").operatorName("inventorysize").interactName("inventorySize")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return ValueTypeInteger.ValueInteger.of(a.getRawValue().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .map(IItemHandler::getSlots)
                        .orElse(0));
            }).build());

    /**
     * The item plant type
     */
    public static final IOperator OBJECT_ITEMSTACK_PLANTTYPE = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING)
            .symbol("plant_type").operatorName("planttype").interactName("plantType")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                String type = "none";
                if (!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof BlockItem
                        && ((BlockItem) a.getRawValue().getItem()).getBlock() instanceof IPlantable) {
                    type = ((IPlantable) ((BlockItem) a.getRawValue().getItem()).getBlock()).getPlantType(null, null).getName();
                }
                return ValueTypeString.ValueString.of(type);
            }).build());

    /**
     * Retrieve the inventory of the given item handler contents.
     */
    public static final IOperator OBJECT_ITEMSTACK_INVENTORY = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("inventory").interactName("inventory")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                return a.getRawValue().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                        .map(itemHandler -> {
                            List<ValueObjectTypeItemStack.ValueItemStack> values = Lists.newArrayListWithCapacity(itemHandler.getSlots());
                            for (int i = 0; i < itemHandler.getSlots(); i++) {
                                values.add(ValueObjectTypeItemStack.ValueItemStack.of(itemHandler.getStackInSlot(i)));
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, values);
                        })
                        .orElseGet(() -> ValueTypes.LIST.getDefault());
            }).build());

    /**
     * The item when this item is planted
     */
    public static final IOperator OBJECT_ITEMSTACK_PLANT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperatorInteract("plant")
            .function(variables -> {
                ValueObjectTypeItemStack.ValueItemStack a = variables.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                BlockState plant = null;
                if (!a.getRawValue().isEmpty() && a.getRawValue().getItem() instanceof BlockItem
                        && ((BlockItem) a.getRawValue().getItem()).getBlock() instanceof IPlantable) {
                    plant = ((BlockItem) a.getRawValue().getItem()).getBlock().defaultBlockState();
                }
                return ValueObjectTypeBlock.ValueBlock.of(plant);
            }).build());

    /**
     * Get an item by name.
     */
    public static final IOperator OBJECT_ITEMSTACK_BY_NAME = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_PREFIX_LONG
            .inputType(ValueTypes.STRING).output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("item_by_name").operatorName("itembyname").interactName("itemByName")
            .function(OperatorBuilders.FUNCTION_STRING_TO_RESOURCE_LOCATION
                    .build(input -> {
                        Item item = ForgeRegistries.ITEMS.getValue(input);
                        ItemStack itemStack = ItemStack.EMPTY;
                        if (item != null) {
                            itemStack = new ItemStack(item);
                        }
                        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                    })).build());

    /**
     * Get the total item count of the given item in a list.
     */
    public static final IOperator OBJECT_ITEMSTACK_LIST_COUNT = REGISTRY.register(OperatorBuilders.ITEMSTACK_2_LONG
            .inputTypes(ValueTypes.LIST, ValueTypes.OBJECT_ITEMSTACK)
            .output(ValueTypes.INTEGER)
            .symbol("item_list_count").operatorName("itemlistcount").interactName("itemListCount")
            .function(variables -> {
                ValueTypeList.ValueList<IValueType<IValue>, IValue> a = variables.getValue(0, ValueTypes.LIST);
                ValueObjectTypeItemStack.ValueItemStack b = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                if (!ValueHelpers.correspondsTo(a.getRawValue().getValueType(), ValueTypes.OBJECT_ITEMSTACK)) {
                    MutableComponent error = Component.translatable(
                            L10NValues.VALUETYPE_ERROR_INVALIDLISTVALUETYPE,
                            Component.translatable(a.getRawValue().getValueType().getTranslationKey()),
                            Component.translatable(ValueTypes.OBJECT_ITEMSTACK.getTranslationKey()));
                    throw new EvaluationException(error);
                }

                ItemStack itemStack = b.getRawValue();
                int count = 0;
                for (IValue listValueRaw : a.getRawValue()) {
                    if (listValueRaw.getType().correspondsTo(ValueTypes.OBJECT_ITEMSTACK)) {
                        ValueObjectTypeItemStack.ValueItemStack listValue = (ValueObjectTypeItemStack.ValueItemStack) listValueRaw;
                        if (!listValue.getRawValue().isEmpty()) {
                            ItemStack listItem = listValue.getRawValue();
                            if (!itemStack.isEmpty()) {
                                if (itemStack.sameItem(listItem) && ItemStack.tagMatches(itemStack, listItem)) {
                                    count += listItem.getCount();
                                }
                            } else {
                                count += listItem.getCount();
                            }
                        }
                    }
                }

                return ValueTypeInteger.ValueInteger.of(count);
            }).build());

    /**
     * Item Stack size operator with one input itemstack and one output NBT tag.
     */
    public static final IOperator OBJECT_ITEMSTACK_NBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorName("nbt").interactName("nbt")
            .function(input -> {
                ValueObjectTypeItemStack.ValueItemStack itemStack = input.getValue(0, ValueTypes.OBJECT_ITEMSTACK);
                // Explicitly check for item emptiness first, because vanilla sometimes persists NBT when setting stacks to empty
                return ValueTypeNbt.ValueNbt.of(itemStack.getRawValue().isEmpty() ? new CompoundTag() : itemStack.getRawValue().getTag());
            }).build());

    /**
     * Item Stack has_nbt operator with one input itemstack and one output boolean.
     */
    public static final IOperator OBJECT_ITEMSTACK_HASNBT = REGISTRY.register(OperatorBuilders.ITEMSTACK_1_PREFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("has_nbt").operatorName("hasnbt").interactName("hasNbt")
            .function(OperatorBuilders.FUNCTION_ITEMSTACK_TO_BOOLEAN.build(
                    itemStack -> !itemStack.isEmpty() && itemStack.hasTag()
            )).build());

    /**
     * ----------------------------------- ENTITY OBJECT OPERATORS -----------------------------------
     */

    /**
     * If the entity is a mob
     */
    public static final IOperator OBJECT_ENTITY_ISMOB = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_mob").operatorName("ismob").interactName("isMob")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof Enemy
            )).build());

    /**
     * If the entity is an animal
     */
    public static final IOperator OBJECT_ENTITY_ISANIMAL = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_animal").operatorName("isanimal").interactName("isAnimal")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof Animal && !(entity instanceof Enemy)
            )).build());

    /**
     * If the entity is an item
     */
    public static final IOperator OBJECT_ENTITY_ISITEM = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_item").operatorName("isitem").interactName("isItem")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof ItemEntity
            )).build());

    /**
     * If the entity is a player
     */
    public static final IOperator OBJECT_ENTITY_ISPLAYER = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_player").operatorName("isplayer").interactName("isPlayer")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof Player
            )).build());

    /**
     * If the entity is a minecart
     */
    public static final IOperator OBJECT_ENTITY_ISMINECART = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_minecart").operatorName("isminecart").interactName("isMinecart")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof AbstractMinecart
            )).build());

    /**
     * The itemstack from the entity
     */
    public static final IOperator OBJECT_ENTITY_ITEMSTACK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX
            .output(ValueTypes.OBJECT_ITEMSTACK).symbolOperatorInteract("item")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                return ValueObjectTypeItemStack.ValueItemStack.of((a.isPresent() && a.get() instanceof ItemEntity) ? ((ItemEntity) a.get()).getItem() : ItemStack.EMPTY);
            }).build());

    /**
     * The entity health
     */
    public static final IOperator OBJECT_ENTITY_HEALTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperatorInteract("health")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 0.0
            )).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_WIDTH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperatorInteract("width")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity != null ? entity.getBbWidth() : 0.0
            )).build());

    /**
     * The entity width
     */
    public static final IOperator OBJECT_ENTITY_HEIGHT = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).symbolOperatorInteract("height")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_DOUBLE.build(
                entity -> entity != null ? entity.getBbHeight() : 0.0
            )).build());

    /**
     * If the entity is burning
     */
    public static final IOperator OBJECT_ENTITY_ISBURNING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_burning").operatorName("isburning").interactName("entityIsBurning")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isOnFire()
            )).build());

    /**
     * If the entity is wet
     */
    public static final IOperator OBJECT_ENTITY_ISWET = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_wet").operatorName("iswet").interactName("isWet")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isInWaterOrRain()
            )).build());

    /**
     * If the entity is crouching
     */
    public static final IOperator OBJECT_ENTITY_ISCROUCHING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_crouching").operatorName("iscrouching").interactName("isCrouching")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity != null && entity.isCrouching()
            )).build());

    /**
     * If the entity is eating
     */
    public static final IOperator OBJECT_ENTITY_ISEATING = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbol("is_eating").operatorName("iseating").interactName("isEating")
            .function(OperatorBuilders.FUNCTION_ENTITY_TO_BOOLEAN.build(
                entity -> entity instanceof LivingEntity && ((LivingEntity) entity).getUseItemRemainingTicks() > 0
            )).build());

    /**
     * The list of armor itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_ARMORINVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("armor_inventory").operatorName("armorinventory").interactName("armorInventory")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityArmorInventory(entity.level, entity));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                }
            }).build());

    /**
     * The list of itemstacks from an entity
     */
    public static final IOperator OBJECT_ENTITY_INVENTORY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbolOperator("inventory").interactName("inventory")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityInventory(entity.level, entity));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                }
            }).build());

    /**
     * The name of the mod owning this entity
     */
    public static final IOperator OBJECT_ENTITY_MODNAME = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbolOperatorInteract("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                        if(a.getRawValue().isPresent()) {
                            Entity entity = a.getRawValue().get();
                            return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                        }
                        return new ResourceLocation("");
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            )))
            .build());

    /**
     * The block the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETBLOCK = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_BLOCK)
            .symbol("target_block").operatorName("targetblock").interactName("targetBlock")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                BlockState blockState = null;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    LivingEntity entity = (LivingEntity) a.getRawValue().get();
                    AttributeInstance reachDistanceAttribute = entity.getAttribute(ForgeMod.REACH_DISTANCE.get());
                    double reachDistance = reachDistanceAttribute == null ? 5 : reachDistanceAttribute.getValue();
                    double eyeHeight = entity.getEyeHeight();
                    Vec3 lookVec = entity.getLookAngle();
                    Vec3 origin = new Vec3(entity.getX(), entity.getY() + eyeHeight, entity.getZ());
                    Vec3 direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

                    ClipContext rayTraceContext = new ClipContext(origin, direction, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity);
                    BlockHitResult mop = entity.level.clip(rayTraceContext);
                    if(mop != null && mop.getType() == HitResult.Type.BLOCK) {
                        blockState = entity.level.getBlockState(mop.getBlockPos());
                    }
                }
                return ValueObjectTypeBlock.ValueBlock.of(blockState);
            }).build());

    /**
     * The entity the given player is currently looking at.
     */
    public static final IOperator OBJECT_PLAYER_TARGETENTITY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ENTITY)
            .symbol("target_entity").operatorName("targetentity").interactName("targetEntity")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Entity entityOut = null;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    LivingEntity entity = (LivingEntity) a.getRawValue().get();
                    AttributeInstance reachDistanceAttribute = entity.getAttribute(ForgeMod.REACH_DISTANCE.get());
                    double reachDistance = reachDistanceAttribute == null ? 5 : reachDistanceAttribute.getValue();

                    // Copied and modified from GameRenderer#getMouseOver
                    Vec3 origin = entity.getEyePosition(1.0F);
                    double reachDistanceSquared = reachDistance * reachDistance;

                    Vec3 lookVec = entity.getViewVector(1.0F);
                    Vec3 direction = origin.add(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);
                    AABB boundingBox = entity.getBoundingBox()
                            .expandTowards(lookVec.scale(reachDistance))
                            .inflate(1.0D, 1.0D, 1.0D);
                    EntityHitResult entityraytraceresult = ProjectileUtil.getEntityHitResult(entity, origin, direction,
                            boundingBox, e -> !e.isSpectator() && e.isPickable(), reachDistanceSquared);
                    if (entityraytraceresult != null) {
                        Entity entity1 = entityraytraceresult.getEntity();
                        Vec3 vec3d3 = entityraytraceresult.getLocation();
                        double distanceSquared = origin.distanceToSqr(vec3d3);
                        if (distanceSquared < reachDistanceSquared
                                && (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame)) {
                            entityOut = entity1;
                        }
                    }
                }
                return ValueObjectTypeEntity.ValueEntity.of(entityOut);
            }).build());

    /**
     * If the given player has an external gui open.
     */
    public static final IOperator OBJECT_PLAYER_HASGUIOPEN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("has_gui_open").operatorName("hasguiopen").interactName("hasGuiOpen")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof Player) {
                    Player entity = (Player) a.getRawValue().get();
                    return ValueTypeBoolean.ValueBoolean.of(entity.containerMenu != entity.inventoryMenu);
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
            }).build());

    /**
     * The item the given entity is currently holding in its main hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_MAIN = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("held_item_1").operatorName("helditem").interactName("heldItem")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    itemStack = ((LivingEntity) a.getRawValue().get()).getMainHandItem();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The item the given entity is currently holding in its off hand.
     */
    public static final IOperator OBJECT_ENTITY_HELDITEM_OFF = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.OBJECT_ITEMSTACK)
            .symbol("held_item_2").operatorName("helditemoffhand").interactName("heldItemOffHand")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    itemStack = ((LivingEntity) a.getRawValue().get()).getOffhandItem();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The entity's mounted entity
     */
    public static final IOperator OBJECT_ENTITY_MOUNTED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.LIST)
            .symbolOperator("mounted").interactName("mounted")
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
            .symbol("itemframe_contents").operatorName("itemframecontents").interactName("itemFrameContents")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ItemStack itemStack = ItemStack.EMPTY;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof ItemFrame) {
                    itemStack = ((ItemFrame) a.getRawValue().get()).getItem();
                }
                return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
            }).build());

    /**
     * The item frame's rotation
     */
    public static final IOperator OBJECT_ITEMFRAME_ROTATION = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER)
            .symbol("itemframe_rotation").operatorName("itemframerotation").interactName("itemFrameRotation")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                Integer rotation = 0;
                if(a.getRawValue().isPresent() && a.getRawValue().get() instanceof ItemFrame) {
                    rotation = ((ItemFrame) a.getRawValue().get()).getRotation();
                }
                return ValueTypeInteger.ValueInteger.of(rotation);
            }).build());

    /**
     * The hurtsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_HURTSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbolOperator("hurtsound").interactName("hurtSound")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                String hurtSound = "";
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    String sound = ((LivingEntity) a.getRawValue().get()).getHurtSound(DamageSource.GENERIC).location.toString();
                    if (sound != null) {
                        hurtSound = sound;
                    }
                }
                return ValueTypeString.ValueString.of(hurtSound);
            }).build());

    /**
     * The deathsound of this entity.
     */
    public static final IOperator OBJECT_ENTITY_DEATHSOUND = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbolOperator("deathsound").interactName("deathSound")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                String hurtSound = "";
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    String sound = ((LivingEntity) a.getRawValue().get()).getDeathSound().location.toString();
                    if (sound != null) {
                        hurtSound = sound;
                    }
                }
                return ValueTypeString.ValueString.of(hurtSound);
            }).build());

    /**
     * The age of this entity.
     */
    public static final IOperator OBJECT_ENTITY_AGE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.INTEGER)
            .symbolOperatorInteract("age")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                int age = 0;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    age = ((LivingEntity) a.getRawValue().get()).getNoActionTime();
                }
                return ValueTypeInteger.ValueInteger.of(age);
            }).build());

    /**
     * If the entity is a child.
     */
    public static final IOperator OBJECT_ENTITY_ISCHILD = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_child").operatorName("ischild").interactName("isChild")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean child = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof LivingEntity) {
                    child = ((LivingEntity) a.getRawValue().get()).isBaby();
                }
                return ValueTypeBoolean.ValueBoolean.of(child);
            }).build());

    /**
     * If the entity can be bred.
     */
    public static final IOperator OBJECT_ENTITY_CANBREED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("canbreed").operatorName("canbreed").interactName("canBreed")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean canBreed = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof AgeableMob) {
                    canBreed = ((AgeableMob) a.getRawValue().get()).getAge() == 0;
                }
                return ValueTypeBoolean.ValueBoolean.of(canBreed);
            }).build());

    /**
     * If the entity is in love.
     */
    public static final IOperator OBJECT_ENTITY_ISINLOVE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_in_love").operatorName("isinlove").interactName("isInLove")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                boolean inLove = false;
                if (a.getRawValue().isPresent() && a.getRawValue().get() instanceof Animal) {
                    inLove = ((Animal) a.getRawValue().get()).isInLove();
                }
                return ValueTypeBoolean.ValueBoolean.of(inLove);
            }).build());

    /**
     * If the entity can be bred with the given item.
     */
    public static final IOperator OBJECT_ENTITY_CANBREEDWITH = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .inputTypes(ValueTypes.OBJECT_ENTITY, ValueTypes.OBJECT_ITEMSTACK)
            .output(ValueTypes.BOOLEAN)
            .symbol("can_breed_with").operatorName("canbreedwith").interactName("canBreedWith")
            .renderPattern(IConfigRenderPattern.INFIX_LONG)
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                ValueObjectTypeItemStack.ValueItemStack b = variables.getValue(1, ValueTypes.OBJECT_ITEMSTACK);
                boolean canBreedWith = false;
                if (a.getRawValue().isPresent() && !b.getRawValue().isEmpty() && a.getRawValue().get() instanceof Animal) {
                    canBreedWith = ((Animal) a.getRawValue().get()).isFood(b.getRawValue());
                }
                return ValueTypeBoolean.ValueBoolean.of(canBreedWith);
            }).build());

    /**
     * If the entity is shearable.
     */
    public static final IOperator OBJECT_ENTITY_ISSHEARABLE = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG.output(ValueTypes.BOOLEAN)
            .symbol("is_shearable").operatorName("isshearable").interactName("isShearable")
            .function(variables -> {
                ValueObjectTypeEntity.ValueEntity a = variables.getValue(0, ValueTypes.OBJECT_ENTITY);
                return ValueTypeBoolean.ValueBoolean.of(a.getRawValue().isPresent()
                        && a.getRawValue().get() instanceof IForgeShearable
                        && ((IForgeShearable) a.getRawValue().get()).isShearable(ItemStack.EMPTY, null, null));
            }).build());

    /**
     * The entity serialized to NBT.
     */
    public static final IOperator OBJECT_ENTITY_NBT = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorInteract("nbt")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity entity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                try {
                    if (entity.getRawValue().isPresent()) {
                        return ValueTypeNbt.ValueNbt.of(entity.getRawValue().get().serializeNBT());
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
            .output(ValueTypes.STRING).symbol("entity_type").operatorName("entitytype").interactName("type")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity entity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                String entityType = "";
                if (entity.getRawValue().isPresent()) {
                    Entity e = entity.getRawValue().get();
                    entityType = ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString();
                }
                return ValueTypeString.ValueString.of(entityType);
            }).build());

    /**
     * The entity items.
     */
    public static final IOperator OBJECT_ENTITY_ITEMS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("entity_items").operatorName("entityitems").interactName("items")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityItems(entity.level, entity, null));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.emptyList());
                }
            }).build());

    /**
     * The entity fluids.
     */
    public static final IOperator OBJECT_ENTITY_FLUIDS = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.LIST).symbol("entity_fluids").operatorName("entityfluids").interactName("fluids")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyEntityFluids(entity.level, entity, null));
                } else {
                    return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_FLUIDSTACK, Collections.emptyList());
                }
            }).build());

    /**
     * The entity energy stored.
     */
    public static final IOperator OBJECT_ENTITY_ENERGY_STORED = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("entity_stored_fe").operatorName("entityenergystored").interactName("energy")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeInteger.ValueInteger.of(entity.getCapability(CapabilityEnergy.ENERGY, null)
                            .map(IEnergyStorage::getEnergyStored)
                            .orElse(0));
                }
                return ValueTypeInteger.ValueInteger.of(0);
            }).build());

    /**
     * The entity energy stored.
     */
    public static final IOperator OBJECT_ENTITY_ENERGY_CAPACITY = REGISTRY.register(OperatorBuilders.ENTITY_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbol("entity_capacity_fe").operatorName("entityenergycapacity").interactName("energyCapacity")
            .function(input -> {
                ValueObjectTypeEntity.ValueEntity valueEntity = input.getValue(0, ValueTypes.OBJECT_ENTITY);
                Optional<Entity> a = valueEntity.getRawValue();
                if(a.isPresent()) {
                    Entity entity = a.get();
                    return ValueTypeInteger.ValueInteger.of(entity.getCapability(CapabilityEnergy.ENERGY, null)
                            .map(IEnergyStorage::getMaxEnergyStored)
                            .orElse(0));
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
            .output(ValueTypes.INTEGER).symbolOperatorInteract("amount")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                    FluidStack::getAmount
            )).build());

    /**
     * The block from the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_BLOCK = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_BLOCK).symbolOperatorInteract("block")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueObjectTypeBlock.ValueBlock.of(!a.isEmpty() ? a.getFluid().getFluidType().getStateForPlacement(null, null, a).createLegacyBlock() : null);
            }).build());

    /**
     * The fluidstack luminosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_LIGHT_LEVEL = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperator("light_level").interactName("lightLevel")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack.getFluid().getFluidType().getLightLevel(fluidStack)
            )).build());

    /**
     * The fluidstack density
     */
    public static final IOperator OBJECT_FLUIDSTACK_DENSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperatorInteract("density")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack.getFluid().getFluidType().getDensity(fluidStack)
            )).build());

    /**
     * The fluidstack temperature
     */
    public static final IOperator OBJECT_FLUIDSTACK_TEMPERATURE = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperatorInteract("temperature")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                    fluidStack -> fluidStack.getFluid().getFluidType().getTemperature(fluidStack)
            )).build());

    /**
     * The fluidstack viscosity
     */
    public static final IOperator OBJECT_FLUIDSTACK_VISCOSITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).symbolOperatorInteract("viscosity")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_INT.build(
                fluidStack -> fluidStack.getFluid().getFluidType().getViscosity(fluidStack)
            )).build());

    /**
     * If the fluidstack is gaseous
     */
    public static final IOperator OBJECT_FLUIDSTACK_IS_LIGHTER_THAN_AIR = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).symbolOperator("lighter_than_air").interactName("isLighterThanAir")
            .function(OperatorBuilders.FUNCTION_FLUIDSTACK_TO_BOOLEAN.build(
                fluidStack -> fluidStack.getFluid().getFluidType().isLighterThanAir()
            )).build());

    /**
     * The rarity of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_RARITY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperatorInteract("rarity")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueTypeString.ValueString.of(a.getFluid().getFluidType().getRarity(a).name());
            }).build());

    /**
     * The bucket empty sound of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_SOUND_BUCKET_EMPTY = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("sound_bucket_empty").interactName("bucketEmptySound")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueTypeString.ValueString.of(Optional.ofNullable(a.getFluid().getFluidType().getSound(a, SoundActions.BUCKET_EMPTY))
                        .map(soundEvent -> soundEvent.getLocation().toString())
                        .orElse(""));
            }).build());

    /**
     * The fluid vaporize sound of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_SOUND_FLUID_VAPORIZE = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("sound_fluid_vaporize").interactName("fluidVaporizeSound")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueTypeString.ValueString.of(Optional.ofNullable(a.getFluid().getFluidType().getSound(a, SoundActions.FLUID_VAPORIZE))
                        .map(soundEvent -> soundEvent.getLocation().toString())
                        .orElse(""));
            }).build());

    /**
     * The bucket fill sound of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_SOUND_BUCKET_FILL = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.STRING).symbolOperator("sound_bucket_fill").interactName("bucketFillSound")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueTypeString.ValueString.of(Optional.ofNullable(a.getFluid().getFluidType().getSound(a, SoundActions.BUCKET_FILL))
                        .map(soundEvent -> soundEvent.getLocation().toString())
                        .orElse(""));
            }).build());

    /**
     * The bucket of the fluidstack
     */
    public static final IOperator OBJECT_FLUIDSTACK_BUCKET = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.OBJECT_ITEMSTACK).symbolOperatorInteract("bucket")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                FluidStack a = valueFluidStack.getRawValue();
                return ValueObjectTypeItemStack.ValueItemStack.of(a.getFluid().getFluidType().getBucket(a));
            }).build());

    /**
     * If the fluid types of the two given fluidstacks are equal
     */
    public static final IOperator OBJECT_FLUIDSTACK_ISRAWFLUIDEQUAL = REGISTRY.register(OperatorBuilders.FLUIDSTACK_2
            .output(ValueTypes.BOOLEAN).symbol("=Raw=").operatorName("israwfluidequal").interactName("isRawEqual")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack0 = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack1 = variables.getValue(1, ValueTypes.OBJECT_FLUIDSTACK);
                return ValueTypeBoolean.ValueBoolean.of(valueFluidStack0.getRawValue().isFluidEqual(valueFluidStack1.getRawValue()));
            }).build());

    /**
     * The name of the mod owning this fluid
     */
    public static final IOperator OBJECT_FLUIDSTACK_MODNAME = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG.output(ValueTypes.STRING)
            .symbolOperatorInteract("mod")
            .function(new IterativeFunction(Lists.newArrayList(
                    (OperatorBase.SafeVariablesGetter variables) -> {
                        ValueObjectTypeFluidStack.ValueFluidStack a = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                        return ForgeRegistries.FLUIDS.getKey(a.getRawValue().getFluid());
                    },
                    OperatorBuilders.PROPAGATOR_RESOURCELOCATION_MODNAME
            ))).build());

    /**
     * The tag of the given fluidstack.
     */
    public static final IOperator OBJECT_FLUIDSTACK_NBT = REGISTRY.register(OperatorBuilders.FLUIDSTACK_1_SUFFIX_LONG
            .output(ValueTypes.NBT).symbol("NBT()").operatorInteract("nbt")
            .function(input -> {
                ValueObjectTypeFluidStack.ValueFluidStack fluidStack = input.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                return ValueTypeNbt.ValueNbt.of(fluidStack.getRawValue().getTag());
            }).build());

    /**
     * Create a new fluidstack with the given amount.
     */
    public static final IOperator OBJECT_FLUIDSTACK_WITH_AMOUNT = REGISTRY.register(OperatorBuilders.FLUIDSTACK_2
            .inputTypes(ValueTypes.OBJECT_FLUIDSTACK, ValueTypes.INTEGER)
            .output(ValueTypes.OBJECT_FLUIDSTACK).symbolOperator("with_amount").interactName("withAmount")
            .function(variables -> {
                ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = variables.getValue(0, ValueTypes.OBJECT_FLUIDSTACK);
                ValueTypeInteger.ValueInteger valueInteger = variables.getValue(1, ValueTypes.INTEGER);
                FluidStack fluidStack = valueFluidStack.getRawValue().copy();
                fluidStack.setAmount(valueInteger.getRawValue());
                return ValueObjectTypeFluidStack.ValueFluidStack.of(fluidStack);
            }).build());

    /**
     * ----------------------------------- OPERATOR OPERATORS -----------------------------------
     */

    /**
     * Apply for a given operator a given value.
     */
    public static final IOperator OPERATOR_APPLY = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER)
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("apply")
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
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("apply2")
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
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("apply3")
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
     * Apply for a given operator the given list of values.
     */
    public static final IOperator OPERATOR_APPLY_N = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER_LIST)
            .inputTypes(ValueTypes.OPERATOR, ValueTypes.LIST)
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("apply_n")
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(ValueTypes.LIST))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR_LIST.build(
                    input -> {
                        IOperator innerOperator = input.getLeft();
                        OperatorBase.SafeVariablesGetter variables = input.getRight();
                        IValueTypeListProxy<IValueType<IValue>, IValue> list = variables.getValue(0, ValueTypes.LIST).getRawValue();
                        return ValueHelpers.evaluateOperator(innerOperator, Iterables.toArray(list, IValue.class));
                    })).build());

    /**
     * Apply for a given operator with zero arguments.
     */
    public static final IOperator OPERATOR_APPLY_0 = REGISTRY.register(OperatorBuilders.OPERATOR_1_PREFIX_LONG
            .conditionalOutputTypeDeriver(OperatorBuilders.OPERATOR_CONDITIONAL_OUTPUT_DERIVER)
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("apply0")
            .typeValidator(OperatorBuilders.createOperatorTypeValidator(new IValueType[0]))
            .function(OperatorBuilders.FUNCTION_OPERATOR_TAKE_OPERATOR.build(
                    input -> {
                        IOperator innerOperator = input.getLeft();
                        return ValueHelpers.evaluateOperator(innerOperator, new IVariable[0]);
                    })).build());

    /**
     * Apply the given operator on all elements of a list, resulting in a new list of mapped values.
     */
    public static final IOperator OPERATOR_MAP = REGISTRY.register(OperatorBuilders.OPERATOR_2_INFIX_LONG
            .inputTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST})
            .output(ValueTypes.LIST).symbolOperatorInteract("map")
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
            .output(ValueTypes.LIST).symbolOperatorInteract("filter")
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
            .output(ValueTypes.OPERATOR).symbol(".&&.").operatorInteract("conjunction")
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
            .output(ValueTypes.OPERATOR).symbol(".||.").operatorInteract("disjunction")
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
            .output(ValueTypes.OPERATOR).symbol("!.").operatorInteract("negation")
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
            .output(ValueTypes.OPERATOR).symbol(".").operatorInteract("pipe")
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
            .output(ValueTypes.OPERATOR).symbol(".2").operatorInteract("pipe2")
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
            .output(ValueTypes.OPERATOR).symbolOperatorInteract("flip")
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
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("reduce")
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
            .output(ValueTypes.CATEGORY_ANY).symbolOperatorInteract("reduce1")
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
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_REDUCE_EMPTY));
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
            .symbol("op_by_name").operatorName("by_name").interactName("operatorByName")
            .function(input -> {
                ValueTypeString.ValueString name = input.getValue(0, ValueTypes.STRING);
                try {
                    ResourceLocation id = new ResourceLocation(name.getRawValue());
                    IOperator operator = Operators.REGISTRY.getOperator(id);
                    if (operator == null) {
                        throw new EvaluationException(Component.translatable(
                                L10NValues.OPERATOR_ERROR_OPERATORNOTFOUND, name.getRawValue()));
                    }
                    return ValueTypeOperator.ValueOperator.of(operator);
                } catch (ResourceLocationException e) {
                    throw new EvaluationException(Component.literal(e.getMessage()));
                }
            }).build());

    /**
     * ----------------------------------- NBT OPERATORS -----------------------------------
     */

    /**
     * The number of entries in an NBT tag
     */
    public static final IOperator NBT_COMPOUND_SIZE = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).operatorName("compound_size").symbol("NBT{}.size").interactName("size")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_TO_INT.build(
                opt -> opt.map(CompoundTag::size).orElse(0)
            )).build());

    /**
     * The list of keys in an NBT tag
     */
    public static final IOperator NBT_COMPOUND_KEYS = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("compound_keys").symbol("NBT{}.keys").interactName("keys")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtKeys(value.getRawValue()));
            }).build());

    /**
     * If an NBT tag has the given key
     */
    public static final IOperator NBT_COMPOUND_HASKEY = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.BOOLEAN).operatorName("compound_haskey").symbol("NBT{}.has_key").interactName("hasKey")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_BOOLEAN.build(
                    Optional::isPresent
            )).build());

    /**
     * The NBT value type of an entry
     */
    public static final IOperator NBT_COMPOUND_VALUE_TYPE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.STRING).operatorName("compound_type").symbol("NBT{}.type").interactName("type")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_STRING.build(tag -> {
                if (tag.isPresent()) {
                    try {
                        return TagTypes.getType(tag.get().getId()).getName();
                    } catch (IndexOutOfBoundsException e) {

                    }
                }
                return "null";
            })).build());

    /**
     * The NBT tag value
     */
    public static final IOperator NBT_COMPOUND_VALUE_TAG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.NBT).operatorName("compound_value_tag").symbol("NBT{}.get_tag").interactName("getTag")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_NBT.build(o -> o)).build());

    /**
     * The NBT boolean value
     */
    public static final IOperator NBT_COMPOUND_VALUE_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.BOOLEAN).operatorName("compound_value_boolean").symbol("NBT{}.get_boolean").interactName("getBoolean")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_BOOLEAN.build(
                    o -> o.map(tag -> tag instanceof NumericTag && ((NumericTag) tag).getAsByte() != 0).orElse(false)
            )).build());

    /**
     * The NBT integer value
     */
    public static final IOperator NBT_COMPOUND_VALUE_INTEGER = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.INTEGER).operatorName("compound_value_integer").symbol("NBT{}.get_integer").interactName("getInteger")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_INT.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsInt() : 0).orElse(0)
            )).build());

    /**
     * The NBT long value
     */
    public static final IOperator NBT_COMPOUND_VALUE_LONG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LONG).operatorName("compound_value_long").symbol("NBT{}.get_long").interactName("getLong")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_LONG.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsLong() : 0).orElse(0L)
            )).build());

    /**
     * The NBT double value
     */
    public static final IOperator NBT_COMPOUND_VALUE_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.DOUBLE).operatorName("compound_value_double").symbol("NBT{}.get_double").interactName("getDouble")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_DOUBLE.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsDouble() : 0).orElse(0D)
            )).build());

    /**
     * The NBT string value
     */
    public static final IOperator NBT_COMPOUND_VALUE_STRING = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.STRING).operatorName("compound_value_string").symbol("NBT{}.get_string").interactName("getString")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_STRING.build(
                    o -> o.map(tag -> tag instanceof StringTag ? tag.getAsString() : "").orElse("")
            )).build());

    /**
     * The NBT compound value
     */
    public static final IOperator NBT_COMPOUND_VALUE_COMPOUND = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.NBT).operatorName("compound_value_compound").symbol("NBT{}.get_compound").interactName("getCompound")
            .function(OperatorBuilders.FUNCTION_NBT_COMPOUND_ENTRY_TO_NBT.build(
                    o -> o.map(tag -> tag instanceof CompoundTag ? (CompoundTag) tag : new CompoundTag())
            )).build());

    /**
     * The NBT tag list value
     */
    public static final IOperator NBT_COMPOUND_VALUE_LIST_TAG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("compound_value_list_tag").symbol("NBT{}.get_list_tag").interactName("getListTag")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListTag(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * The NBT boolean list value
     */
    public static final IOperator NBT_COMPOUND_VALUE_LIST_BYTE = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("compound_value_list_byte").symbol("NBT{}.get_list_byte").interactName("getListByte")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListByte(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * The NBT int list value
     */
    public static final IOperator NBT_COMPOUND_VALUE_LIST_INT = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("compound_value_list_int").symbol("NBT{}.get_list_int").interactName("getListInt")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListInt(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * The NBT long list value
     */
    public static final IOperator NBT_COMPOUND_VALUE_LIST_LONG = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.LIST).operatorName("compound_value_list_long").symbol("NBT{}.get_list_long").interactName("getListLong")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                ValueTypeString.ValueString key = variables.getValue(1, ValueTypes.STRING);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtValueListLong(key.getRawValue(), value.getRawValue()));
            }).build());

    /**
     * Remove an entry from an NBT compound
     */
    public static final IOperator NBT_COMPOUND_WITHOUT = REGISTRY.register(OperatorBuilders.NBT_2
            .output(ValueTypes.NBT).operatorName("compound_without").symbol("NBT{}.without").interactName("without")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt = variables.getValue(0, ValueTypes.NBT);
                Optional<Tag> tag = valueNbt.getRawValue();
                if (tag.isPresent()) {
                    if (!(tag.get() instanceof CompoundTag)) {
                        return ValueTypeNbt.ValueNbt.of();
                    }
                    ValueTypeString.ValueString valueString = variables.getValue(1, ValueTypes.STRING);
                    String key = valueString.getRawValue();
                    CompoundTag tagCompound = (CompoundTag) tag.get();
                    if (tagCompound.contains(key)) {
                        // Copy the tag to ensure immutability
                        tagCompound = tagCompound.copy();
                        tagCompound.remove(key);
                    }
                    return ValueTypeNbt.ValueNbt.of(tagCompound);
                }
                return valueNbt;
            }).build());



    /**
     * Set an NBT compound boolean value
     */
    public static final IOperator NBT_COMPOUND_WITH_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.BOOLEAN)
            .operatorName("compound_with_boolean").symbol("NBT{}.with_boolean").interactName("withBoolean")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeBoolean.ValueBoolean value = input.getRight().getValue(0, ValueTypes.BOOLEAN);
                input.getLeft().ifPresent(tag -> tag.putBoolean(input.getMiddle(), value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound short value
     */
    public static final IOperator NBT_COMPOUND_WITH_SHORT = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.INTEGER)
            .operatorName("compound_with_short").symbol("NBT{}.with_short").interactName("withShort")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeInteger.ValueInteger value = input.getRight().getValue(0, ValueTypes.INTEGER);
                input.getLeft().ifPresent(tag -> tag.putShort(input.getMiddle(), (short) value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound integer value
     */
    public static final IOperator NBT_COMPOUND_WITH_INTEGER = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.INTEGER)
            .operatorName("compound_with_integer").symbol("NBT{}.with_integer").interactName("withInteger")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeInteger.ValueInteger value = input.getRight().getValue(0, ValueTypes.INTEGER);
                input.getLeft().ifPresent(tag -> tag.putInt(input.getMiddle(), value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound long value
     */
    public static final IOperator NBT_COMPOUND_WITH_LONG = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LONG)
            .operatorName("compound_with_long").symbol("NBT{}.with_long").interactName("withLong")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeLong.ValueLong value = input.getRight().getValue(0, ValueTypes.LONG);
                input.getLeft().ifPresent(tag -> tag.putLong(input.getMiddle(), value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound double value
     */
    public static final IOperator NBT_COMPOUND_WITH_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.DOUBLE)
            .operatorName("compound_with_double").symbol("NBT{}.with_double").interactName("withDouble")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeDouble.ValueDouble value = input.getRight().getValue(0, ValueTypes.DOUBLE);
                input.getLeft().ifPresent(tag -> tag.putDouble(input.getMiddle(), value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound float value
     */
    public static final IOperator NBT_COMPOUND_WITH_FLOAT = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.DOUBLE)
            .operatorName("compound_with_float").symbol("NBT{}.with_float").interactName("withFloat")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeDouble.ValueDouble value = input.getRight().getValue(0, ValueTypes.DOUBLE);
                input.getLeft().ifPresent(tag -> tag.putFloat(input.getMiddle(), (float) value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound string value
     */
    public static final IOperator NBT_COMPOUND_WITH_STRING = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.STRING)
            .operatorName("compound_with_string").symbol("NBT{}.with_string").interactName("withString")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeString.ValueString value = input.getRight().getValue(0, ValueTypes.STRING);
                input.getLeft().ifPresent(tag -> tag.putString(input.getMiddle(), value.getRawValue()));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound compound value
     */
    public static final IOperator NBT_COMPOUND_WITH_COMPOUND = REGISTRY.register(OperatorBuilders.NBT_3
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.NBT)
            .operatorName("compound_with_tag").symbol("NBT{}.with_tag").interactName("withTag")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(input -> {
                ValueTypeNbt.ValueNbt value = input.getRight().getValue(0, ValueTypes.NBT);
                input.getLeft()
                        .ifPresent(tag -> value.getRawValue()
                                .ifPresent(v -> tag.put(input.getMiddle(), v)));
                return input.getLeft();
            })).build());

    /**
     * Set an NBT compound tag list value
     */
    public static final IOperator NBT_COMPOUND_WITH_LIST_TAG = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("compound_with_list_tag").symbol("NBT{}.with_tag_list").interactName("withTagList")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, Optional<CompoundTag>>() {
                @Override
                public Optional<CompoundTag> getOutput(Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    input.getLeft().ifPresent(tag -> tag.put(input.getMiddle(),
                            NbtHelpers.getListNbtTag(value, NBT_COMPOUND_WITH_LIST_TAG.getLocalizedNameFull())));
                    return input.getLeft();
                }
            })).build());

    /**
     * Set an NBT compound byte list value
     */
    public static final IOperator NBT_COMPOUND_WITH_LIST_BYTE = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("compound_with_list_byte").symbol("NBT{}.with_byte_list").interactName("withByteList")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, Optional<CompoundTag>>() {
                @Override
                public Optional<CompoundTag> getOutput(Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    input.getLeft().ifPresent(tag -> tag.put(input.getMiddle(),
                            NbtHelpers.getListNbtByte(value, NBT_COMPOUND_WITH_LIST_BYTE.getLocalizedNameFull())));
                    return input.getLeft();
                }
            })).build());

    /**
     * Set an NBT compound int list value
     */
    public static final IOperator NBT_COMPOUND_WITH_LIST_INT = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("compound_with_list_int").symbol("NBT{}.with_int_list").interactName("withIntList")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, Optional<CompoundTag>>() {
                @Override
                public Optional<CompoundTag> getOutput(Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    input.getLeft().ifPresent(tag -> tag.put(input.getMiddle(),
                            NbtHelpers.getListNbtInt(value, NBT_COMPOUND_WITH_LIST_INT.getLocalizedNameFull())));
                    return input.getLeft();
                }
            })).build());

    /**
     * Set an NBT compound long list value
     */
    public static final IOperator NBT_COMPOUND_WITH_LIST_LONG = REGISTRY.register(OperatorBuilders.NBT_3
            .renderPattern(IConfigRenderPattern.INFIX_2_VERYLONG)
            .inputTypes(ValueTypes.NBT, ValueTypes.STRING, ValueTypes.LIST)
            .operatorName("compound_with_list_long").symbol("NBT{}.with_list_long").interactName("withListLong")
            .function(OperatorBuilders.FUNCTION_NBT_COPY_FOR_VALUE_TO_NBT.build(new IOperatorValuePropagator<Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter>, Optional<CompoundTag>>() {
                @Override
                public Optional<CompoundTag> getOutput(Triple<Optional<CompoundTag>, String, OperatorBase.SafeVariablesGetter> input) throws EvaluationException {
                    ValueTypeList.ValueList<?, ?> value = input.getRight().getValue(0, ValueTypes.LIST);
                    input.getLeft().ifPresent(tag -> tag.put(input.getMiddle(),
                            NbtHelpers.getListNbtLong(value, NBT_COMPOUND_WITH_LIST_LONG.getLocalizedNameFull())));
                    return input.getLeft();
                }
            })).build());

    /**
     * Check if the first NBT compound tag is a subset of the second NBT compound tag.
     */
    public static final IOperator NBT_COMPOUND_SUBSET = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.BOOLEAN).operatorName("compound_subset").symbol("NBT{}.⊆").interactName("isSubset")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                if (valueNbt0.getRawValue().isPresent()
                        && valueNbt1.getRawValue().isPresent()
                        && valueNbt0.getRawValue().get() instanceof CompoundTag
                        && valueNbt1.getRawValue().get() instanceof CompoundTag) {
                    return ValueTypeBoolean.ValueBoolean.of(NbtHelpers.nbtMatchesSubset((CompoundTag) valueNbt0.getRawValue().get(), (CompoundTag) valueNbt1.getRawValue().get(), true));
                }
                return ValueTypeBoolean.ValueBoolean.of(false);
            }).build());

    /**
     * The union of the given NBT compound tags. Nested tags will be joined recusively.
     */
    public static final IOperator NBT_COMPOUND_UNION = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("compound_union").symbol("NBT{}.∪").interactName("union")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                if (valueNbt0.getRawValue().isPresent()
                        && valueNbt1.getRawValue().isPresent()
                        && valueNbt0.getRawValue().get() instanceof CompoundTag
                        && valueNbt1.getRawValue().get() instanceof CompoundTag) {
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.union((CompoundTag) valueNbt0.getRawValue().get(), (CompoundTag) valueNbt1.getRawValue().get()));
                }
                return ValueTypeNbt.ValueNbt.of();
            }).build());

    /**
     * The intersection of the given NBT compound tags. Nested tags will be intersected recusively.
     */
    public static final IOperator NBT_COMPOUND_INTERSECTION = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("compound_intersection").symbol("NBT{}.∩").interactName("intersection")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                if (valueNbt0.getRawValue().isPresent()
                        && valueNbt1.getRawValue().isPresent()
                        && valueNbt0.getRawValue().get() instanceof CompoundTag
                        && valueNbt1.getRawValue().get() instanceof CompoundTag) {
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.intersection((CompoundTag) valueNbt0.getRawValue().get(), (CompoundTag) valueNbt1.getRawValue().get()));
                }
                return ValueTypeNbt.ValueNbt.of();
            }).build());

    /**
     * The difference of the given NBT compound tags. Nested tags will be subtracted recusively.
     */
    public static final IOperator NBT_COMPOUND_MINUS = REGISTRY.register(OperatorBuilders.NBT_2_NBT
            .output(ValueTypes.NBT).operatorName("compound_minus").symbol("NBT{}.∖").interactName("minus")
            .function(variables -> {
                ValueTypeNbt.ValueNbt valueNbt0 = variables.getValue(0, ValueTypes.NBT);
                ValueTypeNbt.ValueNbt valueNbt1 = variables.getValue(1, ValueTypes.NBT);
                if (valueNbt0.getRawValue().isPresent()
                        && valueNbt1.getRawValue().isPresent()
                        && valueNbt0.getRawValue().get() instanceof CompoundTag
                        && valueNbt1.getRawValue().get() instanceof CompoundTag) {
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.minus((CompoundTag) valueNbt0.getRawValue().get(), (CompoundTag) valueNbt1.getRawValue().get()));
                }
                return ValueTypeNbt.ValueNbt.of();
            }).build());

    /**
     * The boolean value of an NBT value
     */
    public static final IOperator NBT_AS_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.BOOLEAN).operatorName("as_boolean").symbol("NBT.as_boolean").interactName("asBoolean")
            .function(OperatorBuilders.FUNCTION_NBT_TO_BOOLEAN.build(
                    o -> o.map(tag -> tag instanceof ByteTag && ((ByteTag) tag).getAsByte() != 0).orElse(false)
            )).build());

    /**
     * The byte value of an NBT value
     */
    public static final IOperator NBT_AS_BYTE = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).operatorName("as_byte").symbol("NBT.as_byte").interactName("asByte")
            .function(OperatorBuilders.FUNCTION_NBT_TO_INT.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsInt() : 0).orElse(0)
            )).build());

    /**
     * The short value of an NBT value
     */
    public static final IOperator NBT_AS_SHORT = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).operatorName("as_short").symbol("NBT.as_short").interactName("asShort")
            .function(OperatorBuilders.FUNCTION_NBT_TO_INT.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsInt() : 0).orElse(0)
            )).build());

    /**
     * The int value of an NBT value
     */
    public static final IOperator NBT_AS_INT = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.INTEGER).operatorName("as_int").symbol("NBT.as_int").interactName("asInt")
            .function(OperatorBuilders.FUNCTION_NBT_TO_INT.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsInt() : 0).orElse(0)
            )).build());

    /**
     * The long value of an NBT value
     */
    public static final IOperator NBT_AS_LONG = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LONG).operatorName("as_long").symbol("NBT.as_long").interactName("asLong")
            .function(OperatorBuilders.FUNCTION_NBT_TO_LONG.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsLong() : 0L).orElse(0L)
            )).build());

    /**
     * The double value of an NBT value
     */
    public static final IOperator NBT_AS_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).operatorName("as_double").symbol("NBT.as_double").interactName("asDouble")
            .function(OperatorBuilders.FUNCTION_NBT_TO_DOUBLE.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsDouble() : 0D).orElse(0D)
            )).build());

    /**
     * The float value of an NBT value
     */
    public static final IOperator NBT_AS_FLOAT = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.DOUBLE).operatorName("as_float").symbol("NBT.as_float").interactName("asFloat")
            .function(OperatorBuilders.FUNCTION_NBT_TO_DOUBLE.build(
                    o -> o.map(tag -> tag instanceof NumericTag ? ((NumericTag) tag).getAsFloat() : 0D).orElse(0D)
            )).build());

    /**
     * The string value of an NBT value
     */
    public static final IOperator NBT_AS_STRING = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.STRING).operatorName("as_string").symbol("NBT.as_string").interactName("asString")
            .function(OperatorBuilders.FUNCTION_NBT_TO_STRING.build(
                    o -> o.map(tag -> tag instanceof StringTag ? ((StringTag) tag).getAsString() : "").orElse("")
            )).build());

    /**
     * The tag list value of an NBT value
     */
    public static final IOperator NBT_AS_TAG_LIST = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("as_tag_list").symbol("NBT.as_tag_list").interactName("asTagList")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtAsListTag(value.getRawValue()));
            }).build());

    /**
     * The byte list value of an NBT value
     */
    public static final IOperator NBT_AS_BYTE_LIST = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("as_byte_list").symbol("NBT.as_byte_list").interactName("asByteList")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtAsListByte(value.getRawValue()));
            }).build());

    /**
     * The int list value of an NBT value
     */
    public static final IOperator NBT_AS_INT_LIST = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("as_int_list").symbol("NBT.as_int_list").interactName("asIntList")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtAsListInt(value.getRawValue()));
            }).build());

    /**
     * The long list value of an NBT value
     */
    public static final IOperator NBT_AS_LONG_LIST = REGISTRY.register(OperatorBuilders.NBT_1_SUFFIX_LONG
            .output(ValueTypes.LIST).operatorName("as_long_list").symbol("NBT.as_long_list").interactName("asLongList")
            .function(variables -> {
                ValueTypeNbt.ValueNbt value = variables.getValue(0, ValueTypes.NBT);
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyNbtAsListLong(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a boolean value
     */
    public static final IOperator NBT_FROM_BOOLEAN = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.BOOLEAN).output(ValueTypes.NBT)
            .operatorName("from_boolean").symbol("NBT.from_boolean").interactName("asNbt")
            .function(variables -> {
                ValueTypeBoolean.ValueBoolean value = variables.getValue(0, ValueTypes.BOOLEAN);
                return ValueTypeNbt.ValueNbt.of(ByteTag.valueOf(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a short value
     */
    public static final IOperator NBT_FROM_SHORT = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.INTEGER).output(ValueTypes.NBT)
            .operatorName("from_short").symbol("NBT.from_short").interactName("asNbt", "short", true)
            .function(variables -> {
                ValueTypeInteger.ValueInteger value = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeNbt.ValueNbt.of(ShortTag.valueOf((short) value.getRawValue()));
            }).build());

    /**
     * The NBT value of a byte value
     */
    public static final IOperator NBT_FROM_BYTE = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.INTEGER).output(ValueTypes.NBT)
            .operatorName("from_byte").symbol("NBT.from_byte").interactName("asNbt", "byte", true)
            .function(variables -> {
                ValueTypeInteger.ValueInteger value = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeNbt.ValueNbt.of(ByteTag.valueOf((byte) value.getRawValue()));
            }).build());

    /**
     * The NBT value of an int value
     */
    public static final IOperator NBT_FROM_INT = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.INTEGER).output(ValueTypes.NBT)
            .operatorName("from_int").symbol("NBT.from_int").interactName("asNbt")
            .function(variables -> {
                ValueTypeInteger.ValueInteger value = variables.getValue(0, ValueTypes.INTEGER);
                return ValueTypeNbt.ValueNbt.of(IntTag.valueOf(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a long value
     */
    public static final IOperator NBT_FROM_LONG = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.LONG).output(ValueTypes.NBT)
            .operatorName("from_long").symbol("NBT.from_long").interactName("asNbt")
            .function(variables -> {
                ValueTypeLong.ValueLong value = variables.getValue(0, ValueTypes.LONG);
                return ValueTypeNbt.ValueNbt.of(LongTag.valueOf(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a double value
     */
    public static final IOperator NBT_FROM_DOUBLE = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.DOUBLE).output(ValueTypes.NBT)
            .operatorName("from_double").symbol("NBT.from_double").interactName("asNbt")
            .function(variables -> {
                ValueTypeDouble.ValueDouble value = variables.getValue(0, ValueTypes.DOUBLE);
                return ValueTypeNbt.ValueNbt.of(DoubleTag.valueOf(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a float value
     */
    public static final IOperator NBT_FROM_FLOAT = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.DOUBLE).output(ValueTypes.NBT)
            .operatorName("from_float").symbol("NBT.from_float").interactName("asNbt", "float", true)
            .function(variables -> {
                ValueTypeDouble.ValueDouble value = variables.getValue(0, ValueTypes.DOUBLE);
                return ValueTypeNbt.ValueNbt.of(FloatTag.valueOf((float) value.getRawValue()));
            }).build());

    /**
     * The NBT value of a string value
     */
    public static final IOperator NBT_FROM_STRING = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.STRING).output(ValueTypes.NBT)
            .operatorName("from_string").symbol("NBT.from_string").interactName("asNbt")
            .function(variables -> {
                ValueTypeString.ValueString value = variables.getValue(0, ValueTypes.STRING);
                return ValueTypeNbt.ValueNbt.of(StringTag.valueOf(value.getRawValue()));
            }).build());

    /**
     * The NBT value of a tag list value
     */
    public static final IOperator NBT_FROM_TAG_LIST = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.LIST).output(ValueTypes.NBT)
            .operatorName("from_tag_list").symbol("NBT.from_tag_list").interactName("asNbt", "tagList", true)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList value = variables.getValue(0, ValueTypes.LIST);
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.getListNbtTag(value, NBT_FROM_TAG_LIST.getLocalizedNameFull()));
                }
            }).build());

    /**
     * The NBT value of a byte list value
     */
    public static final IOperator NBT_FROM_BYTE_LIST = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.LIST).output(ValueTypes.NBT)
            .operatorName("from_byte_list").symbol("NBT.from_byte_list").interactName("asNbt", "byteList", true)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList value = variables.getValue(0, ValueTypes.LIST);
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.getListNbtByte(value, NBT_FROM_BYTE_LIST.getLocalizedNameFull()));
                }
            }).build());

    /**
     * The NBT value of a int list value
     */
    public static final IOperator NBT_FROM_INT_LIST = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.LIST).output(ValueTypes.NBT)
            .operatorName("from_int_list").symbol("NBT.from_int_list").interactName("asNbt", "intList", true)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList value = variables.getValue(0, ValueTypes.LIST);
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.getListNbtInt(value, NBT_FROM_INT_LIST.getLocalizedNameFull()));
                }
            }).build());

    /**
     * The NBT value of a long list value
     */
    public static final IOperator NBT_FROM_LONG_LIST = REGISTRY.register(OperatorBuilders.NBT_1_PREFIX_LONG
            .inputType(ValueTypes.LIST).output(ValueTypes.NBT)
            .operatorName("from_long_list").symbol("NBT.from_long_list").interactName("asNbt", "longList", true)
            .function(new OperatorBase.IFunction() {
                @Override
                public IValue evaluate(OperatorBase.SafeVariablesGetter variables) throws EvaluationException {
                    ValueTypeList.ValueList value = variables.getValue(0, ValueTypes.LIST);
                    return ValueTypeNbt.ValueNbt.of(NbtHelpers.getListNbtLong(value, NBT_FROM_LONG_LIST.getLocalizedNameFull()));
                }
            }).build());

    /**
     * Apply the given NBT path expression on the given NBT value and get the first result.
     */
    public static final IOperator NBT_PATH_MATCH_FIRST = REGISTRY.register(OperatorBuilders.NBT_2
            .inputTypes(ValueTypes.STRING, ValueTypes.NBT).output(ValueTypes.NBT)
            .operatorName("path_match_first").symbol("NBT.path_match_first").interactName("nbtPathMatchFirst")
            .function(variables -> {
                ValueTypeString.ValueString string = variables.getValue(0, ValueTypes.STRING);
                ValueTypeNbt.ValueNbt nbt = variables.getValue(1, ValueTypes.NBT);
                INbtPathExpression expression = null;
                try {
                    expression = NbtPath.parse(string.getRawValue());
                } catch (NbtParseException e) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_NBT_PATH_EXPRESSION,
                            string.getRawValue(),
                            e.getMessage()));
                }
                if (!nbt.getRawValue().isPresent()) {
                    return ValueTypeNbt.ValueNbt.of();
                }
                return ValueTypeNbt.ValueNbt.of(expression.match(nbt.getRawValue().get()).getMatches().findAny());
            }).build());

    /**
     * Apply the given NBT path expression on the given NBT value and get all results.
     */
    public static final IOperator NBT_PATH_MATCH_ALL = REGISTRY.register(OperatorBuilders.NBT_2
            .inputTypes(ValueTypes.STRING, ValueTypes.NBT).output(ValueTypes.LIST)
            .operatorName("path_match_all").symbol("NBT.path_match_all").interactName("nbtPathMatchAll")
            .function(variables -> {
                ValueTypeString.ValueString string = variables.getValue(0, ValueTypes.STRING);
                ValueTypeNbt.ValueNbt nbt = variables.getValue(1, ValueTypes.NBT);
                INbtPathExpression expression = null;
                try {
                    expression = NbtPath.parse(string.getRawValue());
                } catch (NbtParseException e) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_NBT_PATH_EXPRESSION,
                            string.getRawValue(),
                            e.getMessage()));
                }
                if (!nbt.getRawValue().isPresent()) {
                    return ValueTypeList.ValueList.ofAll(ValueTypes.NBT);
                }
                List<ValueTypeNbt.ValueNbt> matches = expression.match(nbt.getRawValue().get()).getMatches()
                        .map(ValueTypeNbt.ValueNbt::of)
                        .collect(Collectors.toList());
                return ValueTypeList.ValueList.ofList(ValueTypes.NBT, matches);
            }).build());

    /**
     * Test the given NBT path expression on the given NBT value.
     */
    public static final IOperator NBT_PATH_TEST = REGISTRY.register(OperatorBuilders.NBT_2
            .inputTypes(ValueTypes.STRING, ValueTypes.NBT).output(ValueTypes.BOOLEAN)
            .operatorName("path_test").symbol("NBT.path_test").interactName("nbtPathTest")
            .function(variables -> {
                ValueTypeString.ValueString string = variables.getValue(0, ValueTypes.STRING);
                ValueTypeNbt.ValueNbt nbt = variables.getValue(1, ValueTypes.NBT);
                INbtPathExpression expression = null;
                try {
                    expression = NbtPath.parse(string.getRawValue());
                } catch (NbtParseException e) {
                    throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_NBT_PATH_EXPRESSION,
                            string.getRawValue(),
                            e.getMessage()));
                }
                if (!nbt.getRawValue().isPresent()) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
                return ValueTypeBoolean.ValueBoolean.of(expression.test(nbt.getRawValue().get()));
            }).build());

    /**
     * ----------------------------------- INGREDIENTS OPERATORS -----------------------------------
     */

    /**
     * The list of items.
     */
    public static final IOperator INGREDIENTS_ITEMS = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorInteract("items").symbol("Ingr.items")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.ITEMSTACK))
            .build());

    /**
     * The list of fluids
     */
    public static final IOperator INGREDIENTS_FLUIDS = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorInteract("fluids").symbol("Ingr.fluids")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.FLUIDSTACK))
            .build());

    /**
     * The list of fluids
     */
    public static final IOperator INGREDIENTS_ENERGIES = REGISTRY.register(OperatorBuilders.INGREDIENTS_1_PREFIX_LONG
            .output(ValueTypes.LIST).operatorInteract("energies").symbol("Ingr.energies")
            .function(OperatorBuilders.createFunctionIngredientsList(() -> IngredientComponent.ENERGY))
            .build());

    /**
     * Set an ingredient item
     */
    public static final IOperator INGREDIENTS_WITH_ITEM = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_ITEMSTACK
            .operatorName("with_item").symbol("Ingr.with_item").interactName("withItem")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueObjectTypeItemStack.ValueItemStack itemStack = variables.getValue(2, ValueTypes.OBJECT_ITEMSTACK);
                if (value.getRawValue().isEmpty()) {
                    value = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.ITEMSTACK, itemStack.getRawValue()));
            }).build());

    /**
     * Set an ingredient fluid
     */
    public static final IOperator INGREDIENTS_WITH_FLUID = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_FLUIDSTACK
            .operatorName("with_fluid").symbol("Ingr.with_fluid").interactName("withFluid")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueObjectTypeFluidStack.ValueFluidStack fluidStack = variables.getValue(2, ValueTypes.OBJECT_FLUIDSTACK);
                if (value.getRawValue().isEmpty()) {
                    value = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.FLUIDSTACK, fluidStack.getRawValue()));
            }).build());

    /**
     * Set an ingredient energy
     */
    public static final IOperator INGREDIENTS_WITH_ENERGY = REGISTRY.register(OperatorBuilders.INGREDIENTS_3_LONG
            .operatorName("with_energy").symbol("Ingr.with_energy").interactName("withEnergy")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients value = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeInteger.ValueInteger index = variables.getValue(1, ValueTypes.INTEGER);
                ValueTypeLong.ValueLong energy = variables.getValue(2, ValueTypes.LONG);
                if (value.getRawValue().isEmpty()) {
                    value = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
                }
                IMixedIngredients baseIngredients = value.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsSingle<>(baseIngredients,
                        index.getRawValue(), IngredientComponent.ENERGY, energy.getRawValue()));
            }).build());

    /**
     * Set the list of items
     */
    public static final IOperator INGREDIENTS_WITH_ITEMS = REGISTRY.register(OperatorBuilders.INGREDIENTS_2_LIST
            .operatorName("with_items").symbol("Ingr.with_items").interactName("withItems")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> list = variables.getValue(1, ValueTypes.LIST);
                if (valueIngredients.getRawValue().isEmpty()) {
                    valueIngredients = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
                }
                IMixedIngredients baseIngredients = valueIngredients.getRawValue().get();
                return ValueObjectTypeIngredients.ValueIngredients.of(new ExtendedIngredientsList<>(baseIngredients,
                        IngredientComponent.ITEMSTACK, OperatorBuilders.unwrapIngredientComponentList(IngredientComponent.ITEMSTACK, list)));
            }).build());

    /**
     * Set the list of fluids
     */
    public static final IOperator INGREDIENTS_WITH_FLUIDS = REGISTRY.register(OperatorBuilders.INGREDIENTS_2_LIST
            .operatorName("with_fluids").symbol("Ingr.with_fluids").interactName("withFluids")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> list = variables.getValue(1, ValueTypes.LIST);
                if (valueIngredients.getRawValue().isEmpty()) {
                    valueIngredients = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
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
            .operatorName("with_energies").symbol("Ingr.with_energies").interactName("withEnergies")
            .function(variables -> {
                ValueObjectTypeIngredients.ValueIngredients valueIngredients = variables.getValue(0, ValueTypes.OBJECT_INGREDIENTS);
                ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = variables.getValue(1, ValueTypes.LIST);
                if (valueIngredients.getRawValue().isEmpty()) {
                    valueIngredients = ValueObjectTypeIngredients.ValueIngredients.of(new MixedIngredients(Maps.newIdentityHashMap()));
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
            .operatorInteract("input").symbol("recipe_in")
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
            .operatorInteract("output").symbol("recipe_out")
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
            .operatorName("with_input").symbol("Recipe.with_in").interactName("withInput")
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
            .operatorName("with_output").symbol("Recipe.with_out").interactName("withOutput")
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
            .operatorName("with_input_output").symbol("Recipe.with_io").interactName("withInputOutput")
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
                    try {
                        return ValueObjectTypeRecipe.ValueRecipe.of(new RecipeDefinition(
                                inputs,
                                valueOut.getRawValue().get()
                        ));
                    } catch (IllegalArgumentException e) {
                        throw new EvaluationException(Component.literal(e.getMessage()));
                    }
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
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value.getRawValue(),
                    Component.translatable(ValueTypes.DOUBLE.getTranslationKey())));
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
          throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value.getRawValue(),
                  Component.translatable(ValueTypes.INTEGER.getTranslationKey())));
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
          throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value.getRawValue(),
                  Component.translatable(ValueTypes.LONG.getTranslationKey())));
      }
    }));

    /**
     * NBT Parse operator which takes a string of a form ValueTypeNbt().deserialize() can consume.
     */
    public static final IOperator PARSE_NBT = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.NBT, v -> {
      ValueTypeString.ValueString value = v.getValue(0, ValueTypes.STRING);
      try {
        return ValueTypeNbt.ValueNbt.of(TagParser.parseTag(value.getRawValue()));
      } catch (CommandSyntaxException e) {
        throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value.getRawValue(),
                Component.translatable(ValueTypes.NBT.getTranslationKey())));
      }
    }));

    /**
     * ----------------------------------- GENERAL OPERATORS -----------------------------------
     */

    /**
     * Choice operator with one boolean input, two any inputs and one output any.
     */
    public static final GeneralOperator GENERAL_CHOICE = REGISTRY.register(new GeneralChoiceOperator("?", "choice", "choice"));

    /**
     * Identity operator with one any input and one any output
     */
    public static final GeneralOperator GENERAL_IDENTITY = REGISTRY.register(new GeneralIdentityOperator("id", "identity", "identity"));

    /**
     * Constant operator with two any inputs and one any output
     */
    public static final GeneralOperator GENERAL_CONSTANT = REGISTRY.register(new GeneralConstantOperator("K", "constant", "constant"));

}
