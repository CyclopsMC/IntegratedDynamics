package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;

import java.util.List;

/**
 * Variable facade for variables determined for operators based on other variables in the network determined by their id.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperatorVariableFacade extends VariableFacadeBase {

    private final IOperator operator;
    private final int[] variableIds;
    private IExpression expression = null;

    public OperatorVariableFacade(boolean generateId, IOperator operator, int[] variableIds) {
        super(generateId);
        this.operator = operator;
        this.variableIds = variableIds;
    }

    public OperatorVariableFacade(int id, IOperator operator, int[] variableIds) {
        super(id);
        this.operator = operator;
        this.variableIds = variableIds;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(Network network) {
        if(isValid()) {
            if(expression == null || expression.hasErrored()) {
                IVariable[] variables = new IVariable[variableIds.length];
                for (int i = 0; i < variableIds.length; i++) {
                    int variableId = variableIds[i];
                    if (!network.hasVariableFacade(variableId)) {
                        return null;
                    }
                    IVariableFacade variableFacade = network.getVariableFacade(variableId);
                    if(!variableFacade.isValid() || variableFacade == this) {
                        return null;
                    }
                    variables[i] = variableFacade.getVariable(network);
                    if(variables[i] == this /* Cyclic reference */ || variables[i] == null) {
                        return null;
                    }
                }
                expression = new LazyExpression(getId(), operator, variables, network);
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
    public void validate(Network network, IPartStateWriter validator) {
        if(this.variableIds == null) {
            validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("variable.error.invalidItem"));
        } else {
            IValueType[] valueTypes = new IValueType[variableIds.length];
            boolean checkFurther = true;
            for (int i = 0; i < variableIds.length; i++) {
                int variableId = variableIds[i];
                // Check valid id
                if (variableId < 0) {
                    validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("variable.error.invalidItem"));
                    checkFurther = false;
                } else if (!network.hasVariableFacade(variableId)) { // Check id present in network
                    validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("operator.error.variableNotInNetwork",
                            Integer.toString(variableId)));
                    checkFurther = false;
                } else {
                    // Check variable represented by this id is valid.
                    IVariableFacade variableFacade = network.getVariableFacade(variableId);
                    if(variableFacade == this) {
                        validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("operator.error.cyclicReference",
                                Integer.toString(variableId)));
                        checkFurther = false;
                    } else if (variableFacade != null) {
                        variableFacade.validate(network, validator);
                        if (variableFacade.isValid()) {
                            IVariable variable = variableFacade.getVariable(network);
                            if (variable != null) {
                                valueTypes[i] = variable.getType();
                            }
                        }
                    }
                }
            }
            if(checkFurther) {
                // Check operator validity
                IOperator op = getOperator();
                L10NHelpers.UnlocalizedString error = op.validateTypes(valueTypes);
                if (error != null) {
                    validator.addError(validator.getActiveAspect(), error);
                }
                // Check expected aspect type and operator output type
                if (validator.getActiveAspect().getValueType() != op.getOutputType()) {
                    validator.addError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("aspect.error.invalidType",
                            new L10NHelpers.UnlocalizedString(validator.getActiveAspect().getValueType().getUnlocalizedName()),
                            new L10NHelpers.UnlocalizedString(op.getOutputType().getUnlocalizedName())));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(List<String> list, EntityPlayer entityPlayer) {
        if(isValid()) {
            getOperator().loadTooltip(list, false);
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
            list.add(L10NHelpers.localize("operator.tooltip.variableIds", sb.toString()));
        }
        super.addInformation(list, entityPlayer);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addModelOverlay(VariableModelBaked variableModelBaked, List<BakedQuad> quads) {
        if(isValid()) {
            IValueType valueType = getOperator().getOutputType();
            quads.addAll(variableModelBaked.getValueTypeSubModels().get(valueType).getGeneralQuads());
        }
    }

}
