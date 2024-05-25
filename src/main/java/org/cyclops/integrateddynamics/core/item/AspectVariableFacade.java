package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * Variable facade for variables determined by part aspects.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AspectVariableFacade extends VariableFacadeBase implements IAspectVariableFacade {

    private final int partId;
    private final IAspect aspect;

    public AspectVariableFacade(boolean generateId, int partId, IAspect aspect) {
        super(generateId);
        this.partId = partId;
        this.aspect = aspect;
    }

    public AspectVariableFacade(int id, int partId, IAspect aspect) {
        super(id);
        this.partId = partId;
        this.aspect = aspect;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network) {
        if(isValid() && getAspect() instanceof IAspectRead && network.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect())) {
            return network.getPartVariable(getPartId(), (IAspectRead) getAspect());
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return getPartId() >= 0 && getAspect() != null;
    }

    @Override
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (!(getAspect() instanceof IAspectRead
                && network.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect()))) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_PARTNOTINNETWORK,
                    Integer.toString(getPartId())));
        } else if (!ValueHelpers.correspondsTo(containingValueType, getAspect().getValueType())) {
            validator.addError(Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                    Component.translatable(containingValueType.getTranslationKey()),
                    Component.translatable(getAspect().getValueType().getTranslationKey())));
        }
    }

    @Override
    public IValueType getOutputType() {
        IAspect aspect = getAspect();
        if(aspect == null) return null;
        return aspect.getValueType();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Level world) {
        if(isValid()) {
            getAspect().loadTooltip(list, false);
            list.add(Component.translatable(L10NValues.ASPECT_TOOLTIP_PARTID, getPartId()));
        }
        super.appendHoverText(list, world);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, RandomSource random, ModelData modelData) {
        if(isValid()) {
            IAspect aspect = getAspect();
            IValueType valueType = aspect.getValueType();
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.VALUETYPE).getBakedModels().get(valueType).getQuads(null, null, random, modelData, null));
            quads.addAll(variableModelBaked.getSubModels(VariableModelProviders.ASPECT).getBakedModels().get(aspect).getQuads(null, null, random, modelData, null));
        }
    }
}
