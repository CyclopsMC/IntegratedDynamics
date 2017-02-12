package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.*;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Objects;

/**
 * An operator that somehow combines one or more operators.
 * @author rubensworks
 */
public class CombinedOperator extends OperatorBase {

    private final String unlocalizedType;

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType outputType) {
        this(symbol, operatorName, function, new IValueType[]{ValueTypes.CATEGORY_ANY}, outputType, IConfigRenderPattern.PREFIX_1);
    }

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType[] inputTypes,
                            IValueType outputType, IConfigRenderPattern configRenderPattern) {
        super(symbol, operatorName, inputTypes,
                outputType, function, configRenderPattern);
        this.unlocalizedType = "virtual";
    }

    @Override
    protected String getUnlocalizedType() {
        return unlocalizedType;
    }

    @Override
    public IOperator materialize() {
        return this;
    }

    public static abstract class OperatorsFunction implements IFunction {

        private final IOperator[] operators;

        public OperatorsFunction(IOperator... operators) {
            this.operators = operators;
        }

        public IOperator[] getOperators() {
            return operators;
        }

        public int getInputOperatorCount() {
            return getOperators().length;
        }
    }

    public static class Conjunction extends OperatorsFunction {

        public Conjunction(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                IValue result = ValueHelpers.evaluateOperator(operator, value);
                if (!((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }
            return ValueTypeBoolean.ValueBoolean.of(true);
        }

        public static CombinedOperator asOperator(IOperator... operators) {
            CombinedOperator.Conjunction conjunction = new CombinedOperator.Conjunction(operators);
            return new CombinedOperator(":&&:", "p_conjunction", conjunction, ValueTypes.BOOLEAN);
        }

        public static class Serializer extends ListOperatorSerializer<Conjunction> {

            public Serializer() {
                super("conjunction", Conjunction.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Conjunction.asOperator(operators);
            }

        }
    }

    public static class Disjunction extends OperatorsFunction {

        public Disjunction(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                IValue result = ValueHelpers.evaluateOperator(operator, value);
                if (((ValueTypeBoolean.ValueBoolean) result).getRawValue()) {
                    return ValueTypeBoolean.ValueBoolean.of(true);
                }
            }
            return ValueTypeBoolean.ValueBoolean.of(false);
        }

        public static CombinedOperator asOperator(IOperator... operators) {
            CombinedOperator.Disjunction disjunction = new CombinedOperator.Disjunction(operators);
            return new CombinedOperator(":||:", "p_disjunction", disjunction, ValueTypes.BOOLEAN);
        }

        public static class Serializer extends ListOperatorSerializer<Conjunction> {

            public Serializer() {
                super("disjunction", Disjunction.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Disjunction.asOperator(operators);
            }

        }
    }

    public static class Negation extends OperatorsFunction {

        public Negation(IOperator operator) {
            super(new IOperator[]{operator});
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            IValue result = ValueHelpers.evaluateOperator(getOperators()[0], value);
            return ValueTypeBoolean.ValueBoolean.of(!((ValueTypeBoolean.ValueBoolean) result).getRawValue());
        }

        public static CombinedOperator asOperator(IOperator operator) {
            CombinedOperator.Negation negation = new CombinedOperator.Negation(operator);
            return new CombinedOperator("!:", "p_negation", negation, ValueTypes.BOOLEAN);
        }

        public static class Serializer extends ListOperatorSerializer<Conjunction> {

            public Serializer() {
                super("negation", Negation.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Negation.asOperator(operators[0]);
            }

        }
    }

    public static class Pipe extends OperatorsFunction {

        public Pipe(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            IValue value = variables.getValue(0);
            for (IOperator operator : getOperators()) {
                value = ValueHelpers.evaluateOperator(operator, value);
            }
            return value;
        }

        public static CombinedOperator asOperator(IOperator... operators) {
            CombinedOperator.Pipe pipe = new CombinedOperator.Pipe(operators);
            return new CombinedOperator(":.:", "piped", pipe, operators[operators.length - 1].getOutputType());
        }

        public static class Serializer extends ListOperatorSerializer<Conjunction> {

            public Serializer() {
                super("pipe", Pipe.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Pipe.asOperator(operators);
            }

        }
    }

    public static class Flip extends OperatorsFunction {

        public Flip(IOperator operator) {
            super(new IOperator[]{operator});
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            int size = variables.getVariables().length;
            IValue[] values = new IValue[size];
            for (int i = 0; i < size; i++) {
                values[size - i - 1] = variables.getValue(i);
            }
            return ValueHelpers.evaluateOperator(getOperators()[0], values);
        }

        public static CombinedOperator asOperator(IOperator operator) throws EvaluationException {
            CombinedOperator.Flip flip = new CombinedOperator.Flip(operator);
            IValueType[] originalInputTypes = operator.getInputTypes();
            IValueType[] flippedInputTypes = new IValueType[originalInputTypes.length];
            for (int i = 0; i < flippedInputTypes.length; i++) {
                flippedInputTypes[flippedInputTypes.length - i - 1] = originalInputTypes[i];
            }
            CombinedOperator combinedOperator;
            try {
                combinedOperator = new CombinedOperator(":flip:", "flipped", flip, flippedInputTypes,
                        operator.getOutputType(), IConfigRenderPattern.INFIX);
            } catch (IllegalArgumentException e) {
                throw new EvaluationException(e.getMessage());
            }
            return combinedOperator;
        }

        public static class Serializer extends ListOperatorSerializer<Conjunction> {

            public Serializer() {
                super("flip", Flip.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) throws EvaluationException {
                return Flip.asOperator(operators[0]);
            }

        }
    }

    public static abstract class ListOperatorSerializer<F extends IFunction> implements IOperatorSerializer<CombinedOperator> {

        private final String functionName;
        private final Class<? extends IFunction> functionClass;

        public ListOperatorSerializer(String functionName, Class<? extends IFunction> functionClass) {
            this.functionName = functionName;
            this.functionClass = functionClass;
        }

        @Override
        public boolean canHandle(IOperator operator) {
            return operator instanceof CombinedOperator && functionClass.isInstance(((CombinedOperator) operator).getFunction());
        }

        @Override
        public String getUniqueName() {
            return "combined." + functionName;
        }

        @Override
        public String serialize(CombinedOperator operator) {
            OperatorsFunction function = (OperatorsFunction) operator.getFunction();
            IOperator[] operators = function.getOperators();
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (IOperator functionOperator : operators) {
                list.appendTag(new NBTTagString(Operators.REGISTRY.serialize(functionOperator)));
            }
            tag.setTag("operators", list);
            return tag.toString();
        }

        @Override
        public CombinedOperator deserialize(String valueOperator) throws EvaluationException {
            NBTTagList list;
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(valueOperator);
                list = tag.getTagList("operators", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal());
            } catch (NBTException e) {
                e.printStackTrace();
                throw new EvaluationException(e.getMessage());
            }
            IOperator[] operators = new IOperator[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                operators[i] = Objects.requireNonNull(Operators.REGISTRY.deserialize(list.getStringTagAt(i)));
            }
            return newFunction(operators);
        }

        public abstract CombinedOperator newFunction(IOperator... operators) throws EvaluationException;
    }
}
