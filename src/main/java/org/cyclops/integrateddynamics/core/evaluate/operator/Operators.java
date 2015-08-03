package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

/**
 * Collection of available operators.
 *
 * @author rubensworks
 */
public final class Operators {

    public static final IOperatorRegistry REGISTRY = constructRegistry();

    private static IOperatorRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IOperatorRegistry.class);
        } else {
            return OperatorRegistry.getInstance();
        }
    }

    public static void load() {}

    /**
     * ----------------------------------- LOGICAL OPERATORS -----------------------------------
     */

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_AND = REGISTRY.register(new LogicalOperator("&&", "and", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if (!a) {
                return ValueTypeBoolean.ValueBoolean.of(false);
            } else {
                return variables[1].getValue();
            }
        }
    }));

    /**
     * Short-circuit logical AND operator with two input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_OR = REGISTRY.register(new LogicalOperator("||", "or", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            if (a) {
                return ValueTypeBoolean.ValueBoolean.of(true);
            } else {
                return variables[1].getValue();
            }
        }
    }));

    /**
     * Logical NOT operator with one input booleans and one output boolean.
     */
    public static final LogicalOperator LOGICAL_NOT = REGISTRY.register(new LogicalOperator("!", "not", 1, new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(!a);
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * ----------------------------------- ARITHMETIC OPERATORS -----------------------------------
     */

    private static final ValueTypeInteger.ValueInteger ZERO = ValueTypeInteger.ValueInteger.of(0);

    /**
     * Arithmetic ADD operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_ADDITION = REGISTRY.register(new ArithmeticOperator("+", "addition", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            if (a == 0) { // If a is neutral element for addition
                return variables[1].getValue();
            } else {
                int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
                if (b == 0) { // If b is neutral element for addition
                    return variables[0].getValue();
                } else {
                    return ValueTypeInteger.ValueInteger.of(a + b);
                }
            }
        }
    }));

    /**
     * Arithmetic MINUS operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(new ArithmeticOperator("-", "subtraction", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            if (b == 0) { // If b is neutral element for subtraction
                return variables[0].getValue();
            } else {
                int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a - b);
            }
        }
    }));

    /**
     * Arithmetic MULTIPLY operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(new ArithmeticOperator("*", "multiplication", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            if (a == 0) { // If a is absorbtion element for multiplication
                return variables[0].getValue();
            } else if (a == 1) { // If a is neutral element for multiplication
                return variables[1].getValue();
            } else {
                int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
                if (b == 1) { // If b is neutral element for multiplication
                    return variables[0].getValue();
                } else {
                    return ValueTypeInteger.ValueInteger.of(a * b);
                }
            }
        }
    }));

    /**
     * Arithmetic DIVIDE operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_DIVISION = REGISTRY.register(new ArithmeticOperator("/", "division", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            if (b == 0) { // You can not divide by zero
                throw new EvaluationException("Division by zero");
            } else if (b == 1) { // If b is neutral element for division
                return variables[0].getValue();
            } else {
                int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a / b);
            }
        }
    }));

    /**
     * Arithmetic MODULO operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MODULUS = REGISTRY.register(new ArithmeticOperator("%", "modulus", new BaseOperator.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            if (b == 0) { // You can not divide by zero
                throw new EvaluationException("Division by zero");
            } else if (b == 1) { // If b is neutral element for division
                return ZERO;
            } else {
                int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
                return ValueTypeInteger.ValueInteger.of(a % b);
            }
        }
    }));

    /**
     * Arithmetic MAX operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MAXIMUM = REGISTRY.register(new ArithmeticOperator("max", "maximum", new BaseOperator.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(Math.max(a, b));
        }
    }, IConfigRenderPattern.PREFIX_2));

    /**
     * Arithmetic MIN operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MINIMUM = REGISTRY.register(new ArithmeticOperator("min", "minimum", new BaseOperator.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(Math.min(a, b));
        }
    }, IConfigRenderPattern.PREFIX_2));

    /**
     * Arithmetic INCREMENT operator with one input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_INCREMENT = REGISTRY.register(new ArithmeticOperator("++", "increment", 1, new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a + 1);
        }
    }, IConfigRenderPattern.SUFFIX_1));

    /**
     * Arithmetic INCREMENT operator with one input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_DECREMENT = REGISTRY.register(new ArithmeticOperator("--", "decrement", 1, new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a - 1);
        }
    }, IConfigRenderPattern.SUFFIX_1));

    /**
     * ----------------------------------- RELATIONAL OPERATORS -----------------------------------
     */

    /**
     * Relational == operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_EQUALS = REGISTRY.register(new RelationalEqualsOperator("==", "equals"));

    /**
     * Relational > operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_GT = REGISTRY.register(new RelationalOperator(">", "gt", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a > b);
        }
    }));

    /**
     * Relational > operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_LT = REGISTRY.register(new RelationalOperator(">", "lt", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a < b);
        }
    }));

    /**
     * Relational != operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_NOTEQUALS = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_NOT).apply(RELATIONAL_EQUALS).build(
                    "!=", "notequals", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational >= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_GE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_GT).build(
                    ">=", "ge", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational <= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_LE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_LT).build(
                    "<=", "le", IConfigRenderPattern.INFIX, "relational"));

    /**
     * ----------------------------------- BINARY OPERATORS -----------------------------------
     */

    /**
     * Binary AND operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_AND = REGISTRY.register(new BinaryOperator("&", "and", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a & b);
        }
    }));

    /**
     * Binary OR operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_OR = REGISTRY.register(new BinaryOperator("|", "or", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a | b);
        }
    }));

    /**
     * Binary XOR operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_XOR = REGISTRY.register(new BinaryOperator("^", "xor", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a ^ b);
        }
    }));

    /**
     * Binary COMPLEMENT operator with one input integers and one output integers.
     */
    public static final BinaryOperator BINARY_COMPLEMENT = REGISTRY.register(new BinaryOperator("~", "complement", 1, new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(~a);
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Binary << operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_LSHIFT = REGISTRY.register(new BinaryOperator("<<", "lshift", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a << b);
        }
    }));

    /**
     * Binary >> operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RSHIFT = REGISTRY.register(new BinaryOperator(">>", "rshift", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >> b);
        }
    }));

    /**
     * Binary >>> operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RZSHIFT = REGISTRY.register(new BinaryOperator(">>>", "rzshift", new BaseOperator.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >>> b);
        }
    }));

    /**
     * ----------------------------------- GENERAL OPERATORS -----------------------------------
     */

    /**
     * Choice operator with one boolean input, two any inputs and one output any.
     */
    public static final GeneralOperator GENERAL_CHOICE = REGISTRY.register(new GeneralChoiceOperator("?", "choice"));

    /**
     * Identity operator with one any input and one any output
     */
    public static final GeneralOperator GENERAL_IDENTITY = REGISTRY.register(new GeneralIdentityOperator("id", "identity"));

}
