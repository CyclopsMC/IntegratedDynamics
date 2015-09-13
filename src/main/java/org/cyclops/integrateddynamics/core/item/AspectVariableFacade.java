package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;

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
        if (getPartId() < 0) {
            validator.addError(new L10NHelpers.UnlocalizedString("variable.error.invalidItem"));
        } else if (!(getAspect() instanceof IAspectRead
                && network.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect()))) {
            validator.addError(new L10NHelpers.UnlocalizedString("aspect.error.partNotInNetwork",
                    Integer.toString(getPartId())));
        } else if (!ValueHelpers.correspondsTo(containingValueType, getAspect().getValueType())) {
            validator.addError(new L10NHelpers.UnlocalizedString("aspect.error.invalidType",
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
            list.add(L10NHelpers.localize("aspect.tooltip.partId", getPartId()));
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
