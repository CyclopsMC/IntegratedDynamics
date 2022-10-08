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
 * Test the different number operators.
 * @author rubensworks
 */
public class TestNumberOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableDouble d0;
    private DummyVariableDouble dm10;
    private DummyVariableDouble d0P5;
    private DummyVariableDouble d0P1;
    private DummyVariableDouble d0P9;
    private DummyVariableInteger i10;
    private DummyVariableInteger i1k;
    private DummyVariableInteger i1m;

    @Before
    public void before() {
        d0   = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0  ));
        dm10 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(-10));
        d0P5 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.5));
        d0P1 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.1));
        d0P9 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(0.9));

        i10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10));
        i1k = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1000));
        i1m = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1000000));
    }

    /**
     * ----------------------------------- ROUND -----------------------------------
     */

    @Test
    public void testDoubleRound() throws EvaluationException {
        IValue res1 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("||0|| = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{dm10});
        assertThat("||-10|| = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{d0P5});
        assertThat("||0.5|| = 1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(1));

        IValue res5 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{d0P1});
        assertThat("||0.1|| = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{d0P9});
        assertThat("||0.9|| = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));

        IValue res7 = Operators.NUMBER_ROUND.evaluate(new IVariable[]{i10});
        assertThat("||10|| = 10", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleRoundLarge() throws EvaluationException {
        Operators.NUMBER_ROUND.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleRoundSmall() throws EvaluationException {
        Operators.NUMBER_ROUND.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleRound() throws EvaluationException {
        Operators.NUMBER_ROUND.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- CEIL -----------------------------------
     */

    @Test
    public void testDoubleCeil() throws EvaluationException {
        IValue res1 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("⌈0⌉ = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{dm10});
        assertThat("⌈-10⌉ = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{d0P5});
        assertThat("⌈0.5⌉ = 1", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(1));

        IValue res5 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{d0P1});
        assertThat("⌈0.1⌉ = 1", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(1));

        IValue res6 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{d0P9});
        assertThat("⌈0.9⌉ = 1", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(1));

        IValue res7 = Operators.NUMBER_CEIL.evaluate(new IVariable[]{i10});
        assertThat("⌈10⌉ = 10", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCeilLarge() throws EvaluationException {
        Operators.NUMBER_CEIL.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCeilSmall() throws EvaluationException {
        Operators.NUMBER_CEIL.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleCeil() throws EvaluationException {
        Operators.NUMBER_CEIL.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLOOR -----------------------------------
     */

    @Test
    public void testDoubleFloor() throws EvaluationException {
        IValue res1 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{d0});
        assertThat("result is an integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("⌊0⌋ = 0", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(0));

        IValue res2 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{dm10});
        assertThat("⌊-10⌋ = -10", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(-10));

        IValue res4 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{d0P5});
        assertThat("⌊0.5⌋ = 0", ((ValueTypeInteger.ValueInteger) res4).getRawValue(), is(0));

        IValue res5 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{d0P1});
        assertThat("⌊0.1⌋ = 0", ((ValueTypeInteger.ValueInteger) res5).getRawValue(), is(0));

        IValue res6 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{d0P9});
        assertThat("⌊0.9⌋ = 0", ((ValueTypeInteger.ValueInteger) res6).getRawValue(), is(0));

        IValue res7 = Operators.NUMBER_FLOOR.evaluate(new IVariable[]{i10});
        assertThat("⌊10⌋ = 10", ((ValueTypeInteger.ValueInteger) res7).getRawValue(), is(10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleFloorLarge() throws EvaluationException {
        Operators.NUMBER_FLOOR.evaluate(new IVariable[]{d0, d0});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleFloorSmall() throws EvaluationException {
        Operators.NUMBER_FLOOR.evaluate(new IVariable[]{});
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleFloor() throws EvaluationException {
        Operators.NUMBER_FLOOR.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FUZZY -----------------------------------
     */
    @Test
    public void testIntegerFuzzy() throws EvaluationException {
        IValue res1 = Operators.NUMBER_FUZZY.evaluate(new IVariable[]{d0});
        assertThat("fuzzy(0) = 0", ((ValueTypeString.ValueString) res1).getRawValue(), is("0"));

        IValue res2 = Operators.NUMBER_FUZZY.evaluate(new IVariable[]{i10});
        assertThat("fuzzy(10) = 10", ((ValueTypeString.ValueString) res2).getRawValue(), is("10"));

        IValue res3 = Operators.NUMBER_FUZZY.evaluate(new IVariable[]{i1k});
        assertThat("fuzzy(1000) = 1K", ((ValueTypeString.ValueString) res3).getRawValue(), is("1K"));

        IValue res4 = Operators.NUMBER_FUZZY.evaluate(new IVariable[]{i1m});
        assertThat("fuzzy(1000000) = 1M", ((ValueTypeString.ValueString) res4).getRawValue(), is("1M"));
    }
}
