package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

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
    public static final LogicalOperator LOGICAL_AND = REGISTRY.register(new LogicalOperator("&&", "and", new OperatorBase.IFunction() {
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
    public static final LogicalOperator LOGICAL_OR = REGISTRY.register(new LogicalOperator("||", "or", new OperatorBase.IFunction() {
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
    public static final LogicalOperator LOGICAL_NOT = REGISTRY.register(new LogicalOperator("!", "not", 1, new OperatorBase.IFunction() {
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
    public static final ArithmeticOperator ARITHMETIC_ADDITION = REGISTRY.register(new ArithmeticOperator("+", "addition", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.add(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MINUS operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_SUBTRACTION = REGISTRY.register(new ArithmeticOperator("-", "subtraction", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.subtract(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MULTIPLY operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MULTIPLICATION = REGISTRY.register(new ArithmeticOperator("*", "multiplication", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.multiply(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic DIVIDE operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_DIVISION = REGISTRY.register(new ArithmeticOperator("/", "division", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.divide(variables[0], variables[1]);
        }
    }));

    /**
     * Arithmetic MAX operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MAXIMUM = REGISTRY.register(new ArithmeticOperator("max", "maximum", new OperatorBase.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.max(variables[0], variables[1]);
        }
    }, IConfigRenderPattern.PREFIX_2));

    /**
     * Arithmetic MIN operator with two input integers and one output integer.
     */
    public static final ArithmeticOperator ARITHMETIC_MINIMUM = REGISTRY.register(new ArithmeticOperator("min", "minimum", new OperatorBase.IFunction() {

        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            return ValueTypes.CATEGORY_NUMBER.min(variables[0], variables[1]);
        }
    }, IConfigRenderPattern.PREFIX_2));



    /**
     * ----------------------------------- INTEGER OPERATORS -----------------------------------
     */

    /**
     * Integer MODULO operator with two input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_MODULUS = REGISTRY.register(new IntegerOperator("%", "modulus", new OperatorBase.IFunction() {

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
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_INCREMENT = REGISTRY.register(new IntegerOperator("++", "increment", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a + 1);
        }
    }, IConfigRenderPattern.SUFFIX_1));

    /**
     * Integer INCREMENT operator with one input integers and one output integer.
     */
    public static final IntegerOperator INTEGER_DECREMENT = REGISTRY.register(new IntegerOperator("--", "decrement", 1, new OperatorBase.IFunction() {
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
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_GT = REGISTRY.register(new RelationalOperator(">", "gt", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeBoolean.ValueBoolean.of(a > b);
        }
    }));

    /**
     * Relational &gt; operator with two input integers and one output boolean.
     */
    public static final RelationalOperator RELATIONAL_LT = REGISTRY.register(new RelationalOperator("<", "lt", new OperatorBase.IFunction() {
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
     * Relational &gt;= operator with two inputs of any type (but equal) and one output boolean.
     */
    public static final IOperator RELATIONAL_GE = REGISTRY.register(
            new CompositionalOperator.AppliedOperatorBuilder(LOGICAL_OR).apply(RELATIONAL_EQUALS, RELATIONAL_GT).build(
                    ">=", "ge", IConfigRenderPattern.INFIX, "relational"));

    /**
     * Relational &lt;= operator with two inputs of any type (but equal) and one output boolean.
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
    public static final BinaryOperator BINARY_AND = REGISTRY.register(new BinaryOperator("&", "and", new OperatorBase.IFunction() {
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
    public static final BinaryOperator BINARY_OR = REGISTRY.register(new BinaryOperator("|", "or", new OperatorBase.IFunction() {
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
    public static final BinaryOperator BINARY_XOR = REGISTRY.register(new BinaryOperator("^", "xor", new OperatorBase.IFunction() {
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
    public static final BinaryOperator BINARY_COMPLEMENT = REGISTRY.register(new BinaryOperator("~", "complement", 1, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(~a);
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Binary &lt;&lt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_LSHIFT = REGISTRY.register(new BinaryOperator("<<", "lshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a << b);
        }
    }));

    /**
     * Binary &gt;&gt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RSHIFT = REGISTRY.register(new BinaryOperator(">>", "rshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >> b);
        }
    }));

    /**
     * Binary &gt;&gt;&gt; operator with two input integers and one output integers.
     */
    public static final BinaryOperator BINARY_RZSHIFT = REGISTRY.register(new BinaryOperator(">>>", "rzshift", new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            int a = ((ValueTypeInteger.ValueInteger) variables[0].getValue()).getRawValue();
            int b = ((ValueTypeInteger.ValueInteger) variables[1].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a >>> b);
        }
    }));

    /**
     * ----------------------------------- STRING OPERATORS -----------------------------------
     */

    /**
     * String length operator with one input string and one output integer.
     */
    public static final StringOperator STRING_LENGTH = REGISTRY.register(new StringOperator("len", "length", new IValueType[]{ValueTypes.STRING}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            String a = ((ValueTypeString.ValueString) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of(a.length());
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * String concat operator with two input strings and one output string.
     */
    public static final StringOperator STRING_CONCAT = REGISTRY.register(new StringOperator("+", "concat", 2, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            String a = ((ValueTypeString.ValueString) variables[0].getValue()).getRawValue();
            String b = ((ValueTypeString.ValueString) variables[1].getValue()).getRawValue();
            return ValueTypeString.ValueString.of(a + b);
        }
    }, IConfigRenderPattern.INFIX));

    /**
     * ----------------------------------- DOUBLE OPERATORS -----------------------------------
     */

    /**
     * Double round operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_ROUND = REGISTRY.register(new DoubleOperator("|| ||", "round", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.round(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Double ceil operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_CEIL = REGISTRY.register(new DoubleOperator("⌈ ⌉", "ceil", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.ceil(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

    /**
     * Double floor operator with one input double and one output integers.
     */
    public static final DoubleOperator DOUBLE_FLOOR = REGISTRY.register(new DoubleOperator("⌊ ⌋", "floor", new IValueType[]{ValueTypes.DOUBLE}, ValueTypes.INTEGER, new OperatorBase.IFunction() {
        @Override
        public IValue evaluate(IVariable... variables) throws EvaluationException {
            double a = ((ValueTypeDouble.ValueDouble) variables[0].getValue()).getRawValue();
            return ValueTypeInteger.ValueInteger.of((int) Math.floor(a));
        }
    }, IConfigRenderPattern.PREFIX_1));

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
