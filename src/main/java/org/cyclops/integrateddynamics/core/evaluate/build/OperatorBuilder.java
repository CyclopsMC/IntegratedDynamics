package org.cyclops.integrateddynamics.core.evaluate.build;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.IOperatorValuePropagator;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeSmartFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.helper.Helpers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable builder for operators.
 * Appending the kinds "a", "b" and "c" for example will result in the base localization key of "operator.operators.MOD_ID.a.b.c.".
 * This base key then will be suffixed with {"name", "basename"}.
 * If the operator name would be "xyz", then you'll also need the localization keys "operator.operators.MOD_ID.a.b.c.xyz."{"name", "info"}.
 *
 * The actual operator function can either be set by calling {@link OperatorBuilder#function} or by
 * doing any number of calls to {@link OperatorBuilder#handle(IOperatorValuePropagator)} with value propagators.
 * @author rubensworks
 */
public class OperatorBuilder<O> {

    private final IValueType outputType;
    private final String symbol;
    private final String operatorName;
    private final IValueType[] inputTypes;
    private final OperatorBase.ISmartFunction function;
    private final IConfigRenderPattern renderPattern;
    private final String modId;
    private final List<String> kinds;
    private final IConditionalOutputTypeDeriver conditionalOutputTypeDeriver;
    private final ITypeValidator typeValidator;
    private final List<IOperatorValuePropagator> valuePropagators;

    protected OperatorBuilder(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                              OperatorBase.ISmartFunction function, IConfigRenderPattern renderPattern, String modId,
                              List<String> kinds, IConditionalOutputTypeDeriver conditionalOutputTypeDeriver,
                              ITypeValidator typeValidator, List<IOperatorValuePropagator> valuePropagators) {
        this.symbol = symbol;
        this.operatorName = operatorName;
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.function = function;
        this.renderPattern = renderPattern;
        this.modId = modId;
        this.kinds = kinds;
        this.conditionalOutputTypeDeriver = conditionalOutputTypeDeriver;
        this.typeValidator = typeValidator;
        this.valuePropagators = valuePropagators;
    }

    /**
     * Set the operator output value type.
     * @param outputType The output value type.
     * @return The builder instance.
     */
    public OperatorBuilder<O> output(IValueType outputType) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the operator symbol.
     * @param symbol The symbol for the operator.
     * @return The builder instance.
     */
    public OperatorBuilder<O> symbol(String symbol) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the operator name, used for localization.
     * @param operatorName The operator name.
     * @return The builder instance.
     */
    public OperatorBuilder<O> operatorName(String operatorName) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the symbol and operator name to the given value.
     * The symbol is used for display while the operator name is used for localization.
     * @param symbolOperator The symbol and operator name.
     * @return The builder instance.
     */
    public OperatorBuilder<O> symbolOperator(String symbolOperator) {
        return new OperatorBuilder<>(symbolOperator, symbolOperator, inputTypes, outputType, function, renderPattern,
                modId, kinds, conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the input types for the operator.
     * @param inputTypes Array of value types.
     * @return The builder instance.
     */
    public OperatorBuilder<O> inputTypes(IValueType[] inputTypes) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set input types to a given amount of the given value type.
     * @param length The amount of input types.
     * @param defaultType The input type to replicate `length` times.
     * @return The builder instance.
     */
    public OperatorBuilder<O> inputTypes(int length, IValueType defaultType) {
        return new OperatorBuilder<>(symbol, operatorName, OperatorBase.constructInputVariables(length, defaultType),
                outputType, function, renderPattern, modId, kinds, conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set a single input type.
     * @param inputType The input type.
     * @return The builder instance.
     */
    public OperatorBuilder<O> inputType(IValueType inputType) {
        return inputTypes(1, inputType);
    }

    /**
     * Set the function the operator should use.
     * @param function The function.
     * @return The builder instance.
     */
    public OperatorBuilder<O> function(OperatorBase.ISmartFunction function) {
        if(this.valuePropagators != null) {
            throw new IllegalStateException("Can not add a function when value propagators are present.");
        }
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the render pattern for this operator in guis.
     * @param renderPattern The render pattern.
     * @return The builder instance.
     */
    public OperatorBuilder<O> renderPattern(IConfigRenderPattern renderPattern) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the mod id, by default this will be the Integrated Dynamics mod id.
     * @param modId The mod id.
     * @return The builder instance.
     */
    public OperatorBuilder<O> modId(String modId) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Append a localization key element.
     * @param kind The string to append.
     * @return The builder instance.
     */
    public OperatorBuilder<O> appendKind(String kind) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId,
                Helpers.joinList(kinds, kind), conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the conditional output type deriver.
     * @param conditionalOutputTypeDeriver The output type deriver based on certain input.
     *                                     This will be used for {@link IOperator#getConditionalOutputType(IVariable[])}.
     * @return The builder instance.
     */
    public OperatorBuilder<O> conditionalOutputTypeDeriver(IConditionalOutputTypeDeriver conditionalOutputTypeDeriver) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId,
                kinds, conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Set the type validator.
     * @param typeValidator The type validator. This will be used for {@link IOperator#validateTypes(IValueType[])}.
     * @return The builder instance.
     */
    public OperatorBuilder<O> typeValidator(ITypeValidator typeValidator) {
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId,
                kinds, conditionalOutputTypeDeriver, typeValidator, valuePropagators);
    }

    /**
     * Add a value propagator.
     * @param valuePropagator The value propagator.
     * @return The builder instance.
     */
    public <O2> OperatorBuilder<O2> handle(IOperatorValuePropagator<O, O2> valuePropagator) {
        if(this.function != null) {
            throw new IllegalStateException("Can not add a function when value propagators are present.");
        }
        return new OperatorBuilder<>(symbol, operatorName, inputTypes, outputType, function, renderPattern, modId, kinds,
                conditionalOutputTypeDeriver, typeValidator, Helpers.joinList(valuePropagators, valuePropagator));
    }

    /**
     * Build an operator from the current builder state.
     * @return The built operator
     */
    public IOperator build() {
        return new Built(this);
    }

    /**
     * Create a new builder with the given output type.
     * @param outputType The output type.
     * @return The builder instance.
     */
    public static OperatorBuilder<OperatorBase.SafeVariablesGetter> forType(IValueType<?> outputType) {
        return new OperatorBuilder<>(null, null, null, outputType, null, null, Reference.MOD_ID,
                Collections.<String>emptyList(), null, null, null);
    }

    private static class Built extends OperatorBase {

        private final String modId;
        private final String unlocalizedType;
        private final IConditionalOutputTypeDeriver conditionalOutputTypeDeriver;
        private final ITypeValidator typeValidator;

        protected Built(OperatorBuilder operatorBuilder) {
            super(Objects.requireNonNull(operatorBuilder.symbol),
                    Objects.requireNonNull(operatorBuilder.operatorName),
                    Objects.requireNonNull(operatorBuilder.inputTypes),
                    Objects.requireNonNull(operatorBuilder.outputType),
                    Objects.requireNonNull(deriveFunction(operatorBuilder)),
                    Objects.requireNonNull(operatorBuilder.renderPattern));
            this.modId = Objects.requireNonNull(operatorBuilder.modId);
            this.unlocalizedType = deriveUnlocalizedType(operatorBuilder);
            this.conditionalOutputTypeDeriver = operatorBuilder.conditionalOutputTypeDeriver;
            this.typeValidator = operatorBuilder.typeValidator;
        }

        protected static ISmartFunction deriveFunction(OperatorBuilder operatorBuilder) {
            if(operatorBuilder.valuePropagators != null) {
                return new IterativeSmartFunction(operatorBuilder.valuePropagators);
            } else {
                return Objects.requireNonNull(operatorBuilder.function);
            }
        }

        protected static String deriveUnlocalizedType(OperatorBuilder<?> operatorBuilder) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(String kind : operatorBuilder.kinds) {
                if(!first) {
                    sb.append(".");
                }
                first = false;
                sb.append(kind);
            }
            return sb.toString();
        }

        @Override
        protected String getModId() {
            return this.modId;
        }

        @Override
        protected String getUnlocalizedType() {
            return this.unlocalizedType;
        }

        @Override
        public IValueType getConditionalOutputType(IVariable[] input) {
            return conditionalOutputTypeDeriver != null
                    ? conditionalOutputTypeDeriver.getConditionalOutputType(this, input)
                    : super.getConditionalOutputType(input);
        }

        @Override
        public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
            return typeValidator != null
                    ? typeValidator.validateTypes(this, input)
                    : super.validateTypes(input);
        }
    }

    public static interface IConditionalOutputTypeDeriver {

        public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input);

    }

    public static interface ITypeValidator {

        /**
         * Check the given input value types for this operator.
         * @param operator The operator that is being validated.
         * @param input The ordered input value types.
         * @return An error or null if valid.
         */
        public L10NHelpers.UnlocalizedString validateTypes(OperatorBase operator, IValueType[] input);

    }

}
