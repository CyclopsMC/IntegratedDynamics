package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Variable facade for variables determined by part aspects.
 * @author rubensworks
 */
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

    public int getPartId() {
        return partId;
    }

    public IAspect getAspect() {
        return aspect;
    }

    @Override
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if(isValid() && getAspect() instanceof IAspectRead && partNetwork.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect())) {
            return partNetwork.getPartVariable(getPartId(), (IAspectRead) getAspect());
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return getPartId() >= 0 && getAspect() != null;
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
        if (!isValid()) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (!(getAspect() instanceof IAspectRead
                && partNetwork.hasPartVariable(getPartId(), (IAspectRead<IValue, ?>) getAspect()))) {
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

    @Override
    protected IVariableFacadeClient constructClient() {
        return new AspectVariableFacadeClient(this);
    }

    @Override
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        if(isValid()) {
            getAspect().loadTooltip(tooltipAdder, false);
            tooltipAdder.accept(Component.translatable(L10NValues.ASPECT_TOOLTIP_PARTID, getPartId()));
        }
        super.appendHoverText(tooltipAdder, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AspectVariableFacade that = (AspectVariableFacade) o;
        return partId == that.partId && Objects.equals(aspect, that.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), partId, aspect);
    }
}
