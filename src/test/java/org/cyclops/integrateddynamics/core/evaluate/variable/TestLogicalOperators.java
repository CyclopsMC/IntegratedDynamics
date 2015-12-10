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
 * Test the different logical operators.
 * @author rubensworks
 */
public class TestLogicalOperators {

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
     * ----------------------------------- AND -----------------------------------
     */

    @Test
    public void testLogicalAnd() throws EvaluationException {
        IValue res1 = Operators.LOGICAL_AND.evaluate(new IVariable[]{bTrue, bTrue});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class)); // We can't use hamcrest's isA because of: https://github.com/hamcrest/JavaHamcrest/issues/27
        assertThat("true && true = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.LOGICAL_AND.evaluate(new IVariable[]{bTrue, bFalse});
        assertThat("true && false = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.LOGICAL_AND.evaluate(new IVariable[]{bFalse, bTrue});
        assertThat("false && true = false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));

        IValue res4 = Operators.LOGICAL_AND.evaluate(new IVariable[]{bFalse, bFalse});
        assertThat("false && false = false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAndLarge() throws EvaluationException {
        Operators.LOGICAL_AND.evaluate(new IVariable[]{bTrue, bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeAndSmall() throws EvaluationException {
        Operators.LOGICAL_AND.evaluate(new IVariable[]{bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeAnd() throws EvaluationException {
        Operators.LOGICAL_AND.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test
    public void testShortCircuitingAnd() throws EvaluationException {
        Operators.LOGICAL_AND.evaluate(new IVariable[]{bFalse, bTrue});
        assertThat("first variable was called", bFalse.isFetched(), is(true));
        assertThat("second variable was not called", bTrue.isFetched(), is(false));
    }

    /**
     * ----------------------------------- OR -----------------------------------
     */

    @Test
    public void testLogicalOr() throws EvaluationException {
        IValue res1 = Operators.LOGICAL_OR.evaluate(new IVariable[]{bTrue, bTrue});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class)); // We can't use hamcrest's isA because of: https://github.com/hamcrest/JavaHamcrest/issues/27
        assertThat("true || true = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.LOGICAL_OR.evaluate(new IVariable[]{bTrue, bFalse});
        assertThat("true || false = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.LOGICAL_OR.evaluate(new IVariable[]{bFalse, bTrue});
        assertThat("false || true = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.LOGICAL_OR.evaluate(new IVariable[]{bFalse, bFalse});
        assertThat("false || false = false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeOrLarge() throws EvaluationException {
        Operators.LOGICAL_OR.evaluate(new IVariable[]{bTrue, bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeOrSmall() throws EvaluationException {
        Operators.LOGICAL_OR.evaluate(new IVariable[]{bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeOr() throws EvaluationException {
        Operators.LOGICAL_OR.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    @Test
    public void testShortCircuitingOr() throws EvaluationException {
        Operators.LOGICAL_OR.evaluate(new IVariable[]{bTrue, bFalse});
        assertThat("first variable was called", bTrue.isFetched(), is(true));
        assertThat("second variable was not called", bFalse.isFetched(), is(false));
    }

    /**
     * ----------------------------------- NOT -----------------------------------
     */

    @Test
    public void testLogicalNot() throws EvaluationException {
        IValue res1 = Operators.LOGICAL_NOT.evaluate(new IVariable[]{bTrue});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class)); // We can't use hamcrest's isA because of: https://github.com/hamcrest/JavaHamcrest/issues/27
        assertThat("!true = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.LOGICAL_NOT.evaluate(new IVariable[]{bFalse});
        assertThat("!false = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotLarge() throws EvaluationException {
        Operators.LOGICAL_NOT.evaluate(new IVariable[]{bTrue, bTrue});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotSmall() throws EvaluationException {
        Operators.LOGICAL_NOT.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeNot() throws EvaluationException {
        Operators.LOGICAL_NOT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
