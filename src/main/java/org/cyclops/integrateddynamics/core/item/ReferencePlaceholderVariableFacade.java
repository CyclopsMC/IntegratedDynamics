package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stands in for a referenced variable card in the logic programmer, when the actual card is not available,
 * such as after loading an existing card. It has the id of the referenced card, and resolves to that card in a network.
 * @author rubensworks
 */
public class ReferencePlaceholderVariableFacade extends VariableFacadeBase {

    @Nullable
    private final IValueType<?> valueType;

    public ReferencePlaceholderVariableFacade(int id, @Nullable IValueType<?> valueType) {
        super(id);
        this.valueType = valueType;
    }

    /**
     * @return The type of the referenced variable, or null if unknown.
     */
    @Nullable
    public IValueType<?> getValueType() {
        return valueType;
    }

    @Nullable
    protected IVariableFacade getReferencedFacade(IPartNetwork partNetwork) {
        if (partNetwork.hasVariableFacade(getId())) {
            IVariableFacade variableFacade = partNetwork.getVariableFacade(getId());
            if (variableFacade != this && !(variableFacade instanceof ReferencePlaceholderVariableFacade)) {
                return variableFacade;
            }
        }
        return null;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        IVariableFacade variableFacade = getReferencedFacade(partNetwork);
        return variableFacade != null ? variableFacade.getVariable(network, partNetwork) : null;
    }

    @Override
    public boolean isValid() {
        return getId() >= 0;
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        IVariableFacade variableFacade = getReferencedFacade(partNetwork);
        if (variableFacade == null) {
            validator.addError(Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getId())));
        } else {
            variableFacade.validate(network, partNetwork, validator, containingValueType);
        }
    }

    @Override
    public IValueType getOutputType() {
        return valueType != null ? valueType : ValueTypes.CATEGORY_ANY;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Item.TooltipContext context) {
        list.add(Component.translatable(L10NValues.EXPRESSION_TOOLTIP_REFERENCE));
        if (valueType != null) {
            list.add(Component.translatable(L10NValues.EXPRESSION_TOOLTIP_TYPE,
                    Component.translatable(valueType.getTranslationKey()).withStyle(valueType.getDisplayColorFormat())));
        }
        super.appendHoverText(list, context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        if (valueType != null) {
            BakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType);
            if (bakedModel != null) {
                quads.addAll(bakedModel.getQuads(null, null, random, modelData, null));
            }
        }
    }

}
