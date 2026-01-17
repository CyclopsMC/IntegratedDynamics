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
        ValueCastMappings.load();

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
    public void testNumberCompact() throws EvaluationException {
        IValue res1 = Operators.NUMBER_COMPACT.evaluate(new IVariable[]{d0});
        assertThat("compact(0) = 0", ((ValueTypeString.ValueString) res1).getRawValue(), is("0"));

        IValue res2 = Operators.NUMBER_COMPACT.evaluate(new IVariable[]{i10});
        assertThat("compact(10) = 10", ((ValueTypeString.ValueString) res2).getRawValue(), is("10"));

        IValue res3 = Operators.NUMBER_COMPACT.evaluate(new IVariable[]{i1k});
        assertThat("compact(1000) = 1K", ((ValueTypeString.ValueString) res3).getRawValue(), is("1K"));

        IValue res4 = Operators.NUMBER_COMPACT.evaluate(new IVariable[]{i1m});
        assertThat("compact(1000000) = 1M", ((ValueTypeString.ValueString) res4).getRawValue(), is("1M"));
    }

    /**
     * ----------------------------------- CAST_DOUBLE -----------------------------------
     */

    @Test
    public void testDoubleCastDouble() throws EvaluationException {
        IValue res1 = Operators.NUMBER_CAST_DOUBLE.evaluate(new Variable<>(ValueTypeDouble.ValueDouble.of(1.1D)));
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("cast 1.1D = 1.1D", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(1.1D));

        IValue res2 = Operators.NUMBER_CAST_DOUBLE.evaluate(new Variable<>(ValueTypeLong.ValueLong.of(100L)));
        assertThat("result is a double", res2, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("cast 100L = 100.0D", ((ValueTypeDouble.ValueDouble) res2).getRawValue(), is(100.0D));

        IValue res3 = Operators.NUMBER_CAST_DOUBLE.evaluate(new Variable<>(ValueTypeInteger.ValueInteger.of(10)));
        assertThat("result is a double", res3, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("cast 10 = 10D", ((ValueTypeDouble.ValueDouble) res3).getRawValue(), is(10.0D));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCastDoubleLarge() throws EvaluationException {
        Operators.NUMBER_CAST_DOUBLE.evaluate(d0, d0);
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeDoubleCastDoubleSmall() throws EvaluationException {
        Operators.NUMBER_CAST_DOUBLE.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeDoubleCastDouble() throws EvaluationException {
        Operators.NUMBER_CAST_DOUBLE.evaluate(DUMMY_VARIABLE);
    }

    /**
     * ----------------------------------- CAST_LONG -----------------------------------
     */

    @Test
    public void testLongCastLong() throws EvaluationException {
        IValue res1 = Operators.NUMBER_CAST_LONG.evaluate(new Variable<>(ValueTypeDouble.ValueDouble.of(1.1D)));
        assertThat("result is a long", res1, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("cast 1.1D = 1L", ((ValueTypeLong.ValueLong) res1).getRawValue(), is(1L));

        IValue res2 = Operators.NUMBER_CAST_LONG.evaluate(new Variable<>(ValueTypeLong.ValueLong.of(100L)));
        assertThat("result is a long", res2, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("cast 100L = 100L", ((ValueTypeLong.ValueLong) res2).getRawValue(), is(100L));

        IValue res3 = Operators.NUMBER_CAST_LONG.evaluate(new Variable<>(ValueTypeInteger.ValueInteger.of(10)));
        assertThat("result is a long", res3, instanceOf(ValueTypeLong.ValueLong.class));
        assertThat("cast 10 = 10L", ((ValueTypeLong.ValueLong) res3).getRawValue(), is(10L));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLongCastLongLarge() throws EvaluationException {
        Operators.NUMBER_CAST_LONG.evaluate(d0, d0);
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeLongCastLongSmall() throws EvaluationException {
        Operators.NUMBER_CAST_LONG.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeLongCastLong() throws EvaluationException {
        Operators.NUMBER_CAST_LONG.evaluate(DUMMY_VARIABLE);
    }

    /**
     * ----------------------------------- CAST_INTEGER -----------------------------------
     */

    @Test
    public void testIntegerCastInteger() throws EvaluationException {
        IValue res1 = Operators.NUMBER_CAST_INTEGER.evaluate(new Variable<>(ValueTypeDouble.ValueDouble.of(1.1D)));
        assertThat("result is a integer", res1, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("cast 1.1D = 1", ((ValueTypeInteger.ValueInteger) res1).getRawValue(), is(1));

        IValue res2 = Operators.NUMBER_CAST_INTEGER.evaluate(new Variable<>(ValueTypeLong.ValueLong.of(100L)));
        assertThat("result is a integer", res2, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("cast 100L = 100", ((ValueTypeInteger.ValueInteger) res2).getRawValue(), is(100));

        IValue res3 = Operators.NUMBER_CAST_INTEGER.evaluate(new Variable<>(ValueTypeInteger.ValueInteger.of(10)));
        assertThat("result is a integer", res3, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("cast 10 = 10", ((ValueTypeInteger.ValueInteger) res3).getRawValue(), is(10));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIntegerCastIntegerLarge() throws EvaluationException {
        Operators.NUMBER_CAST_INTEGER.evaluate(d0, d0);
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputSizeIntegerCastIntegerSmall() throws EvaluationException {
        Operators.NUMBER_CAST_INTEGER.evaluate();
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidInputTypeIntegerCastInteger() throws EvaluationException {
        Operators.NUMBER_CAST_INTEGER.evaluate(DUMMY_VARIABLE);
    }
}
