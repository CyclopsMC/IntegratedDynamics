package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
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
import java.util.Random;

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

    public ValueTypeVariableFacade(boolean generateId, IValueType<V> valueType, INBT value) {
        super(generateId);
        this.valueType = valueType;
        this.value = ValueHelpers.deserializeRaw(valueType, value);
    }

    public ValueTypeVariableFacade(int id, IValueType<V> valueType, INBT value) {
        super(id);
        this.valueType = valueType;
        this.value = ValueHelpers.deserializeRaw(valueType, value);
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
            validator.addError(new TranslationTextComponent(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else {
            // Check expected aspect type and operator output type
            if (!ValueHelpers.correspondsTo(getValueType(), containingValueType)) {
                validator.addError(new TranslationTextComponent(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        new TranslationTextComponent(containingValueType.getTranslationKey()),
                        new TranslationTextComponent(getValueType().getTranslationKey())));
            }
        }
    }

    @Override
    public IValueType getOutputType() {
        return getValueType();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(List<ITextComponent> list, World world) {
        if(isValid()) {
            V value = getValue();
            getValueType().loadTooltip(list, false, value);
            list.add(new TranslationTextComponent(L10NValues.VALUETYPE_TOOLTIP_VALUE, getValueType().toCompactString(value)));
        }
        super.addInformation(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, Random random, IModelData modelData) {
        if(isValid()) {
            IBakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(getValueType());
            if(bakedModel != null) {
                quads.addAll(bakedModel.getQuads(null, null, random, modelData));
            }
        }
    }

}
