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

        oGeneralIdentity  = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.GENERAL_IDENTITY));
        oLogicalNot       = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
        oLogicalAnd       = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_AND));
        oIntegerIncrement = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.INTEGER_INCREMENT));

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

        IValue res2 = Operators.OPERATOR_MAP.evaluate(new IVariable[]{oLogicalNot, lbooleans});
        IValueTypeListProxy list2 = ((ValueTypeList.ValueList) res2).getRawValue();
        assertThat("map([false, true, false, true], !)[0] == true", ((ValueTypeBoolean.ValueBoolean) list2.get(0)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], !)[1] == false", ((ValueTypeBoolean.ValueBoolean) list2.get(1)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], !)[2] == true", ((ValueTypeBoolean.ValueBoolean) list2.get(2)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], !)[3] == false", ((ValueTypeBoolean.ValueBoolean) list2.get(3)).getRawValue(), is(false));

        DummyVariableOperator curriedOperatorValue = new DummyVariableOperator((ValueTypeOperator.ValueOperator)
                Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalAnd, bTrue}));
        IValue res3 = Operators.OPERATOR_MAP.evaluate(new IVariable[]{curriedOperatorValue, lbooleans});
        IValueTypeListProxy list3 = ((ValueTypeList.ValueList) res3).getRawValue();
        assertThat("map([false, true, false, true], true&&)[0] == false", ((ValueTypeBoolean.ValueBoolean) list3.get(0)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], true&&)[1] == true", ((ValueTypeBoolean.ValueBoolean) list3.get(1)).getRawValue(), is(true));
        assertThat("map([false, true, false, true], true&&)[2] == false", ((ValueTypeBoolean.ValueBoolean) list3.get(2)).getRawValue(), is(false));
        assertThat("map([false, true, false, true], true&&)[3] == true", ((ValueTypeBoolean.ValueBoolean) list3.get(3)).getRawValue(), is(true));
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

}
