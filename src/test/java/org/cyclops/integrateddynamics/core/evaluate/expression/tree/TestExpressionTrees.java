package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.write(tag, new java.io.DataOutputStream(out));
            return out.size();
        } catch (java.io.IOException e) {
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

    // --------------- Printing ---------------

    @Test
    public void testPrint() {
        assertThat(ExpressionPrinter.print(longString()), is("s -> len(s) > 3"));
    }

    @Test
    public void testPrintEmptyAndRefs() {
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.STRING_LENGTH, ExpressionNode.EMPTY), ref(42)));
        assertThat(ExpressionPrinter.print(expression), is("len(?) > #42"));
        assertThat(ExpressionPrinter.print(expression, id -> id == 42 ? "limit" : null), is("len(?) > #limit"));
    }

    @Test
    public void testPrintNestedInfix() {
        Expression expression = new Expression(op(Operators.ARITHMETIC_MULTIPLICATION,
                op(Operators.ARITHMETIC_ADDITION, p(0), c(1)), p(1)),
                List.of(param("a", ValueTypes.INTEGER), param("b", ValueTypes.INTEGER)));
        assertThat(ExpressionPrinter.print(expression), is("(a, b) -> (a + 1) * b"));
    }

    @Test
    public void testPrintSuffixAndConst() {
        Expression expression = new Expression(op(Operators.ARITHMETIC_INCREMENT, c("x")));
        assertThat(ExpressionPrinter.print(expression), is("\"x\"++"));
    }

    @Test
    public void testPrintWordySuffix() {
        // Suffix operators with word-like symbols are printed in prefix notation
        Expression expression = new Expression(op(Operators.RELATIONAL_GT, op(Operators.NBT_COMPOUND_SIZE, p(0)), c(3)),
                List.of(param("tag", ValueTypes.NBT)));
        assertThat(ExpressionPrinter.print(expression), is("tag -> size(tag) > 3"));
    }

    @Test
    public void testShortNames() {
        assertThat(ExpressionPrinter.getShortName(Operators.RELATIONAL_GT), is(">"));
        assertThat(ExpressionPrinter.getShortName(Operators.LIST_LENGTH), is("| |"));
        assertThat(ExpressionPrinter.getShortName(Operators.NBT_COMPOUND_SIZE), is("size"));
        assertThat(ExpressionPrinter.getShortName(Operators.OBJECT_ITEMSTACK_DATA), is("nbt"));
    }

    @Test
    public void testTokensHavePaths() {
        List<ExpressionPrinter.Token> tokens = ExpressionPrinter.tokenize(longString(), id -> null);
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
