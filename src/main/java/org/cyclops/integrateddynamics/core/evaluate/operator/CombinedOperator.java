package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An operator that somehow combines one or more operators.
 * @author rubensworks
 */
public class CombinedOperator extends OperatorBase {

    private final String unlocalizedType;

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType outputType) {
        this(symbol, operatorName, function, new IValueType[]{ValueTypes.CATEGORY_ANY}, outputType, null);
    }

    public CombinedOperator(String symbol, String operatorName, OperatorsFunction function, IValueType[] inputTypes,
                            IValueType outputType, @Nullable IConfigRenderPattern configRenderPattern) {
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

        public static class Serializer extends ListOperatorSerializer<Disjunction> {

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

        public static class Serializer extends ListOperatorSerializer<Negation> {

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
            return pipeVariablesToOperators(1, variables.getVariables(), getOperators());
        }

        public static IValue pipeVariablesToOperators(int firstInputRange, IVariable[] allVariables, IOperator[] operators) throws EvaluationException {
            IVariable input = allVariables[0];
            IVariable[] intermediates = new IVariable[firstInputRange];
            for (int i = 0; i < firstInputRange; ++i) {
                intermediates[i] = new Variable<>(ValueHelpers.evaluateOperator(operators[i], input));
            }
            IVariable[] remaining = ArrayUtils.subarray(allVariables, 1, allVariables.length);
            IVariable[] newVariables = ArrayUtils.addAll(intermediates, remaining);
            return ValueHelpers.evaluateOperator(operators[operators.length - 1], newVariables);
        }

        public static CombinedOperator asOperator(final IOperator... operators) {
            return asOperator(new CombinedOperator.Pipe(operators), ":.:", "piped", 1, operators);
        }

        public static CombinedOperator asOperator(OperatorsFunction function, String symbol, String operatorName, final int firstInputRange, final IOperator... operators) {
            return new CombinedOperator(symbol, operatorName, function, operators[operators.length - 1].getOutputType()) {
                @Override
                public IValueType getConditionalOutputType(IVariable[] allVariables) {
                    try {
                        return pipeVariablesToOperators(firstInputRange, allVariables, operators).getType();
                    } catch (EvaluationException e) {
                        return ValueTypes.CATEGORY_ANY;
                    }
                }

                @Override
                public IValueType[] getInputTypes() {
                    IValueType[] inputTypes = new IValueType[1];
                    for (int i = 0; i < operators.length; ++i) {
                        IValueType[] operatorInputTypes = operators[i].getInputTypes();
                        if (i < firstInputRange) {
                            if (inputTypes[0] == null) {
                                inputTypes[0] = operatorInputTypes[0];
                            } else {
                                if (inputTypes[0] != operatorInputTypes[0]) {
                                    if (ValueHelpers.correspondsTo(inputTypes[0], operatorInputTypes[0])) {
                                        if (inputTypes[0].isCategory()) {
                                            inputTypes[0] = operatorInputTypes[0];
                                        }
                                    }
                                }
                            }
                        } else {
                            inputTypes = ArrayUtils.addAll(inputTypes,
                                    ArrayUtils.subarray(operatorInputTypes, firstInputRange, operatorInputTypes.length));
                        }
                    }
                    return inputTypes;
                }
            };
        }

        public static class Serializer extends ListOperatorSerializer<Pipe> {

            public Serializer() {
                super("pipe", Pipe.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Pipe.asOperator(operators);
            }

        }
    }

    public static class Pipe2 extends OperatorsFunction {

        public Pipe2(IOperator... operators) {
            super(operators);
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            return Pipe.pipeVariablesToOperators(2, variables.getVariables(), getOperators());
        }

        public static CombinedOperator asOperator(IOperator... operators) {
            return Pipe.asOperator(new CombinedOperator.Pipe2(operators), ":.2:", "piped2", 2, operators);
        }

        public static class Serializer extends ListOperatorSerializer<Pipe2> {

            public Serializer() {
                super("pipe2", Pipe2.class);
            }

            @Override
            public CombinedOperator newFunction(IOperator... operators) {
                return Pipe2.asOperator(operators);
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
                int targetI = i < 2 ? 1 - i : i;
                values[i] = variables.getValue(targetI);
            }
            return ValueHelpers.evaluateOperator(getOperators()[0], values);
        }

        public static CombinedOperator asOperator(IOperator operator) throws EvaluationException {
            CombinedOperator.Flip flip = new CombinedOperator.Flip(operator);
            IValueType[] originalInputTypes = operator.getInputTypes();
            IValueType[] flippedInputTypes = new IValueType[originalInputTypes.length];
            if (originalInputTypes.length < 2) {
                throw new EvaluationException(L10NHelpers.localize(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTHVIRTIUAL,
                        L10NHelpers.localize(Operators.OPERATOR_FLIP.getTranslationKey()),
                        L10NHelpers.localize(operator.getTranslationKey()),
                        originalInputTypes.length, 2));
            }
            for (int i = 0; i < flippedInputTypes.length; i++) {
                int targetI = i < 2 ? 1 - i : i;
                flippedInputTypes[i] = originalInputTypes[targetI];
            }
            CombinedOperator combinedOperator;
            try {
                combinedOperator = new CombinedOperator(":flip:", "flipped", flip, flippedInputTypes,
                        operator.getOutputType(), null);
            } catch (IllegalArgumentException e) {
                throw new EvaluationException(e.getMessage());
            }
            return combinedOperator;
        }

        public static class Serializer extends ListOperatorSerializer<Flip> {

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
        private final Class<F> functionClass;

        public ListOperatorSerializer(String functionName, Class<F> functionClass) {
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
