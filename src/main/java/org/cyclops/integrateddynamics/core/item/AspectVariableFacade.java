package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.network.Network;

import java.util.List;

/**
 * Variable facade for variables determined by part aspects.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AspectVariableFacade extends VariableFacadeBase {

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
    public <V extends IValue> IVariable<V> getVariable(Network network) {
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
    public void validate(Network network, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (!(getAspect() instanceof IAspectRead
                && network.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect()))) {
            validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.VARIABLE_ERROR_PARTNOTINNETWORK,
                    Integer.toString(getPartId())));
        } else if (!ValueHelpers.correspondsTo(containingValueType, getAspect().getValueType())) {
            validator.addError(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                    new L10NHelpers.UnlocalizedString(containingValueType.getUnlocalizedName()),
                    new L10NHelpers.UnlocalizedString(getAspect().getValueType().getUnlocalizedName())));
        }
    }

    @Override
    public IValueType getOutputType() {
        return getAspect().getValueType();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(List<String> list, EntityPlayer entityPlayer) {
        if(isValid()) {
            getAspect().loadTooltip(list, false);
            list.add(L10NHelpers.localize(L10NValues.ASPECT_TOOLTIP_PARTID, getPartId()));
        }
        super.addInformation(list, entityPlayer);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addModelOverlay(VariableModelBaked variableModelBaked, List<BakedQuad> quads) {
        if(isValid()) {
            IAspect aspect = getAspect();
            IValueType valueType = aspect.getValueType();
            quads.addAll(variableModelBaked.getValueTypeSubModels().get(valueType).getGeneralQuads());
            quads.addAll(variableModelBaked.getAspectSubModels().get(aspect).getGeneralQuads());
        }
    }
}
