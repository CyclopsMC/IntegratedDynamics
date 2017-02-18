package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestListOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableList labc;
    private DummyVariableList lintegers;
    private DummyVariableList lintegers_012;
    private DummyVariableList lempty;
    private DummyVariableList lintegers_dup;

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i3;
    private DummyVariableInteger i4;

    private DummyVariableOperator oRelationalEquals;
    private DummyVariableOperator oIntegerIncrement;

    @BeforeClass
    public static void beforeClass() {
        ValueTypeListProxyFactories.load();
    }

    @Before
    public void before() {
        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));
        i3 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3));
        i4 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(4));

        oRelationalEquals = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.RELATIONAL_EQUALS));
        oIntegerIncrement = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.INTEGER_INCREMENT));

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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthLarge() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLengthSmall() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLength() throws EvaluationException {
        Operators.LIST_LENGTH.evaluate(new IVariable[]{DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEmptyLarge() throws EvaluationException {
        Operators.LIST_EMPTY.evaluate(new IVariable[]{labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEmptySmall() throws EvaluationException {
        Operators.LIST_EMPTY.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeEmpty() throws EvaluationException {
        Operators.LIST_EMPTY.evaluate(new IVariable[]{DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotEmptyLarge() throws EvaluationException {
        Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotEmptySmall() throws EvaluationException {
        Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNotEmpty() throws EvaluationException {
        Operators.LIST_NOT_EMPTY.evaluate(new IVariable[]{DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeElementLarge() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc, labc, labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeElementSmall() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{labc});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeElement() throws EvaluationException {
        Operators.LIST_ELEMENT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsLarge() throws EvaluationException {
        Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers, i2, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsSmall() throws EvaluationException {
        Operators.LIST_CONTAINS.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeContains() throws EvaluationException {
        Operators.LIST_CONTAINS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsPredicateLarge() throws EvaluationException {
        Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers, oRelationalEquals, i2});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeContainsPredicateSmall() throws EvaluationException {
        Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeContainsPredicate() throws EvaluationException {
        Operators.LIST_CONTAINS_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeCountLarge() throws EvaluationException {
        Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers, i2, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeCountSmall() throws EvaluationException {
        Operators.LIST_COUNT.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeCount() throws EvaluationException {
        Operators.LIST_COUNT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeCountPredicateLarge() throws EvaluationException {
        Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers, i2, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeCountPredicateSmall() throws EvaluationException {
        Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeCountPredicate() throws EvaluationException {
        Operators.LIST_COUNT_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
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
        assertThat("append([0, 1, 2], 3)[3] = 3", list.getLength(), is(4));

        assertThat("append([0, 1, 2], 3) = [0, 1, 2, 3]", ((ValueTypeList.ValueList) res1).getRawValue().equals(lintegers.getValue().getRawValue()), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAppendInvalidType() throws EvaluationException {
        Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers_012, oRelationalEquals});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAppendLarge() throws EvaluationException {
        Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers, i2, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAppendSmall() throws EvaluationException {
        Operators.LIST_APPEND.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeAppend() throws EvaluationException {
        Operators.LIST_APPEND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- LAZYBUILT -----------------------------------
     */

    @Test
    public void testListLazyBuilt() throws EvaluationException {
        IValue res1 = Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{i3, oIntegerIncrement});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy<ValueTypeInteger, ValueTypeInteger.ValueInteger> list = ((ValueTypeList.ValueList) res1).getRawValue();

        assertThat("lazybuilt(3, ++)[0] = 3", list.get(0).getRawValue(), is(3));
        assertThat("lazybuilt(3, ++)[1] = 4", list.get(1).getRawValue(), is(4));
        assertThat("lazybuilt(3, ++)[5] = 8", list.get(5).getRawValue(), is(8));
        assertThat("lazybuilt(3, ++)[10] = 13", list.get(10).getRawValue(), is(13));
        assertThat("lazybuilt(3, ++)[100] = 103", list.get(100).getRawValue(), is(103));

        assertThat("append([0, 1, 2], 3)[3] = 3", list.getLength(), is(Integer.MAX_VALUE));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLazyBuiltInvalidType() throws EvaluationException {
        Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{i3, oRelationalEquals});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLazyBuiltLarge() throws EvaluationException {
        Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{lintegers, i2, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLazyBuiltSmall() throws EvaluationException {
        Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLazyBuilt() throws EvaluationException {
        Operators.LIST_LAZYBUILT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
