package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;

/**
 * Collection of available operators.
 * @author rubensworks
 */
public final class Operators {

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_AND = new LogicalOperator("&&", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if(!a) {
                return ValueTypeBoolean.ValueBoolean.of(false);
            } else {
                return variables[1].getValue();
            }
        }
    });

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_OR = new LogicalOperator("||", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if(a) {
                return ValueTypeBoolean.ValueBoolean.of(true);
            } else {
                return variables[1].getValue();
            }
        }
    });

    /**
     * Logical NOT operator with one input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_NOT = new LogicalOperator("!", 1, new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(!a);
        }
    });

}
