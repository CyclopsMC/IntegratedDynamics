package org.cyclops.integrateddynamics.core.part.write;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
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
 * A default implementation of the {@link IPartTypeWriter}.
 * @author rubensworks
 */
public class PartStateWriterBase<P extends IPartTypeWriter>
        extends PartStateActiveVariableBase<P> implements IPartStateWriter<P> {

    private IAspectWrite activeAspect = null;
    /**
     * Whether {@link IAspectWrite#onActivate} has been called for {@link #activeAspect}.
     * This is not persisted, as aspects are always deactivated when their network is killed.
     */
    private boolean activeAspectActivated = false;
    private Map<String, List<Component>> errorMessages = Maps.newHashMap();
    private boolean firstTick = true;

    public PartStateWriterBase(int inventorySize) {
        super(inventorySize);
    }

    @Override
    public void serialize(ValueOutput valueOutput) {
        if (this.activeAspect != null) valueOutput.putString("activeAspectName", this.activeAspect.getUniqueName().toString());
        NBTClassType.getType(Map.class, this.errorMessages).writePersistedField("errorMessages", this.errorMessages, valueOutput);
        super.serialize(valueOutput);
    }

    @Override
    public void deserialize(ValueInput valueInput) {
        valueInput.getString("activeAspectName").ifPresent(activeAspect -> {
            IAspect aspect = Aspects.REGISTRY.getAspect(Identifier.parse(activeAspect));
            if (aspect instanceof IAspectWrite) {
                this.activeAspect = (IAspectWrite) aspect;
            }
        });
        this.errorMessages = (Map<String, List<Component>>) NBTClassType.getType(Map.class, this.errorMessages).readPersistedField("errorMessages", valueInput);
        super.deserialize(valueInput);
    }

    @Override
    protected void validate(INetwork network, IPartNetwork partNetwork) {
        // Note that this is only called server-side, so these errors are sent via NBT to the client(s).
        if(getActiveAspect() != null) {
            this.currentVariableFacade.validate(network, partNetwork,
                    new PartStateWriterBase.Validator(this, getActiveAspect()), getActiveAspect().getValueType());
        }
    }

    @Override
    protected void onCorruptedState() {
        super.onCorruptedState();
        this.activeAspect = null;
        this.activeAspectActivated = false;
    }

    @Override
    public boolean hasVariable() {
        return getActiveAspect() != null && getErrors(getActiveAspect()).isEmpty() && super.hasVariable();
    }

    @Override
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect, boolean isNetworkInitializing) {
        if (!isNetworkInitializing) {
            // We skip network content updates during network init,
            // as it will be called once for all parts right after network init.
            // This is to avoid re-updating variable contents many times during network init, which can get expensive.
            onVariableContentsUpdated(partType, target);
        }

        // Aspects are activated and deactivated at most once,
        // as networks are killed and revived without the aspect itself changing.
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null && this.activeAspectActivated && activeAspect != newAspect) {
            activeAspect.onDeactivate(partType, target, this);
            this.activeAspectActivated = false;
        }
        if(newAspect != null && !this.activeAspectActivated) {
            newAspect.onActivate(partType, target, this);
            this.activeAspectActivated = true;
        }

        // Only forget the aspect outside of network (re)initialization.
        // Otherwise, a part that is saved while its network is being killed
        // would persist a null aspect, and lose its configuration after a world restart.
        if(newAspect != null || !isNetworkInitializing) {
            this.activeAspect = newAspect;
        }
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
        return activeAspect;
    }

    @Override
    public List<Component> getErrors(IAspectWrite aspect) {
        List<Component> errors = errorMessages.get(aspect.getUniqueName().toString());
        if(errors == null) {
            return Collections.emptyList();
        }
        return errors;
    }

    @Override
    public void addError(IAspectWrite aspect, MutableComponent error) {
        if(error == null) {
            errorMessages.remove(aspect.getUniqueName().toString());
        } else {
            CollectionHelpers.addToMapList(errorMessages, aspect.getUniqueName().toString(), error);
        }
        onDirty();
        sendUpdate(); // We want this error messages to be sent to the client(s).
    }

    @Override
    public boolean checkAndResetFirstTick() {
        if(firstTick) {
            firstTick = false;
            return true;
        }
        return false;
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
        public void addError(MutableComponent error) {
            this.state.addError(aspect, error);
        }

    }

}
