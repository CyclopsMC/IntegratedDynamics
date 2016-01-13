package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.part.aspect.LazyAspectVariable;
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
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(IPartNetwork network, P partType, PartTarget target, S state) {
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
                new ResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

    /**
     * This is only called lazy.
     * @param target The target to get the value for.
     * @param properties The optional properties for this aspect.
     * @return The value that will be inserted into a variable so it can be used elsewhere.
     */
    protected abstract V getValue(PartTarget target, IAspectProperties properties);

    @Override
    public IAspectVariable<V> createNewVariable(final PartTarget target) {
        return new LazyAspectVariable<V>(getValueType(), target, this) {
            @Override
            public V getValueLazy() {
                return AspectReadBase.this.getValue(target, getAspectProperties());
            }
        };
    }

}
