package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.core.part.aspect.LazyAspectVariable;
import org.cyclops.integrateddynamics.core.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Base class for read aspects.
 * @author rubensworks
 */
public abstract class AspectReadBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectRead<V, T> {

    public AspectReadBase() {
        if(MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(Network network, P partType, PartTarget target, S state) {
        if(partType instanceof IPartTypeReader && state instanceof IPartStateReader) {
            IAspectVariable variable = ((IPartTypeReader) partType).getVariable(target, (IPartStateReader) state, this);
            if (variable.requiresUpdate()) {
                variable.update();
            }
        }
    }

    protected String getUnlocalizedType() {
        return "read";
    }

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                new ModelResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

    /**
     * This is only called lazy.
     * @param target The target to get the value for.
     * @return The value that will be inserted into a variable so it can be used elsewhere.
     */
    protected abstract V getValue(PartTarget target);

    @Override
    public IAspectVariable<V> createNewVariable(final PartTarget target) {
        return new LazyAspectVariable<V>(getValueType(), target) {
            @Override
            public V getValueLazy() {
                return AspectReadBase.this.getValue(target);
            }
        };
    }

}
