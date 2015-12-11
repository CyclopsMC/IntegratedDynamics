package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * An operator composed of a number of other operators.
 * @author rubensworks
 */
public class CompositionalOperator extends OperatorBase {

    private final String unlocalizedType;

    protected CompositionalOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                    IFunction function, IConfigRenderPattern renderPattern, String unlocalizedType) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
        this.unlocalizedType = unlocalizedType;
    }

    @Override
    protected String getUnlocalizedType() {
        return unlocalizedType;
    }

    public static class AppliedOperatorBuilder {

        private final IOperator base;
        private int buildersRequiredInputLength = -1;
        private AppliedOperatorBuilder[] builders = null;

        public AppliedOperatorBuilder(IOperator base) {
            this.base = base;
        }

        public AppliedOperatorBuilder apply(IOperator... operators) {
            AppliedOperatorBuilder[] builders = new AppliedOperatorBuilder[operators.length];
            for(int i = 0; i < operators.length; i++) {
                builders[i] = new AppliedOperatorBuilder(operators[i]);
            }
            return apply(builders);
        }

        public AppliedOperatorBuilder apply(AppliedOperatorBuilder... builders) {
            IValueType[] valueTypes = new IValueType[builders.length];
            for(int i = 0 ; i < builders.length; i++) {
                IOperator operator = builders[i].base;
                valueTypes[i] = operator.getOutputType();
            }
            L10NHelpers.UnlocalizedString error = this.base.validateTypes(valueTypes);
            if(error != null) {
                throw new IllegalArgumentException(error.localize());
            }
            this.builders = builders;
            return this;
        }

        protected int getRequiredInputLength() {
            if(buildersRequiredInputLength == -1) {
                if(this.builders != null) {
                    this.buildersRequiredInputLength = Integer.MAX_VALUE;
                    for(AppliedOperatorBuilder builder : builders) {
                        this.buildersRequiredInputLength = Math.min(this.buildersRequiredInputLength, builder.getRequiredInputLength());
                    }
                } else {
                    this.buildersRequiredInputLength = this.base.getRequiredInputLength();
                }
            }
            return buildersRequiredInputLength;
        }

        protected IValueType[] getInputTypes() {
            IValueType[] inputTypes;
            if(this.builders == null) {
                inputTypes = this.base.getInputTypes();
            } else {
                inputTypes = new IValueType[getRequiredInputLength()];
                for(AppliedOperatorBuilder builder : builders) {
                    IValueType[] subInputTypes = builder.getInputTypes();
                    for(int i = 0; i < subInputTypes.length; i++) {
                        if(inputTypes[i] != null && !inputTypes[i].correspondsTo(subInputTypes[i])) {
                            throw new IllegalArgumentException(String.format("An composed operator expected type %s " +
                                    "at position %s, while a type of %s was found.", inputTypes[i], i, subInputTypes[i]));
                        } else if(inputTypes[i] == null) {
                            inputTypes[i] = subInputTypes[i];
                        }
                    }
                }
            }
            return inputTypes;
        }

        @SuppressWarnings("unchecked")
        protected IValue evaluate(final IVariable... variables) throws EvaluationException {
            if(this.builders == null) {
                return this.base.evaluate(variables);
            } else {
                IVariable[] subVariablesOut = new IVariable[builders.length];
                for(int i = 0; i < builders.length; i++) {
                    final AppliedOperatorBuilder builder = builders[i];
                    // Anonymous class because we want lazy evaluation
                    subVariablesOut[i] = new IVariable() {
                        @Override
                        public IValueType getType() {
                            return builder.base.getOutputType();
                        }

                        @Override
                        public IValue getValue() throws EvaluationException {
                            return builder.evaluate(variables);
                        }
                    };
                }
                return this.base.evaluate(subVariablesOut);
            }
        }

        protected L10NHelpers.UnlocalizedString validateTypes(String unlocalizedOperatorName, IValueType[] input) {
            if(this.builders == null) {
                return this.base.validateTypes(input);
            } else {
                if(input.length != getRequiredInputLength()) {
                    return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                            new L10NHelpers.UnlocalizedString(unlocalizedOperatorName),
                            input.length, getRequiredInputLength());
                }
                IValueType[] subValueTypesOut = new IValueType[builders.length];
                for(int i = 0; i < builders.length; i++) {
                    AppliedOperatorBuilder builder = builders[i];
                    L10NHelpers.UnlocalizedString subError;
                    if((subError = builder.validateTypes(unlocalizedOperatorName, input)) != null) {
                        return subError;
                    }
                    subValueTypesOut[i] = builder.base.getOutputType();
                }
                return this.base.validateTypes(subValueTypesOut);
            }
        }

        /**
         * Make a new operator based on the applied elements.
         * @param symbol The symbol for the operator.
         * @param operatorName The operator name.
         * @param renderPattern The config render pattern.
         * @param unlocalizedType The unlocalized type name.
         * @return The built operator.
         */
        public IOperator build(String symbol, String operatorName, IConfigRenderPattern renderPattern, String unlocalizedType) {
            return new CompositionalOperator(symbol, operatorName, getInputTypes(), this.base.getOutputType(), new IFunction() {
                @Override
                public IValue evaluate(IVariable... variables) throws EvaluationException {
                    return AppliedOperatorBuilder.this.evaluate(variables);
                }
            }, renderPattern, unlocalizedType) {
                @Override
                public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
                    return AppliedOperatorBuilder.this.validateTypes(getUnlocalizedName(), input);
                }
            };
        }

    }

}
