package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.CombinedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test complex currying cases
 * @author rubensworks
 */
public class TestCurryingComplex {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }
    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableBoolean bFalse;
    private DummyVariableBoolean bTrue;

    private DummyVariableInteger i4;
    private DummyVariableInteger i8;

    @Before
    public void before() {
        ValueTypeListProxyFactories.load();

        bFalse = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(false));
        bTrue  = new DummyVariableBoolean(ValueTypeBoolean.ValueBoolean.of(true));

        i4 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(4));
        i8 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(8));
    }

    /**
     * ----------------------------------- APPLY -----------------------------------
     */

    // Discovered in https://github.com/CyclopsMC/IntegratedDynamics/issues/754
    @Test
    public void testBind2() throws EvaluationException {
        DummyVariableOperator oApply2 = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.OPERATOR_APPLY_2));
        DummyVariableOperator oFlip = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.OPERATOR_FLIP));

        IValue oApply2Flipped = Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oApply2});
        assertThat("result is an operator", oApply2Flipped, instanceOf(ValueTypeOperator.ValueOperator.class));
        assertThat("result is a combined operator", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue(), instanceOf(CombinedOperator.class));
        assertThat("result accepts 3 inputs", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue().getRequiredInputLength(), is(3));
        assertThat("result has a correct first input", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue().getInputTypes()[0], is(ValueTypes.CATEGORY_ANY));
        assertThat("result has a correct second input", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue().getInputTypes()[1], is(ValueTypes.OPERATOR));
        assertThat("result has a correct third input", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue().getInputTypes()[2], is(ValueTypes.CATEGORY_ANY));
        assertThat("result has a any as output", ((ValueTypeOperator.ValueOperator) oApply2Flipped).getRawValue().getOutputType(), is(ValueTypes.CATEGORY_ANY));

        IValue oBind2 = Operators.OPERATOR_PIPE.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oApply2Flipped), oFlip});
        assertThat("result is an operator", oBind2, instanceOf(ValueTypeOperator.ValueOperator.class));
        assertThat("result is a combined operator", ((ValueTypeOperator.ValueOperator) oBind2).getRawValue(), instanceOf(CombinedOperator.class));
        assertThat("result accepts 1 input", ((ValueTypeOperator.ValueOperator) oBind2).getRawValue().getRequiredInputLength(), is(1));
        assertThat("result has a correct first input", ((ValueTypeOperator.ValueOperator) oBind2).getRawValue().getInputTypes()[0], is(ValueTypes.CATEGORY_ANY));

        IValue oBound4_8 = Operators.OPERATOR_APPLY_2.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oBind2), i4, i8});
        assertThat("result is an operator", oBound4_8, instanceOf(ValueTypeOperator.ValueOperator.class));
        assertThat("result is a curried operator", ((ValueTypeOperator.ValueOperator) oBound4_8).getRawValue(), instanceOf(CurriedOperator.class));
        assertThat("result accepts a single input", ((ValueTypeOperator.ValueOperator) oBound4_8).getRawValue().getRequiredInputLength(), is(1));

        DummyVariableOperator oAdd = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION));

        IValue iResult = Operators.OPERATOR_APPLY.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oBound4_8), oAdd});
        assertThat("result is an integer", iResult, instanceOf(ValueTypeInteger.ValueInteger.class));
        assertThat("result is 12", ((ValueTypeInteger.ValueInteger) iResult).getRawValue(), is(12));
    }

    @Test(expected = EvaluationException.class)
    public void testBind2Overflow() throws EvaluationException {
        // When more outputs are passed to an operator than it can handle, so they have to be passed to the next one.
        DummyVariableOperator oApply2 = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.OPERATOR_APPLY_2));
        DummyVariableOperator oFlip = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.OPERATOR_FLIP));

        IValue oApply2Flipped = Operators.OPERATOR_FLIP.evaluate(new IVariable[]{oApply2});

        IValue oBind2 = Operators.OPERATOR_PIPE.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oApply2Flipped), oFlip});

        IValue oBound4_8 = Operators.OPERATOR_APPLY_2.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oBind2), i4, i8});

        DummyVariableOperator oAdd = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.ARITHMETIC_ADDITION));

        Operators.OPERATOR_APPLY_2.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oBound4_8), oAdd, oAdd});
    }

    @Test(expected = EvaluationException.class)
    public void testOmegaOperator() throws EvaluationException {
        // This is supposed to throw an EvaluationException, and not result in infinite recursion
        DummyVariableOperator oId = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.GENERAL_IDENTITY));
        DummyVariableOperator oApply = new DummyVariableOperator(ValueTypeOperator.ValueOperator.of(Operators.OPERATOR_APPLY));

        IValue oX = Operators.OPERATOR_PIPE2.evaluate(new IVariable[]{oId, oId, oApply});
        Operators.OPERATOR_APPLY.evaluate(new IVariable[]{new DummyVariable(ValueTypes.OPERATOR, oX), new DummyVariable(ValueTypes.OPERATOR, oX)});
    }

}
