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
public class TestDoubleOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableDouble d0;
    private DummyVariableDouble dm10;
    private DummyVariableDouble d0P5;
    private DummyVariableDouble d0P1;
    private DummyVariableDouble d0P9;

    @Before
    public void before() {
        d0   = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0  ));
        dm10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-10));
        d0P5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.5));
        d0P1 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.1));
        d0P9 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.9));
    }

    /**
     * ----------------------------------- ROUND -----------------------------------
     */

    @Test
    public void testDoubleRound() throws EvaluationException {
        IValue res1 = Operators.DOUBLE_ROUND.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("||0|| = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.DOUBLE_ROUND.evaluate(new IVariable[]{dm10});
        assertThat("||-10|| = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.DOUBLE_ROUND.evaluate(new IVariable[]{d0P5});
        assertThat("||0.5|| = 1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(1));

        IValue res5 = Operators.DOUBLE_ROUND.evaluate(new IVariable[]{d0P1});
        assertThat("||0.1|| = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.DOUBLE_ROUND.evaluate(new IVariable[]{d0P9});
        assertThat("||0.9|| = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleRoundLarge() throws EvaluationException {
        Operators.DOUBLE_ROUND.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleRoundSmall() throws EvaluationException {
        Operators.DOUBLE_ROUND.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleRound() throws EvaluationException {
        Operators.DOUBLE_ROUND.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- CEIL -----------------------------------
     */

    @Test
    public void testDoubleCeil() throws EvaluationException {
        IValue res1 = Operators.DOUBLE_CEIL.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("⌈0⌉ = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.DOUBLE_CEIL.evaluate(new IVariable[]{dm10});
        assertThat("⌈-10⌉ = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.DOUBLE_CEIL.evaluate(new IVariable[]{d0P5});
        assertThat("⌈0.5⌉ = 1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(1));

        IValue res5 = Operators.DOUBLE_CEIL.evaluate(new IVariable[]{d0P1});
        assertThat("⌈0.1⌉ = 1", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(1));

        IValue res6 = Operators.DOUBLE_CEIL.evaluate(new IVariable[]{d0P9});
        assertThat("⌈0.9⌉ = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCeilLarge() throws EvaluationException {
        Operators.DOUBLE_CEIL.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCeilSmall() throws EvaluationException {
        Operators.DOUBLE_CEIL.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleCeil() throws EvaluationException {
        Operators.DOUBLE_CEIL.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLOOR -----------------------------------
     */

    @Test
    public void testDoubleFloor() throws EvaluationException {
        IValue res1 = Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("⌊0⌋ = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{dm10});
        assertThat("⌊-10⌋ = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{d0P5});
        assertThat("⌊0.5⌋ = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{d0P1});
        assertThat("⌊0.1⌋ = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{d0P9});
        assertThat("⌊0.9⌋ = 0", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(0));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleFloorLarge() throws EvaluationException {
        Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleFloorSmall() throws EvaluationException {
        Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleFloor() throws EvaluationException {
        Operators.DOUBLE_FLOOR.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
