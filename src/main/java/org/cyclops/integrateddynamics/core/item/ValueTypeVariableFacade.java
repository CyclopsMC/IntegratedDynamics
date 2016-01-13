package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * Variable facade for variables determined by a raw value.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ValueTypeVariableFacade<V extends IValue> extends VariableFacadeBase implements IValueTypeVariableFacade<V> {

    private final IValueType<V> valueType;
    private final V value;
    private IVariable<V> variable = null;

    public ValueTypeVariableFacade(boolean generateId, IValueType<V> valueType, V value) {
        super(generateId);
        this.valueType = valueType;
        this.value = value;
    }

    public ValueTypeVariableFacade(int id, IValueType<V> valueType, V value) {
        super(id);
        this.valueType = valueType;
        this.value = value;
    }

    public ValueTypeVariableFacade(boolean generateId, IValueType<V> valueType, String value) {
        super(generateId);
        this.valueType = valueType;
        this.value = valueType.deserialize(value);
    }

    public ValueTypeVariableFacade(int id, IValueType<V> valueType, String value) {
        super(id);
        this.valueType = valueType;
        this.value = valueType.deserialize(value);
    }

    @Override
    public IVariable<V> getVariable(IPartNetwork network) {
        if(isValid()) {
            if(variable == null) {
                variable = new Variable<V>(getValueType(), getValue());
            }
            return variable;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return getValueType() != null && getValue() != null;
    }

    @Override
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType) {
        if(!isValid()) {
            validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else {
            // Check expected aspect type and operator output type
            if (!ValueHelpers.correspondsTo(getValueType(), containingValueType)) {
                validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        new L10NHelpers.UnlocalizedString(containingValueType.getUnlocalizedName()),
                        new L10NHelpers.UnlocalizedString(getValueType().getUnlocalizedName())));
            }
        }
    }

    @Override
    public IValueType getOutputType() {
        return getValueType();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(List<String> list, EntityPlayer entityPlayer) {
        if(isValid()) {
            getValueType().loadTooltip(list, false);
            list.add(L10NHelpers.localize(L10NValues.VALUETYPE_TOOLTIP_VALUE, getValueType().toCompactString(getValue())));
        }
        super.addInformation(list, entityPlayer);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads) {
        if(isValid()) {
            IBakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(getValueType());
            if(bakedModel != null) {
                quads.addAll(bakedModel.getGeneralQuads());
            }
        }
    }

}
