package org.cyclops.integrateddynamics.core.item;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
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

    public ValueTypeVariableFacade(boolean generateId, IValueType<V> valueType, ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        super(generateId);
        this.valueType = valueType;
        this.value = ValueHelpers.deserializeRaw(valueDeseralizationContext, valueType, value);
    }

    public ValueTypeVariableFacade(int id, IValueType<V> valueType, ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        super(id);
        this.valueType = valueType;
        this.value = ValueHelpers.deserializeRaw(valueDeseralizationContext, valueType, value);
    }

    @Override
    public IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
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
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        if(!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else {
            // Check expected aspect type and operator output type
            if (!ValueHelpers.correspondsTo(getValueType(), containingValueType)) {
                validator.addError(Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        Component.translatable(containingValueType.getTranslationKey()),
                        Component.translatable(getValueType().getTranslationKey())));
            }
        }
    }

    @Override
    public IValueType getOutputType() {
        return getValueType();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Level world) {
        if(isValid()) {
            V value = getValue();
            getValueType().loadTooltip(list, false, value);
            list.add(Component.translatable(L10NValues.VALUETYPE_TOOLTIP_VALUE, getValueType().toCompactString(value)));
        }
        super.appendHoverText(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        if(isValid()) {
            BakedModel bakedModel = variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(getValueType());
            if(bakedModel != null) {
                quads.addAll(bakedModel.getQuads(null, null, random, modelData, null));
            }
        }
    }

    @Nullable
    @Override
    public BakedModel getVariableItemOverrideModel(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity) {
        if(isValid()) {
            return getValueType().getVariableItemOverrideModel(getValue(), model, stack, world, livingEntity);
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderISTER(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if(isValid()) {
            getValueType().renderISTER(getValue(), stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
        }
    }
}
