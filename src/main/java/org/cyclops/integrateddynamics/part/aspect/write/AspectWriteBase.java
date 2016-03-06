package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Base class for write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectWrite<V, T> {

    protected final String unlocalizedTypeSuffix;

    @Deprecated
    public AspectWriteBase() {
        this(null, null);
    }

    public AspectWriteBase(String unlocalizedTypeSuffix, IAspectProperties defaultProperties) {
        super(defaultProperties);
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
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(IPartNetwork network, P partType, PartTarget target, S state) {
        if(partType instanceof IPartTypeWriter && state instanceof IPartStateWriter
                && ((IPartStateWriter) state).getActiveAspect() == this) {
            IPartStateWriter writerState = (IPartStateWriter) state;
            IVariable variable = ((IPartTypeWriter) partType).getActiveVariable(network, target, writerState);
            if(variable != null
                    && writerState.getErrors(this).isEmpty()
                    && writerState.getActiveAspect().getValueType().correspondsTo(variable.getType())) {
                if(writerState.isDeactivated() || writerState.checkAndResetFirstTick()) {
                    writerState.getActiveAspect().onActivate((IPartTypeWriter) partType, target, writerState);
                }
                try {
                    write((IPartTypeWriter) partType, target, writerState, variable);
                } catch (EvaluationException e) {
                    writerState.addError(this, new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
                    writerState.setDeactivated(true);
                }
            } else if(!((IPartStateWriter) state).isDeactivated()) {
                ((IPartStateWriter) state).getActiveAspect().onDeactivate((IPartTypeWriter) partType, target, (IPartStateWriter) state);
            }
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

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                new ResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

}
