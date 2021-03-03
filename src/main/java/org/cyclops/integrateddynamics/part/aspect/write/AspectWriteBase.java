package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
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
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Base class for write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectWrite<V, T> {

    protected final String unlocalizedTypeSuffix;

    public AspectWriteBase(ModBase mod, String unlocalizedTypeSuffix, IAspectProperties defaultProperties) {
        super(mod, defaultProperties);
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
        if(variable != null
                && writerState.getErrors(this).isEmpty()
                && ValueHelpers.correspondsTo(getValueType(), variable.getType())) {
            if(writerState.isDeactivated() || writerState.checkAndResetFirstTick()) {
                onActivate(partTypeWriter, target, writerState);
            }
            try {
                write(partTypeWriter, target, writerState, variable);
            } catch (EvaluationException e) {
                writerState.addError(this, e.getErrorMessage());
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

    protected String getUnlocalizedType() {
        return "write" + unlocalizedTypeSuffix;
    }

    @OnlyIn(Dist.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                new ResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

}
