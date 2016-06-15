package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
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

    @Before
    public void before() {
        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue  = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        i0 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0));

        oGeneralIdentity = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.GENERAL_IDENTITY));
        oLogicalNot      = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.LOGICAL_NOT));
    }

    /**
     * ----------------------------------- APPLY -----------------------------------
     */

    @Test
    public void testRelationalEquals() throws EvaluationException {
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

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEqualsLarge() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity, bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEqualsSmall() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oGeneralIdentity});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorType() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{bFalse, bFalse});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidOperatorInputType() throws EvaluationException {
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{oLogicalNot, oGeneralIdentity});
    }

    @Test
    public void testValidateTypes() {
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{}), notNullValue());
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{ValueTypes.OPERATOR}), notNullValue());
        assertThat(Operators.OPERATOR_APPLY.validateTypes(new IValueType[]{ValueTypes.OPERATOR, ValueTypes.CATEGORY_ANY}), nullValue());
    }

    @Test
    public void testConditionalOutputTypes() {
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, bFalse}),
                CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, i0}),
                CoreMatchers.<IValueType>is(ValueTypes.INTEGER));
        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oGeneralIdentity, oGeneralIdentity}),
                CoreMatchers.<IValueType>is(ValueTypes.OPERATOR));

        assertThat(Operators.OPERATOR_APPLY.getConditionalOutputType(new IVariable[]{oLogicalNot, bFalse}),
                CoreMatchers.<IValueType>is(ValueTypes.BOOLEAN));
    }

}
