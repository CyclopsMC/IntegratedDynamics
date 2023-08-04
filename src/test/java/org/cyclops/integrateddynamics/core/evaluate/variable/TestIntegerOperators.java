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
public class TestIntegerOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableInteger i0;
    private DummyVariableInteger i1;
    private DummyVariableInteger im10;
    private DummyVariableInteger i10;
    private DummyVariableInteger i15;

    @Before
    public void before() {
        i0   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(0  ));
        i1   = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(1  ));
        im10 = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(-10));
        i10  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(10 ));
        i15  = new DummyVariableInteger(ValueTypeInteger.ValueInteger.of(15 ));
    }

}
