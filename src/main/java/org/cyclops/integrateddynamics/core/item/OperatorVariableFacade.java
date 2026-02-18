package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Variable facade for variables determined for operators based on other variables in the network determined by their id.
 * @author rubensworks
 */
public class OperatorVariableFacade extends VariableFacadeBase implements IOperatorVariableFacade {

    private final IOperator operator;
    private final int[] variableIds;
    private IExpression expression = null;
    private int lastNetworkHash = -1;

    // Flags to detect infinite recursion
    private final boolean[] validatingVariables;
    private final boolean[] variables;

    public OperatorVariableFacade(boolean generateId, IOperator operator, int[] variableIds) {
        super(generateId);
        this.operator = operator;
        this.variableIds = variableIds;
        this.validatingVariables = this.variableIds != null ? new boolean[this.variableIds.length] : null;
        this.variables = this.variableIds != null ? new boolean[this.variableIds.length] : null;
    }

    public OperatorVariableFacade(int id, IOperator operator, int[] variableIds) {
        super(id);
        this.operator = operator;
        this.variableIds = variableIds;
        this.validatingVariables = this.variableIds != null ? new boolean[this.variableIds.length] : null;
        this.variables = this.variableIds != null ? new boolean[this.variableIds.length] : null;
    }

    public IOperator getOperator() {
        return operator;
    }

    public int[] getVariableIds() {
        return variableIds;
    }

    public IExpression getExpression() {
        return expression;
    }

    public void setExpression(IExpression expression) {
        this.expression = expression;
    }

    public int getLastNetworkHash() {
        return lastNetworkHash;
    }

    public void setLastNetworkHash(int lastNetworkHash) {
        this.lastNetworkHash = lastNetworkHash;
    }

    public boolean[] getValidatingVariables() {
        return validatingVariables;
    }

    public boolean[] getVariables() {
        return variables;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if(isValid()) {
            int newNetworkHash = network != null ? network.hashCode() : -1;
            if(expression == null || expression.hasErrored() || newNetworkHash != this.lastNetworkHash) {
                this.lastNetworkHash = newNetworkHash;
                IVariable[] variables = new IVariable[variableIds.length];
                for (int i = 0; i < variableIds.length; i++) {
                    int variableId = variableIds[i];
                    if (!partNetwork.hasVariableFacade(variableId)) {
                        return null;
                    }
                    IVariableFacade variableFacade = partNetwork.getVariableFacade(variableId);
                    if(!variableFacade.isValid() || variableFacade == this) {
                        return null;
                    }
                    if (this.variables[i]) {
                        return null;
                    }
                    this.variables[i] = true;
                    variables[i] = variableFacade.getVariable(network, partNetwork);
                    this.variables[i] = false;
                    if(variables[i] == null) {
                        return null;
                    }
                }
                expression = new LazyExpression(getId(), operator, variables, partNetwork);
            }
            return expression;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return getVariableIds() != null && getOperator() != null;
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, final IValidator validator, IValueType containingValueType) {
        if(!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else {
            IValueType[] valueTypes = new IValueType[variableIds.length];
            IVariable[] variables = new IVariable[variableIds.length];
            boolean checkFurther = true;
            for (int i = 0; i < variableIds.length; i++) {
                int variableId = variableIds[i];
                // Check valid id
                if (variableId < 0) {
                    validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
                    checkFurther = false;
                } else if (!partNetwork.hasVariableFacade(variableId)) { // Check id present in network
                    validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                            Integer.toString(variableId)));
                    checkFurther = false;
                } else {
                    // Check variable represented by this id is valid.
                    IVariableFacade variableFacade = partNetwork.getVariableFacade(variableId);
                    if(variableFacade == this) {
                        validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                                Integer.toString(variableId)));
                        checkFurther = false;
                    } else if (variableFacade != null) {
                        IValueType valueType = getOperator().getInputTypes()[i];
                        final Wrapper<Boolean> isValid = new Wrapper<>(true);
                        if (validatingVariables[i]) {
                            validator.addError(Component.translatable(
                                    L10NValues.OPERATOR_ERROR_CYCLICREFERENCE, getId()));
                            checkFurther = false;
                            break;
                        }
                        validatingVariables[i] = true;
                        variableFacade.validate(network, partNetwork, new IValidator() {
                            @Override
                            public void addError(MutableComponent error) {
                                validator.addError(error);
                                isValid.set(false);
                            }
                        }, valueType);
                        validatingVariables[i] = false;
                        if (isValid.get()) {
                            IVariable variable = variableFacade.getVariable(network, partNetwork);
                            if (variable != null) {
                                variables [i] = variable;
                                valueTypes[i] = variable.getType();
                            }
                        } else {
                            checkFurther = false;
                        }
                    }
                }
            }
            if(checkFurther) {
                // Check operator validity
                IOperator op = getOperator();
                MutableComponent error = op.validateTypes(valueTypes);
                if (error != null) {
                    validator.addError(error);
                }
                // Check expected aspect type and operator output type
                IValueType outputType = op.getConditionalOutputType(variables);
                if (!ValueHelpers.correspondsTo(outputType, containingValueType)) {
                    validator.addError(Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                            Component.translatable(containingValueType.getTranslationKey()),
                            Component.translatable(outputType.getTranslationKey())));
                }
            }
        }
    }

    @Override
    public IValueType getOutputType() {
        IOperator operator = getOperator();
        if(operator == null) return null;
        return operator.getOutputType();
    }

    @Override
    protected IVariableFacadeClient constructClient() {
        return new OperatorVariableFacadeClient(this);
    }

    @Override
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        if(isValid()) {
            getOperator().loadTooltip(tooltipAdder, false);
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for(int variableId : getVariableIds()) {
                if(!first) {
                    sb.append(",");
                }
                sb.append(getReferenceDisplay(variableId));
                first = false;
            }
            sb.append("}");
            tooltipAdder.accept(Component.translatable(L10NValues.OPERATOR_TOOLTIP_VARIABLEIDS, sb.toString()));
        }
        super.appendHoverText(tooltipAdder, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OperatorVariableFacade that = (OperatorVariableFacade) o;
        return lastNetworkHash == that.lastNetworkHash && Objects.equals(operator, that.operator) && Objects.deepEquals(variableIds, that.variableIds) && Objects.equals(expression, that.expression) && Objects.deepEquals(validatingVariables, that.validatingVariables) && Objects.deepEquals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operator, Arrays.hashCode(variableIds), expression, lastNetworkHash, Arrays.hashCode(validatingVariables), Arrays.hashCode(variables));
    }

}
