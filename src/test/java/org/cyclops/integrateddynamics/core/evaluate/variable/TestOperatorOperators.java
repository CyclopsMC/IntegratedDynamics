package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestOperatorOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableBoolean bFalse;
    private DummyVariableBoolean bTrue;

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger i2;
    private DummyVariableInteger i3;
    private DummyVariableInteger i4;

    private DummyVariableOperator oGeneralIdentity;
    private DummyVariableOperator oLogicalNot;
    private DummyVariableOperator oLogicalAnd;
    private DummyVariableOperator oIntegerIncrement;
    private DummyVariableOperator oRelationalEquals;
    private DummyVariableOperator oRelationalGreaterThan;
    private DummyVariableOperator oRelationalLessThan;
    private DummyVariableOperator oIntegerModulus;

    private DummyVariableList lintegers;
    private DummyVariableList lbooleans;

    @Before
    public void before() {
        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue  = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));
        i3 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(3));
        i4 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(4));

        oGeneralIdentity       = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.GENERAL_IDENTITY));
        oLogicalNot            = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
        oLogicalAnd            = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_AND));
        oIntegerIncrement      = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.INTEGER_INCREMENT));
        oRelationalEquals      = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.RELATIONAL_EQUALS));
        oRelationalGreaterThan = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.RELATIONAL_GT));
        oRelationalLessThan    = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.RELATIONAL_LT));
        oIntegerModulus        = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.INTEGER_MODULUS));

        lintegers = new DummyVariableList(ValueTypeList.ValueList.ofAll(i0.getValue(), i1.getValue(), i2.getValue(), i3.getValue()));
        lbooleans = new DummyVariableList(ValueTypeList.ValueList.ofAll(bFalse.getValue(), bTrue.getValue(), bFalse.getValue(), bTrue.getValue()));
    }

    /**
     * ----------------------------------- APPLY -----------------------------------
     */

    @Test
    public void testApply() throws EvaluationException {
        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity, bFalse});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("id(false) == false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity, bTrue});
        assertThat("id(true) == true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalNot, bTrue});
        assertThat("not(true) == false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));

        IValue res4 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalNot, bFalse});
        assertThat("not(true) == false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));
    }

    @Test
    public void testApplyCurring() throws EvaluationException {
        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue});
        assertThat("result is an operator", res1, instanceOf(ValueTypeOperator.ValueOperator.class));
        assertThat("result is a curriedoperator", ((ValueTypeOperator.ValueOperator) res1).getRawValue(), instanceOf(CurriedOperator.class));

        DummyVariableOperator oLogicalAndCurriedTrue = new DummyVariableOperator((ValueTypeOperator.ValueOperator) res1);
        IValue res2_1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAndCurriedTrue, bTrue});
        assertThat("result is a boolean", res2_1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("and(true)(true) == true", ((ValueTypeBoolean.ValueBoolean) res2_1).getRawValue(), is(true));
        IValue res2_2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAndCurriedTrue, bFalse});
        assertThat("and(true)(false) == false", ((ValueTypeBoolean.ValueBoolean) res2_2).getRawValue(), is(false));

        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bFalse});
        assertThat("result is a curriedoperator", ((ValueTypeOperator.ValueOperator) res3).getRawValue(), instanceOf(CurriedOperator.class));

        DummyVariableOperator oLogicalAndCurriedFalse = new DummyVariableOperator((ValueTypeOperator.ValueOperator) res3);
        IValue res4_1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAndCurriedFalse, bTrue});
        assertThat("result is a boolean", res2_1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("and(false)(true) == false", ((ValueTypeBoolean.ValueBoolean) res4_1).getRawValue(), is(false));
        IValue res4_2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAndCurriedFalse, bFalse});
        assertThat("and(false)(false) == false", ((ValueTypeBoolean.ValueBoolean) res4_2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeApplyLarge() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity, bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeApplyCurryingLarge() throws EvaluationException {
        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{curriedOperatorValue, bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeApplySmall() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeApplyCurringSmall() throws EvaluationException {
        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{curriedOperatorValue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypeApply() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypeApply() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalNot, oGeneralIdentity});
    }

    @Test
    public void testValidateTypesApply() {
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesApply() {
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, bFalse}),
                CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, i0}),
                CoreMatchers.<IValueType>is(ValueTypes.INTEGER));
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, oGeneralIdentity}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));

        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oLogicalNot, bFalse}),
                CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
    }

    @Test
    public void testConditionalOutputTypesApplyCurring() throws EvaluationException {
        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));

        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{curriedOperatorValue, bFalse}),
                CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
    }

    /**
     * ----------------------------------- MAP -----------------------------------
     */

    @Test
    public void testMap() throws EvaluationException {
        IValue res1 = Operators.OPERATOR_MAP.evaluate(new IVariable[]{oIntegerIncrement, lintegers});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy list1 = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("map([0, 1, 2, 3], ++)[0] == 1", ((ValueTypeInteger.ValueInteger) list1.get(0)).getRawValue(), is(1));
        assertThat("map([0, 1, 2, 3], ++)[0] == 2", ((ValueTypeInteger.ValueInteger) list1.get(1)).getRawValue(), is(2));
        assertThat("map([0, 1, 2, 3], ++)[0] == 3", ((ValueTypeInteger.ValueInteger) list1.get(2)).getRawValue(), is(3));
        assertThat("map([0, 1, 2, 3], ++)[0] == 4", ((ValueTypeInteger.ValueInteger) list1.get(3)).getRawValue(), is(4));
        assertThat(list1.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.INTEGER));

        IValue res2 = Operators.OPERATOR_MAP.evaluate(new IVariable[]{oLogicalNot, lbooleans});
        IValueTypeListProxy list2 = ((ValueTypeList.ValueList) res2).getRawValue();
        assertThat("map([false, true, false, true], !)[0] == true", ((ValueTypeBoolean.ValueBoolean) list2.get(0)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], !)[1] == false", ((ValueTypeBoolean.ValueBoolean) list2.get(1)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], !)[2] == true", ((ValueTypeBoolean.ValueBoolean) list2.get(2)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], !)[3] == false", ((ValueTypeBoolean.ValueBoolean) list2.get(3)).getRawValue(), is(false));
        assertThat(list2.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));

        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));
        IValue res3 = Operators.OPERATOR_MAP.evaluate(new IVariable[]{curriedOperatorValue, lbooleans});
        IValueTypeListProxy list3 = ((ValueTypeList.ValueList) res3).getRawValue();
        assertThat("map([false, true, false, true], true&&)[0] == false", ((ValueTypeBoolean.ValueBoolean) list3.get(0)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], true&&)[1] == true", ((ValueTypeBoolean.ValueBoolean) list3.get(1)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], true&&)[2] == false", ((ValueTypeBoolean.ValueBoolean) list3.get(2)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], true&&)[3] == true", ((ValueTypeBoolean.ValueBoolean) list3.get(3)).getRawValue(), is(true));
        assertThat(list3.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMapLarge() throws EvaluationException {
        Operators.OPERATOR_MAP.evaluate(new IVariable[]{oIntegerIncrement, lintegers, lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeMapSmall() throws EvaluationException {
        Operators.OPERATOR_MAP.evaluate(new IVariable[]{oIntegerIncrement});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypeMap() throws EvaluationException {
        Operators.OPERATOR_MAP.evaluate(new IVariable[]{bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypeMap() throws EvaluationException {
        Operators.OPERATOR_MAP.evaluate(new IVariable[]{oIntegerIncrement, oIntegerIncrement});
    }

    @Test
    public void testValidateTypesMap() {
        assertThat(Operators.OPERATOR_MAP.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_MAP.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_MAP.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_MAP.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesMap() throws EvaluationException {
        assertThat(Operators.OPERATOR_MAP.getConditionalOutputType(new IVariable[]{oIntegerIncrement, lintegers}),
                CoreMatchers.<IValueType>is(ValueTypes.LIST));
        assertThat(Operators.OPERATOR_MAP.getConditionalOutputType(new IVariable[]{oLogicalNot, lbooleans}),
                CoreMatchers.<IValueType>is(ValueTypes.LIST));

        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));
        assertThat(Operators.OPERATOR_MAP.getConditionalOutputType(new IVariable[]{curriedOperatorValue, lbooleans}),
                CoreMatchers.<IValueType>is(ValueTypes.LIST));
    }

    /**
     * ----------------------------------- PREDICATE CONJUNCTION -----------------------------------
     */

    @Test
    public void testPredicateConjunction() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        DummyVariableOperator twoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalGreaterThan, i2}));
        DummyVariableOperator zeroLessThanandTwoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{zeroLessThan, twoGreaterThan}));

        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanandTwoGreaterThan, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("0< && 2>(0) == false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));
        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanandTwoGreaterThan, i1});
        assertThat("0< && 2>(1) == true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanandTwoGreaterThan, i2});
        assertThat("0< && 2>(2) == false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateConjunctionLarge() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));

        Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{zeroLessThan, zeroLessThan, zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateConjunctionSmall() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypePredicateConjunction() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{bFalse, zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypePredicateConjunction() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));
        Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{equalsTwo, i0});
    }

    @Test
    public void testValidateTypesPredicateConjunction() {
        assertThat(Operators.OPERATOR_CONJUNCTION.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_CONJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_CONJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_CONJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesPredicateConjunction() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        assertThat(Operators.OPERATOR_CONJUNCTION.getConditionalOutputType(new IVariable[]{equalsTwo, equalsTwo}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));
    }

    /**
     * ----------------------------------- PREDICATE DISJUNCTION -----------------------------------
     */

    @Test
    public void testPredicateDisjunction() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        DummyVariableOperator twoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalGreaterThan, i2}));
        DummyVariableOperator zeroLessThanorTwoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{zeroLessThan, twoGreaterThan}));

        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanorTwoGreaterThan, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("0< || 2>(0) == false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanorTwoGreaterThan, i1});
        assertThat("0< || 2>(1) == true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{zeroLessThanorTwoGreaterThan, i2});
        assertThat("0< || 2>(2) == false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateDisjunctionLarge() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));

        Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{zeroLessThan, zeroLessThan, zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateDisjunctionSmall() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypePredicateDisjunction() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{bFalse, zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypePredicateDisjunction() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));
        Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{equalsTwo, i0});
    }

    @Test
    public void testValidateTypesPredicateDisjunction() {
        assertThat(Operators.OPERATOR_DISJUNCTION.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_DISJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_DISJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_DISJUNCTION.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesPredicateDisjunction() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        assertThat(Operators.OPERATOR_DISJUNCTION.getConditionalOutputType(new IVariable[]{equalsTwo, equalsTwo}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));
    }

    /**
     * ----------------------------------- PREDICATE NEGATION -----------------------------------
     */

    @Test
    public void testPredicateNegation() throws EvaluationException {
        DummyVariableOperator twoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalGreaterThan, i2}));
        DummyVariableOperator notTwoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_NEGATION.evaluate(new IVariable[]{twoGreaterThan}));

        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{notTwoGreaterThan, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("!2>(0) == false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));
        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{notTwoGreaterThan, i1});
        assertThat("!2>(1) == false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{notTwoGreaterThan, i2});
        assertThat("!2>(2) == true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));
        IValue res4 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{notTwoGreaterThan, i3});
        assertThat("!2>(3) == true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateNegationLarge() throws EvaluationException {
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));

        Operators.OPERATOR_NEGATION.evaluate(new IVariable[]{zeroLessThan, zeroLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateNegationSmall() throws EvaluationException {
        Operators.OPERATOR_NEGATION.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypePredicateNegation() throws EvaluationException {
        Operators.OPERATOR_NEGATION.evaluate(new IVariable[]{bFalse});
    }

    @Test
    public void testValidateTypesPredicateNegation() {
        assertThat(Operators.OPERATOR_NEGATION.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_NEGATION.validateTypes(new IValueType[]{ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_NEGATION.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesPredicateNegation() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        assertThat(Operators.OPERATOR_NEGATION.getConditionalOutputType(new IVariable[]{equalsTwo}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));
    }

    /**
     * ----------------------------------- PREDICATE PIPE -----------------------------------
     */

    @Test
    public void testPredicatePipe() throws EvaluationException {
        DummyVariableOperator increment2 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_PIPE.evaluate(new IVariable[]{oIntegerIncrement, oIntegerIncrement}));

        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{increment2, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("++ ++(0) == 2", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(2));
        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{increment2, i1});
        assertThat("++ ++(1) == 3", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(3));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicatePipeLarge() throws EvaluationException {
        Operators.OPERATOR_PIPE.evaluate(new IVariable[]{oIntegerIncrement, oIntegerIncrement, oIntegerIncrement});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicatePipeSmall() throws EvaluationException {
        Operators.OPERATOR_PIPE.evaluate(new IVariable[]{oIntegerIncrement});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypePredicatePipe() throws EvaluationException {
        Operators.OPERATOR_PIPE.evaluate(new IVariable[]{bFalse, oIntegerIncrement});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypePredicatePipe() throws EvaluationException {
        Operators.OPERATOR_PIPE.evaluate(new IVariable[]{oIntegerIncrement, i0});
    }

    @Test
    public void testValidateTypesPredicatePipe() {
        assertThat(Operators.OPERATOR_PIPE.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_PIPE.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_PIPE.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_PIPE.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.OPERATOR}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesPredicatePipe() throws EvaluationException {
        assertThat(Operators.OPERATOR_PIPE.getConditionalOutputType(new IVariable[]{oIntegerIncrement, oIntegerIncrement}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));
    }

    /**
     * ----------------------------------- PREDICATE FLIP -----------------------------------
     */

    @Test
    public void testPredicateFlip() throws EvaluationException {
        DummyVariableOperator lessThanFlipped = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oRelationalLessThan}));
        DummyVariableOperator lessThan2 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{lessThanFlipped, i2}));

        IValue res1 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{lessThan2, i0});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("<2(0) == true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));
        IValue res2 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{lessThan2, i1});
        assertThat("<2(1) == true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
        IValue res3 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{lessThan2, i2});
        assertThat("<2(2) == false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));
        IValue res4 = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{lessThan2, i3});
        assertThat("<2(3) == false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateFlipLarge() throws EvaluationException {
        Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oRelationalLessThan, oRelationalLessThan});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizePredicateFlipSmall() throws EvaluationException {
        Operators.OPERATOR_FLIP.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypePredicateFlip() throws EvaluationException {
        Operators.OPERATOR_FLIP.evaluate(new IVariable[]{bFalse});
    }

    @Test
    public void testValidateTypesPredicateFlip() {
        assertThat(Operators.OPERATOR_FLIP.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_FLIP.validateTypes(new IValueType[]{ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_FLIP.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesPredicateFlip() throws EvaluationException {
        DummyVariableOperator lessThanFlipped = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oRelationalLessThan}));

        assertThat(Operators.OPERATOR_FLIP.getConditionalOutputType(new IVariable[]{lessThanFlipped}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));
    }

    /**
     * ----------------------------------- FILTER -----------------------------------
     */

    @Test
    public void testFilter() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        // Filter: equal to 2
        IValue res1 = Operators.OPERATOR_FILTER.evaluate(new IVariable[]{equalsTwo, lintegers});
        assertThat("result is a list", res1, instanceOf(ValueTypeList.ValueList.class));
        IValueTypeListProxy list1 = ((ValueTypeList.ValueList) res1).getRawValue();
        assertThat("length(filter([0, 1, 2, 3], 2==)) == 1", list1.getLength(), is(1));
        assertThat("filter([0, 1, 2, 3], 2==)[0] == 2", ((ValueTypeInteger.ValueInteger) list1.get(0)).getRawValue(), is(2));
        assertThat(list1.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.INTEGER));

        // Filter: greater than 0 and less than 2
        DummyVariableOperator zeroLessThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalLessThan, i0}));
        DummyVariableOperator twoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalGreaterThan, i2}));
        DummyVariableOperator zeroLessThanandTwoGreaterThan = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_CONJUNCTION.evaluate(new IVariable[]{zeroLessThan, twoGreaterThan}));
        IValue res2 = Operators.OPERATOR_FILTER.evaluate(new IVariable[]{zeroLessThanandTwoGreaterThan, lintegers});
        IValueTypeListProxy list2 = ((ValueTypeList.ValueList) res2).getRawValue();
        assertThat("length(<2 && >0([0, 1, 2, 3])) == 1", list2.getLength(), is(1));
        assertThat("<2 && >0([0, 1, 2, 3])[0] == 1", ((ValueTypeInteger.ValueInteger) list2.get(0)).getRawValue(), is(1));
        assertThat(list2.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.INTEGER));

        // Filter: all even numbers
        DummyVariableOperator modulusFlipped = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oIntegerModulus}));
        DummyVariableOperator modulus2 = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{modulusFlipped, i2}));
        DummyVariableOperator isZero = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i0}));
        DummyVariableOperator isEvenUnsafe = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_PIPE.evaluate(new IVariable[]{modulus2, isZero}));
        DummyVariableOperator isEven = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_DISJUNCTION.evaluate(new IVariable[]{isZero, isEvenUnsafe}));
        IValue res3 = Operators.OPERATOR_FILTER.evaluate(new IVariable[]{isEven, lintegers});
        IValueTypeListProxy list3 = ((ValueTypeList.ValueList) res3).getRawValue();
        assertThat("length(even([0, 1, 2, 3])) == 2", list3.getLength(), is(2));
        assertThat("even([0, 1, 2, 3])[0] == 0", ((ValueTypeInteger.ValueInteger) list3.get(0)).getRawValue(), is(0));
        assertThat("even([0, 1, 2, 3])[1] == 2", ((ValueTypeInteger.ValueInteger) list3.get(1)).getRawValue(), is(2));
        assertThat(list2.getValueType(), CoreMatchers.<IValueType>is(ValueTypes.INTEGER));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeFilterLarge() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        Operators.OPERATOR_FILTER.evaluate(new IVariable[]{equalsTwo, lintegers, lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeFilterSmall() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        Operators.OPERATOR_FILTER.evaluate(new IVariable[]{equalsTwo});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorTypeFilter() throws EvaluationException {
        Operators.OPERATOR_FILTER.evaluate(new IVariable[]{bFalse, lintegers});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputTypeFilter() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        Operators.OPERATOR_FILTER.evaluate(new IVariable[]{equalsTwo, oIntegerIncrement});
    }

    @Test
    public void testValidateTypesFilter() {
        assertThat(Operators.OPERATOR_FILTER.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_FILTER.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_FILTER.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.BOOLEAN}), notNullValue());
        assertThat(Operators.OPERATOR_FILTER.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.LIST}), nullValue());
    }

    @Test
    public void testConditionalOutputTypesFilter() throws EvaluationException {
        DummyVariableOperator equalsTwo = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oRelationalEquals, i2}));

        assertThat(Operators.OPERATOR_FILTER.getConditionalOutputType(new IVariable[]{equalsTwo, lintegers}),
                CoreMatchers.<IValueType>is(ValueTypes.LIST));
    }

}
