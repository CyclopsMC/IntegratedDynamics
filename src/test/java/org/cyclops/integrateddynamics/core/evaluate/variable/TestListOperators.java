package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the different integer operators.
 *
 * @author rubensworks
 */
public class TestListOperators {

    static {
        CyclopsCoreInstance.MOD = new ModBaseMocked();
    }

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableList labc;
    private DummyVariableList lintegers;
    private DummyVariableList lintegers_012;
    private DummyVariableList lempty;
    private DummyVariableList lintegers_dup;
    private DummyVariableList lintegers_rev_dup;
    private DummyVariableList llongs_hash_collision;
    private DummyVariableList lintegers_inf;

    private DummyVariableInteger im1;
    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i3;
    private DummyVariableInteger i4;
    private DummyVariableInteger i5;

    private DummyVariableString sx;

    private DummyVariableOperator oRelationalEquals;
    private DummyVariableOperator oArithmeticIncrement;

    @BeforeAll
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    @BeforeEach
    public void before() {
        im1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1));
        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));
        i3 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3));
        i4 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(4));
        i5 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(5));

        sx = new DummyVariableString(ValueTypeString.ValueString.of("x"));

        oRelationalEquals = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.RELATIONAL_EQUALS));
        oArithmeticIncrement = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_INCREMENT));

        labc = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeString.ValueString.of("a"),
                ValueTypeString.ValueString.of("b"),
                ValueTypeString.ValueString.of("c")
        ));
        lintegers = new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(), i3.getValue()));
        lintegers_012 = new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue()));
        lempty = new DummyVariableList(ValueTypeList.ValueList.ofAll());
        lintegers_dup = new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(),
                i3.getValue(), i1.getValue(), i2.getValue(), i3.getValue(), i2.getValue(), i3.getValue(), i3.getValue()));
        lintegers_rev_dup = new DummyVariableList(ValueTypeList.ValueList.ofAll(i3.getValue(), i2.getValue(), i1.getValue(),
                i0.getValue(), i2.getValue(), i1.getValue(), i0.getValue(), i1.getValue(), i0.getValue(), i0.getValue()));
        llongs_hash_collision = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeLong.ValueLong.of(0xAAAAAAAA12345678L),
                ValueTypeLong.ValueLong.of(0x3333333312345678L),
                ValueTypeLong.ValueLong.of(0x12345678AAAAAAAAL),
                ValueTypeLong.ValueLong.of(0x1234567833333333L)
        ));
        lintegers_inf = new DummyVariableList(ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyLazyBuilt<>(
                ValueTypeInteger.ValueInteger.of(0), Operators.ARITHMETIC_INCREMENT)));
    }

    /**
     * ----------------------------------- LENGTH -----------------------------------
     */

    @Test
    public void testListLength() throws EvaluationException {
        IValue res1 = Operators.LIST_LENGTH.evaluate(new IVariable[]{labc});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("len(abc) = 3", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(3));
    }

    @Test
    public void testInvalidInputSizeLengthLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LENGTH.evaluate(new IVariable[]{labc, labc}));
    }

    @Test
    public void testInvalidInputSizeLengthSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LENGTH.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeLength() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LENGTH.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- EMPTY -----------------------------------
     */

    @Test
    public void testListEmpty() throws EvaluationException {
        IValue res1 = Operators.LIST_EMPTY.evaluate(new IVariable[]{labc});
        assertThat("result is an boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("empty(abc) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.LIST_EMPTY.evaluate(new IVariable[]{lempty});
        assertThat("empty(empty) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test
    public void testInvalidInputSizeEmptyLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EMPTY.evaluate(new IVariable[]{labc, labc}));
    }

    @Test
    public void testInvalidInputSizeEmptySmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EMPTY.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeEmpty() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EMPTY.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- NOT_EMPTY -----------------------------------
     */

    @Test
    public void testListNotEmpty() throws EvaluationException {
        IValue res1 = Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{labc});
        assertThat("result is an boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("empty(abc) = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{lempty});
        assertThat("empty(empty) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test
    public void testInvalidInputSizeNotEmptyLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{labc, labc}));
    }

    @Test
    public void testInvalidInputSizeNotEmptySmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeNotEmpty() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- GET -----------------------------------
     */

    @Test
    public void testListElement() throws EvaluationException {
        IValue res1 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0))});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 0) = 'a'", ((ValueTypeString.ValueString) res1).getRawValue(), is("a"));

        IValue res2 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1))});
        assertThat("result is a string", res2, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 1) = 'b'", ((ValueTypeString.ValueString) res2).getRawValue(), is("b"));

        IValue res3 = Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2))});
        assertThat("result is a string", res3, instanceOf(ValueTypeString.ValueString.class));
        assertThat("get(abc, 2) = 'c'", ((ValueTypeString.ValueString) res3).getRawValue(), is("c"));
    }

    @Test
    public void testListElementIndexOutOfBounds() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3))}));
    }

    @Test
    public void testInvalidInputSizeElementLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, labc, labc}));
    }

    @Test
    public void testInvalidInputSizeElementSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc}));
    }

    @Test
    public void testInvalidInputTypeElement() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- GET_OR_DEFAULT -----------------------------------
     */

    @Test
    public void testListElementOrDefault() throws EvaluationException {
        IValue res1 = Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0)), sx});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("getOrDefault(abc, x, 0) = 'a'", ((ValueTypeString.ValueString) res1).getRawValue(), is("a"));

        IValue res2 = Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1)), sx});
        assertThat("result is a string", res2, instanceOf(ValueTypeString.ValueString.class));
        assertThat("getOrDefault(abc, x, 1) = 'b'", ((ValueTypeString.ValueString) res2).getRawValue(), is("b"));

        IValue res3 = Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2)), sx});
        assertThat("result is a string", res3, instanceOf(ValueTypeString.ValueString.class));
        assertThat("getOrDefault(abc, x, 2) = 'c'", ((ValueTypeString.ValueString) res3).getRawValue(), is("c"));
    }

    @Test
    public void testListElementOrDefaultIndexOutOfBounds() throws EvaluationException {
        IValue res1 = Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3)), sx});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("getOrDefault(abc, x, 3) = 'x'", ((ValueTypeString.ValueString) res1).getRawValue(), is("x"));

        IValue res2 = Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-1)), sx});
        assertThat("getOrDefault(abc, x, -1) = 'x'", ((ValueTypeString.ValueString) res2).getRawValue(), is("x"));
    }

    @Test
    public void testInvalidInputSizeElementOrDefaultLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, i0, sx, sx}));
    }

    @Test
    public void testInvalidInputSizeElementOrDefaultSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{labc, i0}));
    }

    @Test
    public void testInvalidInputTypeElementOrDefault() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_ELEMENT_DEFAULT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- CONTAINS -----------------------------------
     */

    @Test
    public void testListContains() throws EvaluationException {
        IValue res1 = Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("contains([0, 1, 2, 3], 0) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i1});
        assertThat("contains([0, 1, 2, 3, 1) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i2});
        assertThat("contains([0, 1, 2, 3], 2) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i3});
        assertThat("contains([0, 1, 2, 3], 3) = true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));

        IValue res5 = Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i4});
        assertThat("contains([0, 1, 2, 3], 4) = false", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test
    public void testInvalidInputSizeContainsLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i2, i0}));
    }

    @Test
    public void testInvalidInputSizeContainsSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeContains() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- CONTAINS_PREDICATE -----------------------------------
     */

    @Test
    public void testListContainsPredicate() throws EvaluationException {
        DummyVariableOperator equals0 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i0}));
        DummyVariableOperator equals1 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i1}));
        DummyVariableOperator equals2 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));
        DummyVariableOperator equals3 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i3}));
        DummyVariableOperator equals4 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i4}));

        IValue res1 = Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, equals0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("containspredicate([0, 1, 2, 3], 0==) = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, equals1});
        assertThat("containspredicate([0, 1, 2, 3], 1==) = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, equals2});
        assertThat("containspredicate([0, 1, 2, 3], 2==) = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, equals3});
        assertThat("containspredicate([0, 1, 2, 3], 3==) = true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));

        IValue res5 = Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, equals4});
        assertThat("containspredicate([0, 1, 2, 3], 4==) = false", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test
    public void testInvalidInputSizeContainsPredicateLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, oRelationalEquals, i2}));
    }

    @Test
    public void testInvalidInputSizeContainsPredicateSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeContainsPredicate() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- COUNT -----------------------------------
     */

    @Test
    public void testListCount() throws EvaluationException {
        IValue res1 = Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_dup, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("count([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 0) = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_dup, i1});
        assertThat("count([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 1) = 2", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(2));

        IValue res3 = Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_dup, i2});
        assertThat("count([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 2) = 3", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(3));

        IValue res4 = Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_dup, i3});
        assertThat("count([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 3) = 4", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(4));

        IValue res5 = Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_dup, i4});
        assertThat("count([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 4) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test
    public void testInvalidInputSizeCountLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers, i2, i0}));
    }

    @Test
    public void testInvalidInputSizeCountSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeCount() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    @Test
    public void testListCountInfinite() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers_inf, i0}));
    }

    /**
     * ----------------------------------- COUNT_PREDICATE -----------------------------------
     */

    @Test
    public void testListCountPredicate() throws EvaluationException {
        DummyVariableOperator equals0 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i0}));
        DummyVariableOperator equals1 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i1}));
        DummyVariableOperator equals2 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));
        DummyVariableOperator equals3 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i3}));
        DummyVariableOperator equals4 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i4}));

        IValue res1 = Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_dup, equals0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("countpredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 0) = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_dup, equals1});
        assertThat("countpredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 1) = 2", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(2));

        IValue res3 = Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_dup, equals2});
        assertThat("countpredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 2) = 3", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(3));

        IValue res4 = Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_dup, equals3});
        assertThat("countpredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 3) = 4", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(4));

        IValue res5 = Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_dup, equals4});
        assertThat("countpredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], 4) = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));
    }

    @Test
    public void testInvalidInputSizeCountPredicateLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers, i2, i0}));
    }

    @Test
    public void testInvalidInputSizeCountPredicateSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeCountPredicate() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    @Test
    public void testListCountPredicateInfinite() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers_inf, i0}));
    }

    /**
     * ----------------------------------- APPEND -----------------------------------
     */

    @Test
    public void testListAppend() throws EvaluationException {
        IValue res1 = Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers_012, i3});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("append([0, 1, 2], 3)[0] = 0", list.get(0).getRawValue(), is(0));
        assertThat("append([0, 1, 2], 3)[1] = 1", list.get(1).getRawValue(), is(1));
        assertThat("append([0, 1, 2], 3)[2] = 2", list.get(2).getRawValue(), is(2));
        assertThat("append([0, 1, 2], 3)[3] = 3", list.get(3).getRawValue(), is(3));
        assertThat("append([0, 1, 2], 3).size = 3", list.getLength(), is(4));
    }

    @Test
    public void testInvalidInputSizeAppendInvalidType() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers_012, oRelationalEquals}));
    }

    @Test
    public void testInvalidInputSizeAppendLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers, i2, i0}));
    }

    @Test
    public void testInvalidInputSizeAppendSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeAppend() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_APPEND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- CONCAT -----------------------------------
     */

    @Test
    public void testListConcat() throws EvaluationException {
        IValue res1 = Operators.LIST_CONCAT.evaluate(new IVariable[]{lintegers_012, lintegers});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[0] = 0", list.get(0).getRawValue(), is(0));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[1] = 1", list.get(1).getRawValue(), is(1));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[2] = 2", list.get(2).getRawValue(), is(2));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[3] = 0", list.get(3).getRawValue(), is(0));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[4] = 1", list.get(4).getRawValue(), is(1));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[5] = 2", list.get(5).getRawValue(), is(2));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3])[6] = 3", list.get(6).getRawValue(), is(3));
        assertThat("concat([0, 1, 2], [0, 1, 2, 3]).size = 7", list.getLength(), is(7));
    }

    @Test
    public void testInvalidInputSizeConcatInvalidType() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONCAT.evaluate(new IVariable[]{lintegers_012, oRelationalEquals}));
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONCAT.evaluate(new IVariable[]{oRelationalEquals, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeConcatLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONCAT.evaluate(new IVariable[]{lintegers, lintegers_012, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeConcatSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONCAT.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeConcat() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_CONCAT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- LAZYBUILT -----------------------------------
     */

    @Test
    public void testListLazyBuilt() throws EvaluationException {
        IValue res1 = Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{i3, oArithmeticIncrement});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("lazybuilt(3, ++)[0] = 3", list.get(0).getRawValue(), is(3));
        assertThat("lazybuilt(3, ++)[1] = 4", list.get(1).getRawValue(), is(4));
        assertThat("lazybuilt(3, ++)[5] = 8", list.get(5).getRawValue(), is(8));
        assertThat("lazybuilt(3, ++)[10] = 13", list.get(10).getRawValue(), is(13));
        assertThat("lazybuilt(3, ++)[100] = 103", list.get(100).getRawValue(), is(103));

        assertThat("lazybuilt([0, 1, 2], 3).size = 3", list.getLength(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testInvalidInputSizeLazyBuiltInvalidType() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{i3, oRelationalEquals}));
    }

    @Test
    public void testInvalidInputSizeLazyBuiltLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{lintegers, i2, i0}));
    }

    @Test
    public void testInvalidInputSizeLazyBuiltSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeLazyBuilt() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- HEAD -----------------------------------
     */

    @Test
    public void testListHead() throws EvaluationException {
        IValue res1 = Operators.LIST_HEAD.evaluate(new IVariable[]{labc});
        assertThat("result is a string", res1, instanceOf(ValueTypeString.ValueString.class));
        assertThat("head(abc) = 'a'", ((ValueTypeString.ValueString) res1).getRawValue(), is("a"));
    }

    @Test
    public void testInvalidInputSizeHeadLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_HEAD.evaluate(new IVariable[]{labc, labc}));
    }

    @Test
    public void testInvalidInputSizeHeadSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_HEAD.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeHead() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_HEAD.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- TAIL -----------------------------------
     */

    @Test
    public void testListTail() throws EvaluationException {
        IValue res1 = Operators.LIST_TAIL.evaluate(new IVariable[]{lintegers});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("tail([0, 1, 2, 3])[0] = 0", list.get(0).getRawValue(), is(1));
        assertThat("tail([0, 1, 2, 3])[1] = 1", list.get(1).getRawValue(), is(2));
        assertThat("tail([0, 1, 2, 3])[2] = 2", list.get(2).getRawValue(), is(3));
        assertThat("tail([0, 1, 2, 3]).size = 3", list.getLength(), is(3));
    }

    @Test
    public void testInvalidInputSizeTailLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_TAIL.evaluate(new IVariable[]{lintegers, i2}));
    }

    @Test
    public void testInvalidInputSizeTailSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_TAIL.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeTail() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_TAIL.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- UNIQ_PREDICATE -----------------------------------
     */

    @Test
    public void testListUniqPredicate() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{lintegers_dup, oRelationalEquals});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniqPredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], ==)[0] = 0", list.get(0).getRawValue(), is(0));
        assertThat("uniqPredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], ==)[1] = 1", list.get(1).getRawValue(), is(1));
        assertThat("uniqPredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], ==)[2] = 2", list.get(2).getRawValue(), is(2));
        assertThat("uniqPredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], ==)[3] = 3", list.get(3).getRawValue(), is(3));
        assertThat("uniqPredicate([0, 1, 2, 3, 1, 2, 3, 2, 3, 3], ==).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testListUniqPredicateOrder() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{lintegers_rev_dup, oRelationalEquals});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniqPredicate([3, 2, 1, 0, 2, 1, 0, 1, 0, 0], ==)[0] = 3", list.get(0).getRawValue(), is(3));
        assertThat("uniqPredicate([3, 2, 1, 0, 2, 1, 0, 1, 0, 0], ==)[1] = 2", list.get(1).getRawValue(), is(2));
        assertThat("uniqPredicate([3, 2, 1, 0, 2, 1, 0, 1, 0, 0], ==)[2] = 1", list.get(2).getRawValue(), is(1));
        assertThat("uniqPredicate([3, 2, 1, 0, 2, 1, 0, 1, 0, 0], ==)[3] = 0", list.get(3).getRawValue(), is(0));
        assertThat("uniqPredicate([3, 2, 1, 0, 2, 1, 0, 1, 0, 0], ==).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testListUniqPredicateHashCollision() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{llongs_hash_collision, oRelationalEquals});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeLong, ValueTypeLong.ValueLong> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniqPredicate([0xAAAAAAAA12345678L, 0x3333333312345678L, 0x12345678AAAAAAAAL, 0x1234567833333333L], ==).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testInvalidInputSizeUniqPredicateLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{lintegers, oRelationalEquals, i2}));
    }

    @Test
    public void testInvalidInputSizeUniqPredicateSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeUniqPredicate() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- UNIQ -----------------------------------
     */

    @Test
    public void testListUniq() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ.evaluate(new IVariable[]{lintegers_dup});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniq([0, 1, 2, 3, 1, 2, 3, 2, 3, 3])[0] = 0", list.get(0).getRawValue(), is(0));
        assertThat("uniq([0, 1, 2, 3, 1, 2, 3, 2, 3, 3])[1] = 1", list.get(1).getRawValue(), is(1));
        assertThat("uniq([0, 1, 2, 3, 1, 2, 3, 2, 3, 3])[2] = 2", list.get(2).getRawValue(), is(2));
        assertThat("uniq([0, 1, 2, 3, 1, 2, 3, 2, 3, 3])[3] = 3", list.get(3).getRawValue(), is(3));
        assertThat("uniq([0, 1, 2, 3, 1, 2, 3, 2, 3, 3]).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testListUniqOrder() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ.evaluate(new IVariable[]{lintegers_rev_dup});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniq([3, 2, 1, 0, 2, 1, 0, 1, 0, 0])[0] = 3", list.get(0).getRawValue(), is(3));
        assertThat("uniq([3, 2, 1, 0, 2, 1, 0, 1, 0, 0])[1] = 2", list.get(1).getRawValue(), is(2));
        assertThat("uniq([3, 2, 1, 0, 2, 1, 0, 1, 0, 0])[2] = 1", list.get(2).getRawValue(), is(1));
        assertThat("uniq([3, 2, 1, 0, 2, 1, 0, 1, 0, 0])[3] = 0", list.get(3).getRawValue(), is(0));
        assertThat("uniq([3, 2, 1, 0, 2, 1, 0, 1, 0, 0]).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testListUniqHashCollision() throws EvaluationException {
        IValue res1 = Operators.LIST_UNIQ.evaluate(new IVariable[]{llongs_hash_collision});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeLong, ValueTypeLong.ValueLong> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("uniq([0xAAAAAAAA12345678L, 0x3333333312345678L, 0x12345678AAAAAAAAL, 0x1234567833333333L]).size = 4", list.getLength(), is(4));
    }

    @Test
    public void testInvalidInputSizeUniqLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ.evaluate(new IVariable[]{lintegers, i2}));
    }

    @Test
    public void testInvalidInputSizeUniqSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ.evaluate(new IVariable[]{}));
    }

    @Test
    public void testInvalidInputTypeUniq() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_UNIQ.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- SLICE -----------------------------------
     */

    @Test
    public void testListSlice() throws EvaluationException {
        IValue res1 = Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i0, i4});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("slice([0, 1, 2, 3], 0, 4)[0] = 0", list.get(0).getRawValue(), is(0));
        assertThat("slice([0, 1, 2, 3], 0, 4)[1] = 1", list.get(1).getRawValue(), is(1));
        assertThat("slice([0, 1, 2, 3], 0, 4)[2] = 2", list.get(2).getRawValue(), is(2));
        assertThat("slice([0, 1, 2, 3], 0, 4)[3] = 3", list.get(3).getRawValue(), is(3));
        assertThat("slice([0, 1, 2, 3], 0, 4).size = 4", list.getLength(), is(4));

        IValue res2 = Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i1, i4});
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list2 = ((ValueTypeList.ValueList) res2).getRawValue();

        assertThat("slice([0, 1, 2, 3], 1, 4)[0] = 1", list2.get(0).getRawValue(), is(1));
        assertThat("slice([0, 1, 2, 3], 1, 4)[1] = 2", list2.get(1).getRawValue(), is(2));
        assertThat("slice([0, 1, 2, 3], 1, 4)[2] = 3", list2.get(2).getRawValue(), is(3));
        assertThat("slice([0, 1, 2, 3], 1, 4).size = 3", list2.getLength(), is(3));

        IValue res3 = Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i3, i5});
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list3 = ((ValueTypeList.ValueList) res3).getRawValue();

        assertThat("slice([0, 1, 2, 3], 3, 5)[0] = 3", list3.get(0).getRawValue(), is(3));
        assertThat("slice([0, 1, 2, 3], 3, 5).size = 1", list3.getLength(), is(1));
    }

    @Test
    public void testInvalidInputSizeSliceNegative1() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i0, im1}));
    }

    @Test
    public void testInvalidInputSizeSliceNegative2() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, im1, i1}));
    }

    @Test
    public void testInvalidInputSizeSliceToNotLargerThanFrom() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i1, i1}));
    }

    @Test
    public void testInvalidInputSizeSliceLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i2, i2, i2}));
    }

    @Test
    public void testInvalidInputSizeSliceSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{lintegers, i2}));
    }

    @Test
    public void testInvalidInputTypeSlice() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_SLICE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- INTERSECTION -----------------------------------
     */

    @Test
    public void testIntersection() throws EvaluationException {
        DummyVariableList list1 = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeString.ValueString.of("a"),
                ValueTypeString.ValueString.of("b"),
                ValueTypeString.ValueString.of("c")
        ));
        DummyVariableList list2 = new DummyVariableList(ValueTypeList.ValueList.ofAll(
                ValueTypeString.ValueString.of("d"),
                ValueTypeString.ValueString.of("c"),
                ValueTypeString.ValueString.of("b")
        ));
        IValue result = Operators.LIST_INTERSECTION.evaluate(new IVariable[]{list1, list2});
        IValueTypeListProxy<ValueTypeString, ValueTypeString.ValueString> resultValues = ((ValueTypeList.ValueList) result).getRawValue();
        assertThat(
                "len(a,b,c ∩ d,c,b) == 2",
                resultValues.getLength(),
                is(2)
        );
        assertThat(
                "(a,b,c ∩ d,c,b)[0]",
                resultValues.get(0).getRawValue(),
                is("b")
        );
        assertThat(
                "(a,b,c ∩ d,c,b)[1]",
                resultValues.get(1).getRawValue(),
                is("c")
        );
    }

    /**
     * ----------------------------------- EQUALS_SET -----------------------------------
     */

    @Test
    public void testListEqualsSet() throws EvaluationException {
        IValue res1 = Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i2.getValue(), i3.getValue(), i3.getValue(), i1.getValue())),
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(), i3.getValue()))
        });
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("result is true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
    }

    @Test
    public void testInvalidInputSizeEqualsSetInvalidType() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{lintegers_012, oRelationalEquals}));
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{oRelationalEquals, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeEqualsSetLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{lintegers, lintegers_012, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeEqualsSetSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeEqualsSet() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_SET.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }

    /**
     * ----------------------------------- EQUALS_MULTISET -----------------------------------
     */

    @Test
    public void testListEqualsMultiSet() throws EvaluationException {
        IValue res1 = Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i2.getValue(), i3.getValue(), i3.getValue(), i1.getValue())),
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(), i3.getValue()))
        });
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("result is true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i2.getValue(), i3.getValue(), i3.getValue(), i1.getValue())),
                new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(), i3.getValue(), i3.getValue()))
        });
        assertThat("result is true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test
    public void testInvalidInputSizeEqualsMultiSetInvalidType() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{lintegers_012, oRelationalEquals}));
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{oRelationalEquals, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeEqualsMultiSetLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{lintegers, lintegers_012, lintegers_012}));
    }

    @Test
    public void testInvalidInputSizeEqualsMultiSetSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{lintegers}));
    }

    @Test
    public void testInvalidInputTypeEqualsMultiSet() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.LIST_EQUALS_MULTISET.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }
}
