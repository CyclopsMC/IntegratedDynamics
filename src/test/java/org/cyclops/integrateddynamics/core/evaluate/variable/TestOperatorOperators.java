package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
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

    private DummyVariableOperator oGeneralIdentity;
    private DummyVariableOperator oLogicalNot;
    private DummyVariableOperator oLogicalAnd;

    @Before
    public void before() {
        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue  = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));

        oGeneralIdentity = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.GENERAL_IDENTITY));
        oLogicalNot      = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
        oLogicalAnd      = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_AND));
    }

    /**
     * ----------------------------------- APPLY -----------------------------------
     */

    @Test
    public void testRelationalApply() throws EvaluationException {
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
    public void testRelationalApplyCurring() throws EvaluationException {
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

}
