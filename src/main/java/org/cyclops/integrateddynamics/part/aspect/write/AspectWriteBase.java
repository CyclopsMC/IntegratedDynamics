package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Base class for write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectWrite<V, T> {

    public AspectWriteBase() {
        if(MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(Network network, P partType, PartTarget target, S state) {
        if(partType instanceof IPartTypeWriter && state instanceof IPartStateWriter) {
            IVariable variable = ((IPartTypeWriter) partType).getActiveVariable(network, target, (IPartStateWriter) state);
            if(variable != null) {
                write((IPartTypeWriter) partType, target, (IPartStateWriter) state, variable);
            }
        }
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state) {

    }

    protected String getUnlocalizedType() {
        return "write";
    }

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                new ModelResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

}
