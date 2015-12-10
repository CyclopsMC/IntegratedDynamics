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
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestRelationalOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger im10;
    private DummyVariableInteger i10;

    @Before
    public void before() {
        i0   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0  ));
        i1   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1  ));
        im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        i10  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10 ));
    }

    /**
     * ----------------------------------- EQUALS -----------------------------------
     */

    @Test
    public void testRelationalEquals() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 == 10 = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i0, i10});
        assertThat("0 == 10 = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i10, i0});
        assertThat("10 == 0 = false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));

        IValue res4 = Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 == 10 = false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));

        IValue res5 = Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i10, im10});
        assertThat("10 == -10 = false", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEqualsLarge() throws EvaluationException {
        Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeEqualsSmall() throws EvaluationException {
        Operators.RELATIONAL_EQUALS.evaluate(new IVariable[]{i0});
    }

    /**
     * ----------------------------------- GT -----------------------------------
     */

    @Test
    public void testRelationalGT() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_GT.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 > 10 = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.RELATIONAL_GT.evaluate(new IVariable[]{i0, i10});
        assertThat("0 > 10 = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.RELATIONAL_GT.evaluate(new IVariable[]{i10, i0});
        assertThat("10 > 0 = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.RELATIONAL_GT.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 > 10 = false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));

        IValue res5 = Operators.RELATIONAL_GT.evaluate(new IVariable[]{i10, im10});
        assertThat("10 > -10 = true", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeGTLarge() throws EvaluationException {
        Operators.RELATIONAL_GT.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeGTSmall() throws EvaluationException {
        Operators.RELATIONAL_GT.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeGT() throws EvaluationException {
        Operators.RELATIONAL_GT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- LT -----------------------------------
     */

    @Test
    public void testRelationalLT() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_LT.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 < 10 = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.RELATIONAL_LT.evaluate(new IVariable[]{i0, i10});
        assertThat("0 < 10 = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.RELATIONAL_LT.evaluate(new IVariable[]{i10, i0});
        assertThat("10 < 0 = false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));

        IValue res4 = Operators.RELATIONAL_LT.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 < 10 = true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));

        IValue res5 = Operators.RELATIONAL_LT.evaluate(new IVariable[]{i10, im10});
        assertThat("10 < -10 = false", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLTLarge() throws EvaluationException {
        Operators.RELATIONAL_LT.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLTSmall() throws EvaluationException {
        Operators.RELATIONAL_LT.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLT() throws EvaluationException {
        Operators.RELATIONAL_LT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- NOT EQUALS -----------------------------------
     */

    @Test
    public void testRelationalNotEquals() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 != 10 = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(false));

        IValue res2 = Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i0, i10});
        assertThat("0 != 10 = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i10, i0});
        assertThat("10 != 0 = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 != 10 = true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));

        IValue res5 = Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i10, im10});
        assertThat("10 != -10 = true", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotEqualsLarge() throws EvaluationException {
        Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeNotEqualsSmall() throws EvaluationException {
        Operators.RELATIONAL_NOTEQUALS.evaluate(new IVariable[]{i0});
    }

    /**
     * ----------------------------------- GE -----------------------------------
     */

    @Test
    public void testRelationalGE() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_GE.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 >= 10 = true", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.RELATIONAL_GE.evaluate(new IVariable[]{i0, i10});
        assertThat("0 >= 10 = false", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(false));

        IValue res3 = Operators.RELATIONAL_GE.evaluate(new IVariable[]{i10, i0});
        assertThat("10 >= 0 = true", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(true));

        IValue res4 = Operators.RELATIONAL_GE.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 >= 10 = false", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(false));

        IValue res5 = Operators.RELATIONAL_GE.evaluate(new IVariable[]{i10, im10});
        assertThat("10 >= -10 = true", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(true));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeGELarge() throws EvaluationException {
        Operators.RELATIONAL_GE.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeGESmall() throws EvaluationException {
        Operators.RELATIONAL_GE.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeGE() throws EvaluationException {
        Operators.RELATIONAL_GE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- LE -----------------------------------
     */

    @Test
    public void testRelationalLE() throws EvaluationException {
        IValue res1 = Operators.RELATIONAL_LE.evaluate(new IVariable[]{i10, i10});
        assertThat("result is a boolean", res1, instanceOf(ValueTypeBoolean.ValueBoolean.class));
        assertThat("10 <= 10 = false", ((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), is(true));

        IValue res2 = Operators.RELATIONAL_LE.evaluate(new IVariable[]{i0, i10});
        assertThat("0 <= 10 = true", ((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), is(true));

        IValue res3 = Operators.RELATIONAL_LE.evaluate(new IVariable[]{i10, i0});
        assertThat("10 <= 0 = false", ((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), is(false));

        IValue res4 = Operators.RELATIONAL_LE.evaluate(new IVariable[]{im10, i10});
        assertThat("-10 <= 10 = true", ((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), is(true));

        IValue res5 = Operators.RELATIONAL_LE.evaluate(new IVariable[]{i10, im10});
        assertThat("10 <= -10 = false", ((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), is(false));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLELarge() throws EvaluationException {
        Operators.RELATIONAL_LE.evaluate(new IVariable[]{i0, i0, i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLESmall() throws EvaluationException {
        Operators.RELATIONAL_LE.evaluate(new IVariable[]{i0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLE() throws EvaluationException {
        Operators.RELATIONAL_LE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
