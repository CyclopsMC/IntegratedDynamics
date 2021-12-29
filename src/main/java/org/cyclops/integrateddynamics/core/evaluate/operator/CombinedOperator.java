package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.Reference;
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
        super(symbol, operatorName,
                inputTypes, outputType, function, configRenderPattern);
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
                ValueHelpers.validatePredicateOutput(operator, result);
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
                ValueHelpers.validatePredicateOutput(operator, result);
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
            IOperator operator = getOperators()[0];
            IValue result = ValueHelpers.evaluateOperator(operator, value);
            ValueHelpers.validatePredicateOutput(operator, result);
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
            return pipeVariablesToOperators(variables.getVariables(), getOperators());
        }

        /**
         * Pass the first variable to all n-1 first operators.
         * Prepend the results of these operators to the variables array.
         * Pass the final variables array to the last operator, and return the result.
         * @param allVariables The input variables.
         * @param operators The operators to apply to. The n-1 first ones are the inputs, and the last one is the target to pipe to.
         * @return The final result.
         * @throws EvaluationException If evaluation failed.
         */
        public static IValue pipeVariablesToOperators(IVariable[] allVariables, IOperator[] operators) throws EvaluationException {
            int firstInputRange = operators.length - 1;
            IVariable input = allVariables[0];
            IVariable[] intermediates = new IVariable[firstInputRange];
            for (int i = 0; i < firstInputRange; ++i) {
                intermediates[i] = new Variable<>(ValueHelpers.evaluateOperator(operators[i], input));
            }
            IVariable[] remaining = ArrayUtils.subarray(allVariables, 1, allVariables.length);
            IVariable[] newVariables = ArrayUtils.addAll(intermediates, remaining);
            return ValueHelpers.evaluateOperator(operators[operators.length - 1], newVariables);
        }

        /**
         * Determine the input types and output type for the given operators.
         * @param operators The operators to apply to. The n-1 first ones are the inputs, and the last one is the target to pipe to.
         * @return The input types and output type.
         */
        public static Pair<IValueType[], IValueType> getPipedInputOutputTypes(IOperator[] operators) {
            int firstInputRange = operators.length - 1;
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
            return Pair.of(inputTypes, operators[operators.length - 1].getOutputType());

        }

        public static CombinedOperator asOperator(final IOperator... operators) {
            return asOperator(new CombinedOperator.Pipe(operators), ":.:", "piped", operators);
        }

        public static CombinedOperator asOperator(OperatorsFunction function, String symbol, String operatorName, final IOperator... operators) {
            Pair<IValueType[], IValueType> ioTypes = getPipedInputOutputTypes(operators);
            return new CombinedOperator(symbol, operatorName, function, ioTypes.getRight()) {
                @Override
                public IValueType getConditionalOutputType(IVariable[] allVariables) {
                    try {
                        return pipeVariablesToOperators(allVariables, operators).getType();
                    } catch (EvaluationException e) {
                        return ValueTypes.CATEGORY_ANY;
                    }
                }

                @Override
                public IValueType[] getInputTypes() {
                    return ioTypes.getLeft();
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
            return Pipe.pipeVariablesToOperators(variables.getVariables(), getOperators());
        }

        public static CombinedOperator asOperator(IOperator... operators) {
            return Pipe.asOperator(new CombinedOperator.Pipe2(operators), ":.2:", "piped2", operators);
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
                throw new EvaluationException(new TranslatableComponent(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTHVIRTIUAL,
                        new TranslatableComponent(Operators.OPERATOR_FLIP.getTranslationKey()),
                        new TranslatableComponent(operator.getTranslationKey()),
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
                throw new EvaluationException(new TranslatableComponent(e.getMessage()));
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
        public ResourceLocation getUniqueName() {
            return new ResourceLocation(Reference.MOD_ID, "combined." + functionName);
        }

        @Override
        public Tag serialize(CombinedOperator operator) {
            OperatorsFunction function = (OperatorsFunction) operator.getFunction();
            IOperator[] operators = function.getOperators();
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (IOperator functionOperator : operators) {
                CompoundTag elementTag = new CompoundTag();
                elementTag.put("v", Operators.REGISTRY.serialize(functionOperator));
                list.add(elementTag);
            }
            tag.put("operators", list);
            return tag;
        }

        @Override
        public CombinedOperator deserialize(Tag valueOperator) throws EvaluationException {
            ListTag list;
            try {
                CompoundTag tag = (CompoundTag) valueOperator;
                list = (ListTag) tag.get("operators");
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new EvaluationException(new TranslatableComponent(L10NValues.VALUETYPE_ERROR_DESERIALIZE,
                        valueOperator, e.getMessage()));
            }
            IOperator[] operators = new IOperator[list.size()];
            for (int i = 0; i < list.size(); i++) {
                try {
                    operators[i] = Objects.requireNonNull(Operators.REGISTRY.deserialize(list.getCompound(i).get("v")));
                } catch (Throwable e) {
                    // TODO: remove this in next major version (and try-catch block), as we just needed it for backwards-compat.
                    operators[i] = Objects.requireNonNull(Operators.REGISTRY.deserialize(list.get(i)));
                }
            }
            return newFunction(operators);
        }

        public abstract CombinedOperator newFunction(IOperator... operators) throws EvaluationException;
    }
}
