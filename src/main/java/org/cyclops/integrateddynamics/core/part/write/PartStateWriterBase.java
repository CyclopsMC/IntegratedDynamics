package org.cyclops.integrateddynamics.core.part.write;

import com.google.common.collect.Maps;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of the {@link IPartTypeWriter} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class PartStateWriterBase<P extends IPartTypeWriter>
        extends PartStateActiveVariableBase<P> implements IPartStateWriter<P> {
    @NBTPersist
    private String activeAspectName = null;
    @NBTPersist
    private Map<String, List<L10NHelpers.UnlocalizedString>> errorMessages = Maps.newHashMap();

    public PartStateWriterBase(int inventorySize) {
        super(inventorySize);
    }

    @Override
    protected void validate(IPartNetwork network) {
        // Note that this is only called server-side, so these errors are sent via NBT to the client(s).
        if(getActiveAspect() != null) {
            this.currentVariableFacade.validate(network,
                    new PartStateWriterBase.Validator(this, getActiveAspect()), getActiveAspect().getValueType());
        }
    }

    @Override
    protected void onCorruptedState() {
        super.onCorruptedState();
        this.activeAspectName = null;
    }

    @Override
    public boolean hasVariable() {
        return getActiveAspect() != null && getErrors(getActiveAspect()).isEmpty() && super.hasVariable();
    }

    @Override
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect) {
        onVariableContentsUpdated(partType, target);
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null && activeAspect != newAspect) {
            activeAspect.onDeactivate(partType, target, this);
        }
        this.activeAspectName = newAspect == null ? null : newAspect.getUnlocalizedName();
    }

    @Override
    public void onVariableContentsUpdated(P partType, PartTarget target) {
        // Resets the errors for this aspect
        super.onVariableContentsUpdated(partType, target);
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null) {
            addError(activeAspect, null);
        }
    }

    @Override
    public IAspectWrite getActiveAspect() {
        if(this.activeAspectName == null) {
            return null;
        }
        IAspect aspect = Aspects.REGISTRY.getAspect(this.activeAspectName);
        if(!(aspect instanceof IAspectWrite)) {
            return null;
        }
        return (IAspectWrite) aspect;
    }

    @Override
    public List<L10NHelpers.UnlocalizedString> getErrors(IAspectWrite aspect) {
        List<L10NHelpers.UnlocalizedString> errors = errorMessages.get(aspect.getUnlocalizedName());
        if(errors == null) {
            return Collections.emptyList();
        }
        return errors;
    }

    @Override
    public void addError(IAspectWrite aspect, L10NHelpers.UnlocalizedString error) {
        if(error == null) {
            errorMessages.remove(aspect.getUnlocalizedName());
        } else {
            CollectionHelpers.addToMapList(errorMessages, aspect.getUnlocalizedName(), error);
        }
        onDirty();
        sendUpdate(); // We want this error messages to be sent to the client(s).
    }

    @Override
    public Class<? extends IPartState> getPartStateClass() {
        return IPartStateWriter.class;
    }

    public static class Validator implements IVariableFacade.IValidator {

        private final IPartStateWriter state;
        private final IAspectWrite aspect;

        /**
         * Make a new instance
         * @param state The part state.
         * @param aspect The aspect to set the error for.
         */
        public Validator(IPartStateWriter state, IAspectWrite aspect) {
            this.state = state;
            this.aspect = aspect;
        }

        @Override
        public void addError(L10NHelpers.UnlocalizedString error) {
            this.state.addError(aspect, error);
        }

    }

}
