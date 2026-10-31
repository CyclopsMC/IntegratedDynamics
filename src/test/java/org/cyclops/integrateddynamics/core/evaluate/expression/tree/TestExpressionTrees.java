package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueDeseralizationContextMocked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for expression trees.
 * @author rubensworks
 */
public class TestExpressionTrees {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    @Before
    public void before() {
        ValueTypeListProxyFactories.load();
    }

    private static ExpressionNode op(IOperator operator, ExpressionNode... children) {
        return new ExpressionNode.Operator(operator, List.of(children));
    }

    private static ExpressionNode c(int value) {
        return new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(value));
    }

    private static ExpressionNode c(String value) {
        return new ExpressionNode.Const(ValueTypeString.ValueString.of(value));
    }

    private static ExpressionNode ref(int id) {
        return new ExpressionNode.Ref(id);
    }

    private static ExpressionNode p(int index) {
        return new ExpressionNode.Param(index);
    }

    private static ExpressionParameter param(String name, IValueType<?> type) {
        return new ExpressionParameter(name, type);
    }

    // s -> length(s) > 3
    private static Expression longString() {
        return new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), c(3)),
                List.of(param("s", ValueTypes.STRING)));
    }

    private static IValue eval(IOperator operator, IValue... values) throws EvaluationException {
        return ValueHelpers.evaluateOperator(operator, values);
    }

    private static boolean bool(IValue value) {
        return ((ValueTypeBoolean.ValueBoolean) value).getRawValue();
    }

    private static int integer(IValue value) {
        return ((ValueTypeInteger.ValueInteger) value).getRawValue();
    }

    private static ValueTypeList.ValueList strings(String... values) {
        ValueTypeString.ValueString[] list = new ValueTypeString.ValueString[values.length];
        for (int i = 0; i < values.length; i++) {
            list[i] = ValueTypeString.ValueString.of(values[i]);
        }
        return ValueTypeList.ValueList.ofAll(list);
    }

    private static ValueTypeList.ValueList ints(int... values) {
        ValueTypeInteger.ValueInteger[] list = new ValueTypeInteger.ValueInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            list[i] = ValueTypeInteger.ValueInteger.of(values[i]);
        }
        return ValueTypeList.ValueList.ofAll(list);
    }

    private static int binarySize(CompoundTag tag) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(out));
            return out.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --------------- Serialization ---------------

    @Test
    public void testSerializationRoundTrip() throws EvaluationException {
        Expression expression = new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, p(1), ref(42)),
                c("abc"),
                op(Operators.STRING_CONCAT, p(0), ExpressionNode.EMPTY)),
                List.of(param("a", ValueTypes.STRING), param("b", ValueTypes.CATEGORY_ANY)));
        CompoundTag tag = ExpressionTrees.serialize(ValueDeseralizationContextMocked.get(), expression);
        Expression read = ExpressionTrees.deserialize(ValueDeseralizationContextMocked.get(), tag);
        assertThat(read, is(expression));
    }

    @Test
    public void testSerializationDeepTree() throws EvaluationException {
        // 1 ++ ++ ++ ... (60 times)
        ExpressionNode node = c(1);
        for (int i = 0; i < 60; i++) {
            node = op(Operators.ARITHMETIC_INCREMENT, node);
        }
        Expression expression = new Expression(node);
        CompoundTag tag = ExpressionTrees.serialize(ValueDeseralizationContextMocked.get(), expression);
        // Binary size grows linearly, about 60 bytes per operator node
        int size = binarySize(tag);
        assertTrue("Serialized size was " + size, size < 5000);
        Expression read = ExpressionTrees.deserialize(ValueDeseralizationContextMocked.get(), tag);
        assertThat(read, is(expression));
        assertThat(ExpressionTrees.depth(read.root()), is(60));

        IVariable<?> variable = ExpressionEvaluator.buildLazy(read.root(), 0, new ExpressionEvaluator.LocalValueCache(), id -> null);
        assertThat(integer(variable.getValue()), is(61));
    }

    @Test(expected = EvaluationException.class)
    public void testSerializationTooDeep() throws EvaluationException {
        ExpressionNode node = c(1);
        for (int i = 0; i < ExpressionTrees.MAX_DEPTH + 2; i++) {
            node = op(Operators.ARITHMETIC_INCREMENT, node);
        }
        CompoundTag tag = ExpressionTrees.serialize(ValueDeseralizationContextMocked.get(), new Expression(node));
        ExpressionTrees.deserialize(ValueDeseralizationContextMocked.get(), tag);
    }

    @Test(expected = EvaluationException.class)
    public void testSerializationInvalidParamIndex() throws EvaluationException {
        CompoundTag tag = ExpressionTrees.serialize(ValueDeseralizationContextMocked.get(), new Expression(op(Operators.STRING_LENGTH, p(3))));
        ExpressionTrees.deserialize(ValueDeseralizationContextMocked.get(), tag);
    }

    // --------------- Paths and queries ---------------

    @Test
    public void testPaths() {
        ExpressionNode root = op(Operators.ARITHMETIC_ADDITION, c(1), op(Operators.ARITHMETIC_INCREMENT, ExpressionNode.EMPTY));
        assertThat(ExpressionTrees.get(root, new int[]{1, 0}), is(ExpressionNode.EMPTY));
        assertThat(ExpressionTrees.get(root, new int[]{0, 0}), nullValue());
        assertThat(ExpressionTrees.countEmpty(root), is(1));
        ExpressionNode replaced = ExpressionTrees.replace(root, new int[]{1, 0}, c(5));
        assertThat(ExpressionTrees.countEmpty(replaced), is(0));
        assertThat(ExpressionTrees.get(replaced, new int[]{1, 0}), is(c(5)));
        assertThat(ExpressionTrees.countEmpty(root), is(1)); // Immutable
    }

    @Test
    public void testPlainOperator() {
        assertTrue(ExpressionTrees.isPlainOperator(new Expression(op(Operators.ARITHMETIC_ADDITION, ref(1), ref(2)))));
        assertTrue(!ExpressionTrees.isPlainOperator(new Expression(op(Operators.ARITHMETIC_ADDITION, ref(1), c(2)))));
        assertTrue(!ExpressionTrees.isPlainOperator(new Expression(op(Operators.STRING_LENGTH, ref(1)), List.of(param("x", ValueTypes.STRING)))));
        Expression plain = ExpressionTrees.fromPlainOperator(Operators.ARITHMETIC_ADDITION, new int[]{3, 4});
        assertThat(ExpressionTrees.getPlainOperatorVariableIds(plain), is(new int[]{3, 4}));
    }

    @Test
    public void testRemoveParam() {
        ExpressionNode root = op(Operators.ARITHMETIC_ADDITION, p(0), p(1));
        assertThat(ExpressionTrees.removeParam(root, 0), is(op(Operators.ARITHMETIC_ADDITION, ExpressionNode.EMPTY, p(0))));
    }

    // --------------- Evaluation without parameters ---------------

    @Test
    public void testEvaluateEquivalentToChain() throws EvaluationException {
        Variable<ValueTypeString.ValueString> s = new Variable<>(ValueTypeString.ValueString.of("abcd"));

        // Chain of single-operator expressions, as with separate cards
        ExpressionEvaluator.LocalValueCache cache = new ExpressionEvaluator.LocalValueCache();
        LazyExpression<?> length = new LazyExpression<>(1, Operators.STRING_LENGTH, new IVariable[]{s}, cache);
        LazyExpression<?> three = new LazyExpression<>(2, Operators.GENERAL_IDENTITY,
                new IVariable[]{new Variable<>(ValueTypeInteger.ValueInteger.of(3))}, cache);
        LazyExpression<?> chain = new LazyExpression<>(3, Operators.ARITHMETIC_ADDITION, new IVariable[]{length, three}, cache);

        // Single expression with a reference and inline constant
        IVariable<?> tree = ExpressionEvaluator.buildLazy(op(Operators.ARITHMETIC_ADDITION, op(Operators.STRING_LENGTH, ref(7)), c(3)),
                10, new ExpressionEvaluator.LocalValueCache(), id -> id == 7 ? s : null);

        assertThat(tree.getValue(), is(chain.getValue()));
        assertThat(integer(tree.getValue()), is(7));
        assertThat(tree.getType(), is((IValueType) ValueTypes.INTEGER));
    }

    @Test
    public void testEvaluateMissingRef() {
        IVariable<?> tree = ExpressionEvaluator.buildLazy(op(Operators.STRING_LENGTH, ref(7)),
                10, new ExpressionEvaluator.LocalValueCache(), id -> null);
        assertThat(tree, nullValue());
    }

    @Test
    public void testEvaluateInvalidation() throws EvaluationException {
        MutableVariable<ValueTypeString.ValueString> s = new MutableVariable<>(ValueTypes.STRING, ValueTypeString.ValueString.of("ab"));
        IVariable<?> tree = ExpressionEvaluator.buildLazy(op(Operators.ARITHMETIC_ADDITION, op(Operators.STRING_LENGTH, ref(7)), c(3)),
                10, new ExpressionEvaluator.LocalValueCache(), id -> s);
        assertThat(integer(tree.getValue()), is(5));
        s.set(ValueTypeString.ValueString.of("abcdef"));
        assertThat(integer(tree.getValue()), is(9));
    }

    // --------------- Evaluation with parameters ---------------

    @Test
    public void testOperatorSignature() {
        ExpressionOperator operator = ExpressionOperator.of(longString());
        assertThat(operator.getInputTypes(), is(new IValueType[]{ValueTypes.STRING}));
        assertThat(operator.getOutputType(), is((IValueType) ValueTypes.BOOLEAN));
        assertThat(operator.getRequiredInputLength(), is(1));
    }

    @Test
    public void testOperatorEvaluate() throws EvaluationException {
        ExpressionOperator operator = ExpressionOperator.of(longString());
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abcd"))), is(true));
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abc"))), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testOperatorEvaluateWrongType() throws EvaluationException {
        eval(ExpressionOperator.of(longString()), ValueTypeInteger.ValueInteger.of(3));
    }

    @Test
    public void testOperatorParamUsedTwice() throws EvaluationException {
        // x -> x * x
        ExpressionOperator square = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_MULTIPLICATION, p(0), p(0)),
                List.of(param("x", ValueTypes.INTEGER))));
        assertThat(integer(eval(square, ValueTypeInteger.ValueInteger.of(7))), is(49));
    }

    @Test
    public void testOperatorWithRef() throws EvaluationException {
        // s -> length(s) > #limit
        MutableVariable<ValueTypeInteger.ValueInteger> limit = new MutableVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        refs.put(5, limit);
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), ref(5)),
                List.of(param("s", ValueTypes.STRING))), refs);
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abcd"))), is(true));
        limit.set(ValueTypeInteger.ValueInteger.of(10));
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abcd"))), is(false));
    }

    @Test
    public void testOperatorApply() throws EvaluationException {
        IValue result = eval(Operators.OPERATOR_APPLY, ValueTypeOperator.ValueOperator.of(ExpressionOperator.of(longString())),
                ValueTypeString.ValueString.of("hello"));
        assertThat(bool(result), is(true));
    }

    @Test
    public void testOperatorFilter() throws EvaluationException {
        IValue result = eval(Operators.OPERATOR_FILTER, ValueTypeOperator.ValueOperator.of(ExpressionOperator.of(longString())),
                strings("a", "abcd", "abc", "hello"));
        assertThat(result, is(strings("abcd", "hello")));
    }

    @Test
    public void testOperatorMap() throws EvaluationException {
        // s -> length(s) + 1
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_ADDITION, op(Operators.STRING_LENGTH, p(0)), c(1)),
                List.of(param("s", ValueTypes.STRING))));
        IValue result = eval(Operators.OPERATOR_MAP, ValueTypeOperator.ValueOperator.of(operator), strings("a", "abcd"));
        assertThat(result, is(ints(2, 5)));
    }

    @Test
    public void testOperatorReduce() throws EvaluationException {
        // (acc, x) -> acc + x * 2
        ExpressionOperator operator = ExpressionOperator.of(new Expression(
                op(Operators.ARITHMETIC_ADDITION, p(0), op(Operators.ARITHMETIC_MULTIPLICATION, p(1), c(2))),
                List.of(param("acc", ValueTypes.INTEGER), param("x", ValueTypes.INTEGER))));
        IValue result = eval(Operators.OPERATOR_REDUCE, ValueTypeOperator.ValueOperator.of(operator), ints(1, 2, 3),
                ValueTypeInteger.ValueInteger.of(0));
        assertThat(integer(result), is(12));
    }

    @Test
    public void testOperatorPipe() throws EvaluationException {
        IValue piped = eval(Operators.OPERATOR_PIPE, ValueTypeOperator.ValueOperator.of(ExpressionOperator.of(longString())),
                ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
        IOperator pipedOperator = ((ValueTypeOperator.ValueOperator) piped).getRawValue();
        assertThat(bool(eval(pipedOperator, ValueTypeString.ValueString.of("abcd"))), is(false));
        assertThat(bool(eval(pipedOperator, ValueTypeString.ValueString.of("ab"))), is(true));
    }

    @Test
    public void testOperatorFlip() throws EvaluationException {
        // (a, b) -> a - b
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_SUBTRACTION, p(0), p(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER))));
        IValue flipped = eval(Operators.OPERATOR_FLIP, ValueTypeOperator.ValueOperator.of(operator));
        IOperator flippedOperator = ((ValueTypeOperator.ValueOperator) flipped).getRawValue();
        assertThat(integer(eval(flippedOperator, ValueTypeInteger.ValueInteger.of(10), ValueTypeInteger.ValueInteger.of(3))), is(-7));
    }

    @Test
    public void testOperatorCurrying() throws EvaluationException {
        // (a, b) -> a - b, applied to 10
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_SUBTRACTION, p(0), p(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER))));
        IValue curried = eval(operator, ValueTypeInteger.ValueInteger.of(10));
        assertThat(curried, instanceOf(ValueTypeOperator.ValueOperator.class));
        IOperator curriedOperator = ((ValueTypeOperator.ValueOperator) curried).getRawValue();
        assertThat(integer(eval(curriedOperator, ValueTypeInteger.ValueInteger.of(4))), is(6));
    }

    @Test
    public void testOperatorLazyChoice() throws EvaluationException {
        // x -> x > 0 ? x : (1 / 0), the else branch must never be evaluated for positive x
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, p(0), c(0)), p(0), op(Operators.ARITHMETIC_DIVISION, c(1), c(0))),
                List.of(param("x", ValueTypes.INTEGER))));
        assertThat(integer(eval(operator, ValueTypeInteger.ValueInteger.of(5))), is(5));
    }

    @Test
    public void testOperatorSerialization() throws EvaluationException {
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        refs.put(5, new Variable<>(ValueTypeInteger.ValueInteger.of(3)));
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), ref(5)),
                List.of(param("s", ValueTypes.STRING))), refs);
        ValueTypeOperator.ValueOperator value = ValueTypeOperator.ValueOperator.of(operator);
        CompoundTag tag = ValueHelpers.serialize(ValueDeseralizationContextMocked.get(), value);
        IValue read = ValueHelpers.deserialize(ValueDeseralizationContextMocked.get(), tag);
        IOperator readOperator = ((ValueTypeOperator.ValueOperator) read).getRawValue();
        assertThat(readOperator, instanceOf(ExpressionOperator.class));
        // Refs are replaced by their values when serialized
        assertThat(((ExpressionOperator) readOperator).getExpression().root(),
                is(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), c(3))));
        assertThat(readOperator.getOutputType(), is((IValueType) ValueTypes.BOOLEAN));
        assertThat(bool(eval(readOperator, ValueTypeString.ValueString.of("abcd"))), is(true));
    }

    @Test
    public void testOperatorMaterialize() throws EvaluationException {
        MutableVariable<ValueTypeInteger.ValueInteger> limit = new MutableVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        refs.put(5, limit);
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), ref(5)),
                List.of(param("s", ValueTypes.STRING))), refs);
        IOperator materialized = operator.materialize();
        assertThat(((ExpressionOperator) materialized).getRefs().isEmpty(), is(true));
        limit.set(ValueTypeInteger.ValueInteger.of(10));
        // The materialized operator no longer follows the reference
        assertThat(bool(eval(materialized, ValueTypeString.ValueString.of("abcd"))), is(true));
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abcd"))), is(false));
    }

    @Test
    public void testOperatorMaterializeInPipe() throws EvaluationException {
        // Materializing a combined operator must materialize the inner expression operator as well
        MutableVariable<ValueTypeInteger.ValueInteger> limit = new MutableVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        refs.put(5, limit);
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), ref(5)),
                List.of(param("s", ValueTypes.STRING))), refs);
        IValue piped = eval(Operators.OPERATOR_PIPE, ValueTypeOperator.ValueOperator.of(operator),
                ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
        IValue materialized = ValueTypes.OPERATOR.materialize((ValueTypeOperator.ValueOperator) piped);
        limit.set(ValueTypeInteger.ValueInteger.of(10));
        assertThat(bool(eval(((ValueTypeOperator.ValueOperator) materialized).getRawValue(), ValueTypeString.ValueString.of("abcd"))), is(false));
    }

    // --------------- Complex expressions ---------------

    private static ExpressionNode opConst(IOperator operator) {
        return new ExpressionNode.Const(ValueTypeOperator.ValueOperator.of(operator));
    }

    private static IValue i(int value) {
        return ValueTypeInteger.ValueInteger.of(value);
    }

    // x -> x * 2
    private static ExpressionOperator doubler() {
        return ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_MULTIPLICATION, p(0), c(2)),
                List.of(param("x", ValueTypes.INTEGER))));
    }

    @Test
    public void testThreeParameters() throws EvaluationException {
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, p(0), p(1)),
                op(Operators.ARITHMETIC_MULTIPLICATION, p(0), p(2)),
                op(Operators.ARITHMETIC_SUBTRACTION, p(1), p(2))),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER), param("c", ValueTypes.INTEGER))));
        assertThat(ExpressionPrinter.print(operator.getExpression()), is("(a, b, c) => a > b ? a * c : b - c"));
        assertThat(operator.getRequiredInputLength(), is(3));
        assertThat(operator.getOutputType(), is((IValueType) ValueTypes.INTEGER));
        assertThat(integer(eval(operator, i(5), i(2), i(10))), is(50));
        assertThat(integer(eval(operator, i(1), i(2), i(10))), is(-8));

        // Partial application, one argument at a time
        IOperator curried1 = ((ValueTypeOperator.ValueOperator) eval(operator, i(5))).getRawValue();
        assertThat(curried1.getRequiredInputLength(), is(2));
        IOperator curried2 = ((ValueTypeOperator.ValueOperator) eval(curried1, i(2))).getRawValue();
        assertThat(integer(eval(curried2, i(10))), is(50));

        // apply_2 with the first two arguments, then apply the last one
        IValue applied = eval(Operators.OPERATOR_APPLY_2, ValueTypeOperator.ValueOperator.of(operator), i(1), i(2));
        assertThat(integer(eval(((ValueTypeOperator.ValueOperator) applied).getRawValue(), i(10))), is(-8));

        // flip swaps a and b
        IOperator flipped = ((ValueTypeOperator.ValueOperator) eval(Operators.OPERATOR_FLIP, ValueTypeOperator.ValueOperator.of(operator))).getRawValue();
        assertThat(integer(eval(flipped, i(2), i(5), i(10))), is(50));
    }

    @Test
    public void testUnusedParameter() throws EvaluationException {
        // (a, b) -> a + 1
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_ADDITION, p(0), c(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.CATEGORY_ANY))));
        assertThat(integer(eval(operator, i(1), ValueTypeString.ValueString.of("ignored"))), is(2));
    }

    @Test
    public void testHigherOrderParameter() throws EvaluationException {
        // (f, x) -> apply(f, apply(f, x))
        ExpressionOperator twice = ExpressionOperator.of(new Expression(
                op(Operators.OPERATOR_APPLY, p(0), op(Operators.OPERATOR_APPLY, p(0), p(1))),
                List.of(param("f", ValueTypes.OPERATOR), param("x", ValueTypes.CATEGORY_ANY))));
        assertThat(integer(eval(twice, ValueTypeOperator.ValueOperator.of(doubler()), i(3))), is(12));
        // Also works with a regular operator
        assertThat(integer(eval(twice, ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_INCREMENT), i(3))), is(5));
        // And twice applied to itself through currying: twice(twice(double)) multiplies by 16
        IOperator twiceDouble = ((ValueTypeOperator.ValueOperator) eval(twice, ValueTypeOperator.ValueOperator.of(doubler()))).getRawValue();
        assertThat(integer(eval(twice, ValueTypeOperator.ValueOperator.of(twiceDouble), i(1))), is(16));
    }

    @Test
    public void testExpressionOperatorAsConstant() throws EvaluationException {
        // l -> reduce(+, map(double, l), 0), with operators as inline constants
        ExpressionOperator operator = ExpressionOperator.of(new Expression(
                op(Operators.OPERATOR_REDUCE, opConst(Operators.ARITHMETIC_ADDITION), op(Operators.OPERATOR_MAP, opConst(doubler()), p(0)), c(0)),
                List.of(param("l", ValueTypes.LIST))));
        assertThat(integer(eval(operator, ints(1, 2, 3))), is(12));

        // Survives serialization, including the nested expression operator constant
        CompoundTag tag = ValueHelpers.serialize(ValueDeseralizationContextMocked.get(), ValueTypeOperator.ValueOperator.of(operator));
        IOperator read = ((ValueTypeOperator.ValueOperator) ValueHelpers.deserialize(ValueDeseralizationContextMocked.get(), tag)).getRawValue();
        assertThat(integer(eval(read, ints(1, 2, 3))), is(12));
        assertThat(read, is((IOperator) operator));
    }

    @Test
    public void testPartialApplicationAsInlineLambda() throws EvaluationException {
        // l -> map(apply((*), 2), l), doubling without a second card
        Expression expression = new Expression(op(Operators.OPERATOR_MAP,
                op(Operators.OPERATOR_APPLY, opConst(Operators.ARITHMETIC_MULTIPLICATION), c(2)), p(0)),
                List.of(param("l", ValueTypes.LIST)));
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, id -> null);
        assertThat(result.isValid(), is(true));
        assertThat(result.outputType(), is((IValueType) ValueTypes.LIST));
        assertThat(ExpressionPrinter.print(expression), is("l => map(apply((*), 2), l)"));
        assertThat(eval(ExpressionOperator.of(expression), ints(1, 2, 3)), is((IValue) ints(2, 4, 6)));
    }

    @Test
    public void testExpressionOperatorsAsRefs() throws EvaluationException {
        // (l) -> reduce(#add, map(#double, l), 0), where both refs are expression operators
        ExpressionOperator add = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_ADDITION, p(0), p(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER))));
        Int2ObjectMap<IVariable<?>> refs = new Int2ObjectOpenHashMap<>();
        refs.put(1, new Variable<>(ValueTypeOperator.ValueOperator.of(add)));
        refs.put(2, new Variable<>(ValueTypeOperator.ValueOperator.of(doubler())));
        ExpressionOperator operator = ExpressionOperator.of(new Expression(
                op(Operators.OPERATOR_REDUCE, ref(1), op(Operators.OPERATOR_MAP, ref(2), p(0)), c(0)),
                List.of(param("l", ValueTypes.LIST))), refs);
        assertThat(integer(eval(operator, ints(1, 2, 3))), is(12));
        assertThat(ExpressionPrinter.print(operator.getExpression()), is("l => reduce(#1, map(#2, l), 0)"));

        // Materializing keeps working when the refs are themselves expression operators
        IOperator materialized = operator.materialize();
        assertThat(integer(eval(materialized, ints(1, 2, 3))), is(12));
    }

    @Test
    public void testNestedMaterialization() throws EvaluationException {
        // Inner: x -> x + #limit, outer: l -> map(#inner, l)
        MutableVariable<ValueTypeInteger.ValueInteger> limit = new MutableVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(1));
        Int2ObjectMap<IVariable<?>> innerRefs = new Int2ObjectOpenHashMap<>();
        innerRefs.put(5, limit);
        ExpressionOperator inner = ExpressionOperator.of(new Expression(op(Operators.ARITHMETIC_ADDITION, p(0), ref(5)),
                List.of(param("x", ValueTypes.INTEGER))), innerRefs);
        Int2ObjectMap<IVariable<?>> outerRefs = new Int2ObjectOpenHashMap<>();
        outerRefs.put(6, new Variable<>(ValueTypeOperator.ValueOperator.of(inner)));
        ExpressionOperator outer = ExpressionOperator.of(new Expression(op(Operators.OPERATOR_MAP, ref(6), p(0)),
                List.of(param("l", ValueTypes.LIST))), outerRefs);

        IOperator materialized = outer.materialize();
        limit.set(ValueTypeInteger.ValueInteger.of(100));
        // The live operator follows the reference, the materialized one keeps the old value at every level
        assertThat(eval(outer, ints(1, 2)), is((IValue) ints(101, 102)));
        assertThat(eval(materialized, ints(1, 2)), is((IValue) ints(2, 3)));
    }

    @Test
    public void testDeepParameterUse() throws EvaluationException {
        // x -> (((x + 1) + 1) + 1)... 30 levels deep
        ExpressionNode node = p(0);
        for (int j = 0; j < 30; j++) {
            node = op(Operators.ARITHMETIC_ADDITION, node, c(1));
        }
        ExpressionOperator operator = ExpressionOperator.of(new Expression(node, List.of(param("x", ValueTypes.INTEGER))));
        assertThat(integer(eval(operator, i(0))), is(30));
        assertThat(operator.getOutputType(), is((IValueType) ValueTypes.INTEGER));
    }

    @Test
    public void testParameterInSeveralSubtrees() throws EvaluationException {
        // (s, n) -> len(s) * n == n * len(s) && len(s) > n
        ExpressionNode lenS = op(Operators.STRING_LENGTH, p(0));
        ExpressionOperator operator = ExpressionOperator.of(new Expression(op(Operators.LOGICAL_AND,
                op(Operators.RELATIONAL_EQUALS, op(Operators.ARITHMETIC_MULTIPLICATION, lenS, p(1)), op(Operators.ARITHMETIC_MULTIPLICATION, p(1), lenS)),
                op(Operators.RELATIONAL_GT, lenS, p(1))),
                List.of(param("s", ValueTypes.STRING), param("n", ValueTypes.INTEGER))));
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("abcd"), i(3))), is(true));
        assertThat(bool(eval(operator, ValueTypeString.ValueString.of("ab"), i(3))), is(false));
        assertThat(ExpressionPrinter.print(operator.getExpression()), is("(s, n) => ((s.length() * n) == (n * s.length())) && (s.length() > n)"));
    }

    @Test
    public void testEvaluationErrorInNestedNode() {
        // x -> 10 / (x - x)
        ExpressionOperator operator = ExpressionOperator.of(new Expression(
                op(Operators.ARITHMETIC_DIVISION, c(10), op(Operators.ARITHMETIC_SUBTRACTION, p(0), p(0))),
                List.of(param("x", ValueTypes.INTEGER))));
        try {
            eval(operator, i(3));
            fail("Expected division by zero");
        } catch (EvaluationException e) {
            // Expected
        }
    }

    @Test
    public void testFilterWithExpressionUsingRef() throws EvaluationException {
        // l -> filter(#pred, l), with #pred = s -> len(s) > #limit, all through refs
        MutableVariable<ValueTypeInteger.ValueInteger> limit = new MutableVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        Int2ObjectMap<IVariable<?>> predRefs = new Int2ObjectOpenHashMap<>();
        predRefs.put(1, limit);
        ExpressionOperator pred = ExpressionOperator.of(new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), ref(1)),
                List.of(param("s", ValueTypes.STRING))), predRefs);

        // Parameterless lazy tree: filter(#pred, #list)
        Variable<ValueTypeList.ValueList> list = new Variable<>(strings("a", "abcd", "abcdef"));
        IVariable<?> tree = ExpressionEvaluator.buildLazy(op(Operators.LIST_LENGTH, op(Operators.OPERATOR_FILTER, ref(2), ref(3))),
                9, new ExpressionEvaluator.LocalValueCache(), id -> id == 2 ? new Variable<>(ValueTypeOperator.ValueOperator.of(pred)) : list);
        assertThat(integer(tree.getValue()), is(2));
    }

    // --------------- Type checking ---------------

    @Test
    public void testTypeCheckValid() {
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(longString(), id -> null);
        assertThat(result.isValid(), is(true));
        assertThat(result.outputType(), is((IValueType) ValueTypes.BOOLEAN));
    }

    @Test
    public void testTypeCheckEmpty() {
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, ExpressionNode.EMPTY), ExpressionNode.EMPTY));
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, id -> null);
        assertThat(result.errors().isEmpty(), is(true));
        assertThat(result.emptySlots(), is(2));
        assertThat(result.isValid(), is(false));
    }

    @Test
    public void testTypeCheckErrorPosition() {
        // length(s) > "abc": the string is in slot 1 of the root
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), c("abc")),
                List.of(param("s", ValueTypes.STRING)));
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, id -> null);
        assertThat(result.errors().size(), is(1));
        ExpressionTypeChecker.Error error = result.errors().get(0);
        assertThat(error.path(), is(new int[0]));
        assertThat(error.slot(), is(1));
        assertThat(error.expected(), is((IValueType) ValueTypes.CATEGORY_NUMBER));
        assertThat(error.actual(), is((IValueType) ValueTypes.STRING));
        assertThat(result.getError(new int[0], 1), notNullValue());
    }

    @Test
    public void testTypeCheckErrorMessages() {
        // length(s) > "abc" && 5: the string in slot 1 of >, and the integer in slot 1 of &&
        Expression expression = new Expression(op(Operators.LOGICAL_AND,
                op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, p(0)), c("abc")), c(5)),
                List.of(param("s", ValueTypes.STRING)));
        ExpressionNode root = expression.root();
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, id -> null);
        assertThat(result.errors().size(), is(2));
        // A value shows the error of its slot, and an operator its own errors
        assertThat(result.getErrorMessage(root, new int[]{0, 1}), is(result.getError(new int[]{0}, 1).message()));
        assertThat(result.getErrorMessage(root, new int[]{1}), is(result.getError(new int[0], 1).message()));
        assertThat(result.getErrorMessage(root, new int[]{0}), is(result.getError(new int[]{0}, 1).message()));
        assertThat(result.getErrorMessage(root, new int[0]), is(result.getError(new int[0], 1).message()));
        // Nodes without errors, also a value in a slot without one
        assertThat(result.getErrorMessage(root, new int[]{0, 0}), nullValue());
        assertThat(result.getErrorMessage(root, new int[]{0, 0, 0}), nullValue());

        // An operator with an error in each slot shows both, one per line
        Expression twice = new Expression(op(Operators.RELATIONAL_GT, c("a"), c("b")));
        ExpressionTypeChecker.Result twiceResult = ExpressionTypeChecker.check(twice, id -> null);
        assertThat(twiceResult.errors().size(), is(2));
        assertThat(twiceResult.getErrorMessage(twice.root(), new int[0]).getString().split("\n").length, is(2));
        assertThat(twiceResult.getErrorMessage(twice.root(), new int[]{1}), is(twiceResult.getError(new int[0], 1).message()));
    }

    @Test
    public void testTypeCheckNestedErrorPosition() {
        // length(5) > 3: error in slot 0 of node [0]
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, c(5)), c(3)));
        ExpressionTypeChecker.Result result = ExpressionTypeChecker.check(expression, id -> null);
        assertThat(result.errors().size(), is(1));
        assertThat(result.errors().get(0).path(), is(new int[]{0}));
        assertThat(result.errors().get(0).slot(), is(0));
        assertThat(result.hasErrorWithin(new int[0]), is(true));
        assertThat(result.hasErrorWithin(new int[]{0}), is(true));
        assertThat(result.hasErrorWithin(new int[]{1}), is(false));
    }

    @Test
    public void testTypeCheckRefTypes() {
        Expression expression = new Expression(op(Operators.STRING_LENGTH, ref(3)));
        assertThat(ExpressionTypeChecker.check(expression, id -> ValueTypes.STRING).errors().isEmpty(), is(true));
        assertThat(ExpressionTypeChecker.check(expression, id -> ValueTypes.BOOLEAN).errors().size(), is(1));
        // Unknown refs are not an error
        assertThat(ExpressionTypeChecker.check(expression, id -> null).errors().isEmpty(), is(true));
    }

    @Test
    public void testTypeCheckAnyParam() {
        Expression expression = new Expression(op(Operators.STRING_LENGTH, p(0)), List.of(param("x", ValueTypes.CATEGORY_ANY)));
        assertThat(ExpressionTypeChecker.check(expression, id -> null).errors().isEmpty(), is(true));
    }

    @Test
    public void testTypeCheckConditionalOutput() {
        // choice(true, "a", "b") is a string
        Expression expression = new Expression(op(Operators.GENERAL_CHOICE,
                new ExpressionNode.Const(ValueTypeBoolean.ValueBoolean.of(true)), c("a"), c("b")));
        assertThat(ExpressionTypeChecker.check(expression, id -> null).outputType(), is((IValueType) ValueTypes.STRING));
    }

    @Test
    public void testFacadeOutputTypeWithReferenceTypes() {
        // a -> a + #5, written while #5 was an Integer card
        Expression expression = new Expression(op(Operators.ARITHMETIC_ADDITION, p(0), ref(5)), List.of(param("a", ValueTypes.INTEGER)));
        ExpressionVariableFacade facade = new ExpressionVariableFacade(1, expression, Map.of(5, ValueTypes.INTEGER), null);
        assertThat(facade.getKnownRootOutputType(), is(ValueTypes.INTEGER));
        // Networks check the actual variables against the type without references
        assertThat(facade.getRootOutputType(), is(ValueTypes.CATEGORY_ANY));
        assertThat(new ExpressionVariableFacade(1, expression, Map.of(), null).getKnownRootOutputType(), is(ValueTypes.CATEGORY_ANY));
    }

    // --------------- Printing ---------------

    @Test
    public void testPrint() {
        assertThat(ExpressionPrinter.print(longString()), is("s => s.length() > 3"));
    }

    @Test
    public void testPrintEmptyAndRefs() {
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, ExpressionNode.EMPTY), ref(42)));
        // length is shared by strings and lists, and an empty slot has no type to tell them apart
        assertThat(ExpressionPrinter.print(expression), is("length(?) > #42"));
        assertThat(ExpressionPrinter.print(expression, id -> id == 42 ? "limit" : null), is("length(?) > #limit"));
    }

    @Test
    public void testPrintNestedInfix() {
        Expression expression = new Expression(op(Operators.ARITHMETIC_MULTIPLICATION,
                op(Operators.ARITHMETIC_ADDITION, p(0), c(1)), p(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER)));
        assertThat(ExpressionPrinter.print(expression), is("(a, b) => (a + 1) * b"));
    }

    @Test
    public void testPrintCallAndConst() {
        Expression expression = new Expression(op(Operators.ARITHMETIC_INCREMENT, c("x")));
        assertThat(ExpressionPrinter.print(expression), is("\"x\".increment()"));
    }

    @Test
    public void testPrintSharedNameByType() {
        // size is shared by item stacks and NBT, the parameter type tells them apart
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.NBT_COMPOUND_SIZE, p(0)), c(3)),
                List.of(param("tag", ValueTypes.NBT)));
        assertThat(ExpressionPrinter.print(expression), is("tag => tag.size() > 3"));
    }

    @Test
    public void testPrintChoice() {
        Expression expression = new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, p(0), p(1)),
                op(Operators.ARITHMETIC_MULTIPLICATION, p(0), p(2)),
                op(Operators.GENERAL_CHOICE, op(Operators.RELATIONAL_LT, p(1), p(2)), p(1), p(2))),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER), param("c", ValueTypes.INTEGER)));
        assertThat(ExpressionPrinter.print(expression), is("(a, b, c) => a > b ? a * c : (b < c ? b : c)"));

        // Parenthesized inside other operators
        Expression inInfix = new Expression(op(Operators.ARITHMETIC_ADDITION, op(Operators.GENERAL_CHOICE, p(0), c(1), c(2)), c(3)),
                List.of(param("x", ValueTypes.BOOLEAN)));
        assertThat(ExpressionPrinter.print(inInfix), is("x => (x ? 1 : 2) + 3"));

        // Incomplete choices are printed in prefix notation
        Expression incomplete = new Expression(op(Operators.GENERAL_CHOICE, p(0), ExpressionNode.EMPTY, c(2)),
                List.of(param("x", ValueTypes.BOOLEAN)));
        assertThat(ExpressionPrinter.print(incomplete), is("x => choice(x, ?, 2)"));
        Expression incompleteCondition = new Expression(op(Operators.GENERAL_CHOICE,
                op(Operators.RELATIONAL_GT, ExpressionNode.EMPTY, c(1)), c(1), c(2)));
        assertThat(ExpressionPrinter.print(incompleteCondition), is("choice(? > 1, 1, 2)"));
    }

    @Test
    public void testPrintOperatorValues() {
        Expression expression = new Expression(op(Operators.OPERATOR_REDUCE, opConst(Operators.ARITHMETIC_ADDITION),
                op(Operators.OPERATOR_MAP, opConst(Operators.STRING_LENGTH), p(0)), c(0)),
                List.of(param("l", ValueTypes.LIST)));
        // Operator values have no argument to tell shared names apart
        assertThat(ExpressionPrinter.print(expression), is("l => reduce((+), map((length), l), 0)"));
        // Unregistered operators are printed by their full symbol
        assertThat(ExpressionPrinter.printValue(ValueTypeOperator.ValueOperator.of(doubler())), is("(x => x * 2)"));
    }

    @Test
    public void testPrintCallsInsteadOfSymbols() {
        // =NBT= and NBT{}.get_integer
        Expression items = new Expression(op(Operators.LOGICAL_AND,
                op(Operators.OBJECT_ITEMSTACK_ISDATAEQUAL, p(0), p(0)),
                op(Operators.RELATIONAL_EQUALS, op(Operators.NBT_COMPOUND_VALUE_INTEGER, op(Operators.OBJECT_ITEMSTACK_DATA, p(0)), c("k")), c(1))),
                List.of(param("item", ValueTypes.OBJECT_ITEMSTACK)));
        assertThat(ExpressionPrinter.print(items), is("item => item.isNbtEqual(item) && (item.nbt().getInteger(\"k\") == 1)"));
        // String + only when it is read as concat
        assertThat(ExpressionPrinter.print(new Expression(op(Operators.STRING_CONCAT, c("a"), c("b")))), is("\"a\" + \"b\""));
        assertThat(ExpressionPrinter.print(new Expression(op(Operators.STRING_CONCAT, p(0), p(0)), List.of(param("x", ValueTypes.CATEGORY_ANY)))),
                is("x => x.concat(x)"));
        // ∩
        Expression lists = new Expression(op(Operators.LIST_INTERSECTION, p(0), p(0)), List.of(param("l", ValueTypes.LIST)));
        assertThat(ExpressionPrinter.print(lists), is("l => l.intersection(l)"));
        // !
        Expression not = new Expression(op(Operators.LOGICAL_NOT, op(Operators.LOGICAL_AND, p(0), op(Operators.LOGICAL_NOT, p(0)))),
                List.of(param("x", ValueTypes.BOOLEAN)));
        assertThat(ExpressionPrinter.print(not), is("x => !(x && !x)"));
    }

    @Test
    public void testPrintWithRefTypes() {
        Expression expression = new Expression(op(Operators.LIST_LENGTH, ref(5)));
        assertThat(ExpressionPrinter.print(expression), is("listLength(#5)"));
        assertThat(ExpressionPrinter.print(expression, id -> null, id -> id == 5 ? ValueTypes.LIST : null), is("#5.length()"));
    }

    @Test
    public void testTokensHavePaths() {
        List<ExpressionPrinter.Token> tokens = ExpressionPrinter.tokenize(longString(), id -> null,
                ExpressionTypeChecker.check(longString(), id -> null));
        boolean found = false;
        for (ExpressionPrinter.Token token : tokens) {
            if (token.text().equals("3")) {
                assertThat(token.path(), is(new int[]{1}));
                found = true;
            }
        }
        if (!found) {
            fail("Token not found");
        }
    }

    public static class MutableVariable<V extends IValue> extends VariableAdapter<V> {
        private final IValueType<V> type;
        private V value;

        public MutableVariable(IValueType<V> type, V value) {
            this.type = type;
            this.value = value;
        }

        public void set(V value) {
            this.value = value;
            invalidate();
        }

        @Override
        public IValueType<V> getType() {
            return type;
        }

        @Override
        public V getValue() {
            return value;
        }
    }

}
