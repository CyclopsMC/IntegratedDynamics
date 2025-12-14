package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the different integer operators.
 * @author rubensworks
 */
public class TestDoubleOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableDouble d16;
    private DummyVariableDouble d2;
    private DummyVariableDouble d3;

    @BeforeEach
    public void before() {
        d16 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(16));
        d2 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(2));
        d3 = new DummyVariableDouble(ValueTypeDouble.ValueDouble.of(3));
    }

    /**
     * ----------------------------------- SQRT -----------------------------------
     */

    @Test
    public void testArithmeticSqrt() throws EvaluationException {
        IValue res1 = Operators.DOUBLE_SQRT.evaluate(new IVariable[]{d16});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("sqrt(16) = 4", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(4D));
    }

    @Test
    public void testInvalidInputSizeSqrtLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_SQRT.evaluate(new IVariable[]{d16, d16}));
    }
    @Test
    public void testInvalidInputSizeSqrtSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_SQRT.evaluate(new IVariable[]{}));
    }
    @Test
    public void testInvalidInputTypeSqrt() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_SQRT.evaluate(new IVariable[]{DUMMY_VARIABLE}));
    }
    /**
     * ----------------------------------- POW -----------------------------------
     */

    @Test
    public void testArithmeticPow() throws EvaluationException {
        IValue res1 = Operators.DOUBLE_POW.evaluate(new IVariable[]{d2, d3});
        assertThat("result is a double", res1, instanceOf(ValueTypeDouble.ValueDouble.class));
        assertThat("pow(2, 3) = 8", ((ValueTypeDouble.ValueDouble) res1).getRawValue(), is(8D));
    }

    @Test
    public void testInvalidInputSizePowLarge() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_POW.evaluate(new IVariable[]{d16, d16, d16}));
    }
    @Test
    public void testInvalidInputSizePowSmall() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_POW.evaluate(new IVariable[]{d16}));
    }
    @Test
    public void testInvalidInputTypePow() throws EvaluationException {
        Assertions.assertThrows(EvaluationException.class, () -> Operators.DOUBLE_POW.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE}));
    }
}
