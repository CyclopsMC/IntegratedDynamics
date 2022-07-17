package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * Variable facade for variables determined for operators based on other variables in the network determined by their id.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
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

    @Override
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network) {
        if(isValid()) {
            int newNetworkHash = network != null ? network.hashCode() : -1;
            if(expression == null || expression.hasErrored() || newNetworkHash != this.lastNetworkHash) {
                this.lastNetworkHash = newNetworkHash;
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
                    if (this.variables[i]) {
                        return null;
                    }
                    this.variables[i] = true;
                    variables[i] = variableFacade.getVariable(network);
                    this.variables[i] = false;
                    if(variables[i] == null) {
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
    public void validate(IPartNetwork network, final IValidator validator, IValueType containingValueType) {
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
                } else if (!network.hasVariableFacade(variableId)) { // Check id present in network
                    validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                            Integer.toString(variableId)));
                    checkFurther = false;
                } else {
                    // Check variable represented by this id is valid.
                    IVariableFacade variableFacade = network.getVariableFacade(variableId);
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
                        variableFacade.validate(network, new IValidator() {
                            @Override
                            public void addError(MutableComponent error) {
                                validator.addError(error);
                                isValid.set(false);
                            }
                        }, valueType);
                        validatingVariables[i] = false;
                        if (isValid.get()) {
                            IVariable variable = variableFacade.getVariable(network);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Level world) {
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
            list.add(Component.translatable(L10NValues.OPERATOR_TOOLTIP_VARIABLEIDS, sb.toString()));
        }
        super.appendHoverText(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        if(isValid()) {
            IValueType valueType = getOperator().getOutputType();
            BakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType);
            if(bakedModel != null) {
                quads.addAll(bakedModel.getQuads(null, null, random, modelData, null));
            }
        }
    }

}
