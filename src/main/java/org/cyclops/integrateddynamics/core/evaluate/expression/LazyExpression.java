package org.cyclops.integrateddynamics.core.evaluate.expression;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;

/**
 * A generic expression with arbitrarily nested binary operations.
 * This is evaluated in a lazy manner.
 * @author rubensworks
 */
public class LazyExpression implements IExpression {

    private final IOperator op;
    private final IVariable[] input;

    private LazyExpression(IOperator op, IVariable[] input) {
        this.op = op;
        this.input = input;
    }

    @Override
    public IValue evaluate() throws EvaluationException {
        return op.evaluate(input);
    }
}
