package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the different general operators.
 * @author rubensworks
 */
public class TestGeneralOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableBoolean bTrue;
    private DummyVariableBoolean bFalse;

    @Before
    public void before() {
        bTrue = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));
        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
    }

    /**
     * ----------------------------------- CHOICE -----------------------------------
     */

    @Test
    public void testLogicalChoice() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        DummyVariableInteger i2 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(2));

        IValue res1 = Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, i1, i2});
        assertThat("result is 1", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("true ? 1 : 2 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bFalse, i1, i2});
        assertThat("false ? 1 : 2 = 1", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(2));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeChoiceLarge() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, bTrue, bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeChoiceSmall() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeChoice() throws EvaluationException {
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidLogicalChoiceDifferentTypes() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));
        Operators.GENERAL_CHOICE.evaluate(new IVariable[]{bFalse, i1, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- IDENTITY -----------------------------------
     */

    @Test
    public void testLogicalIdentity() throws EvaluationException {
        DummyVariableInteger i1 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1));

        IValue res1 = Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{i1});
        assertThat("result is 1", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("1 = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{bFalse});
        assertThat("false = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIdentityLarge() throws EvaluationException {
        Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIdentitySmall() throws EvaluationException {
        Operators.GENERAL_IDENTITY.evaluate(new IVariable[]{});
    }

}
