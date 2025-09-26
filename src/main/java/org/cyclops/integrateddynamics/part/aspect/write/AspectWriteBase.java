package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Base class for write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectWrite<V, T> {

    protected final String unlocalizedTypeSuffix;

    public AspectWriteBase(String modId, String unlocalizedTypeSuffix, IAspectProperties defaultProperties) {
        super(modId, defaultProperties);
        if(unlocalizedTypeSuffix == null) {
            unlocalizedTypeSuffix = "";
        }
        this.unlocalizedTypeSuffix = unlocalizedTypeSuffix;
        if(MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(INetwork network, IPartNetwork partNetwork, P partType, PartTarget target, S state) {
        IPartTypeWriter partTypeWriter = (IPartTypeWriter) partType;
        IPartStateWriter writerState = (IPartStateWriter) state;
        IVariable variable = partTypeWriter.getActiveVariable(network, partNetwork, target, writerState);
        if(variable != null && writerState.getErrors(this).isEmpty()) {
            if (ValueHelpers.correspondsTo(variable, getValueType())) {
                if(writerState.isDeactivated() || writerState.checkAndResetFirstTick()) {
                    onActivate(partTypeWriter, target, writerState);
                }
                try {
                    write(partTypeWriter, target, writerState, variable);
                } catch (EvaluationException e) {
                    writerState.addError(this, e.getErrorMessage());
                    writerState.setDeactivated(true);
                    e.addResolutionListeners(() -> writerState.onVariableContentsUpdated(partTypeWriter, target));
                }
            } else {
                // This will only occur in cases where the variable is of any type, and the value type is more precise.
                // In all other cases, type checking will already have happen using the variable facades.
                try {
                    writerState.addError(this, Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        Component.translatable(getValueType().getTranslationKey()),
                        Component.translatable(variable.getValue().getType().getTranslationKey())));
                } catch (EvaluationException e) {
                    // Fallback to a less precise form of error reporting
                    writerState.addError(this, Component.translatable(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                            Component.translatable(getValueType().getTranslationKey()),
                            Component.translatable(variable.getType().getTranslationKey())));
                }
                writerState.setDeactivated(true);
            }
        } else if(!writerState.isDeactivated()) {
            onDeactivate(partTypeWriter, target, writerState);
        }
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
        state.setDeactivated(false);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType, PartTarget target, S state) {
        state.setDeactivated(true);
    }

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setProperties(P partType, PartTarget target, S state, IAspectProperties properties) {
        onDeactivate((IPartTypeWriter) partType, target, (IPartStateWriter) state);
        super.setProperties(partType, target, state, properties);
        onActivate((IPartTypeWriter) partType, target, (IPartStateWriter) state);
    }

    protected String getUnlocalizedType() {
        return "write" + unlocalizedTypeSuffix;
    }

    @OnlyIn(Dist.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                ResourceLocation.parse(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

}
