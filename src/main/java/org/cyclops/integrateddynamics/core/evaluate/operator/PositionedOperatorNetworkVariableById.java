package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * An operator that gets a variable by id.
 * @author rubensworks
 */
public class PositionedOperatorNetworkVariableById extends PositionedOperator {

    public PositionedOperatorNetworkVariableById(DimPos pos, Direction side) {
        super("variablebyid", "variablebyid", "variablebyid", new IValueType[]{ValueTypes.INTEGER}, ValueTypes.CATEGORY_ANY, new Function(), IConfigRenderPattern.PREFIX_1, pos, side);
        ((PositionedOperatorNetworkVariableById.Function) this.getFunction()).setOperator(this);
    }

    public PositionedOperatorNetworkVariableById() {
        this(null, null);
        ((PositionedOperatorNetworkVariableById.Function) this.getFunction()).setOperator(this);
    }

    @Override
    protected String getUnlocalizedType() {
        return "virtual";
    }

    public static class Function implements IFunction {

        private PositionedOperatorNetworkVariableById operator;

        public void setOperator(PositionedOperatorNetworkVariableById operator) {
            this.operator = operator;
        }

        public PositionedOperatorNetworkVariableById getOperator() {
            return operator;
        }

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            INetwork network = NetworkHelpers.getNetwork(PartPos.of(getOperator().getPos(), getOperator().getSide())).orElse(null);
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
            if (network != null && partNetwork != null) {
                int variableId = variables.getValue(0, ValueTypes.INTEGER).getRawValue();
                IVariableFacade variableFacade = partNetwork.getVariableFacade(variableId);
                if (variableFacade != null) {
                    return variableFacade.getVariable(network, partNetwork).getValue();
                }
                EvaluationException exception = new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(variableId)));
                exception.setRetryEvaluation(true);
                throw exception;
            }
            EvaluationException exception = new EvaluationException(Component.translatable(L10NValues.GENERAL_ERROR_NONETWORK));
            exception.setRetryEvaluation(true);
            throw exception;
        }
    }

}
