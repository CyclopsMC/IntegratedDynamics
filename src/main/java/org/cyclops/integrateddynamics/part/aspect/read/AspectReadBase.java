package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.part.aspect.LazyAspectVariable;
import org.cyclops.integrateddynamics.part.aspect.AspectBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.function.Supplier;

/**
 * Base class for read aspects.
 * @author rubensworks
 */
public abstract class AspectReadBase<V extends IValue, T extends IValueType<V>> extends AspectBase<V, T>
        implements IAspectRead<V, T> {

    private final String unlocalizedTypeSuffix;
    private final AspectUpdateType updateType;

    public AspectReadBase(String modId, String unlocalizedTypeSuffix,
                          IAspectProperties defaultProperties, AspectUpdateType updateType) {
        super(modId, defaultProperties);
        if(unlocalizedTypeSuffix == null) {
            unlocalizedTypeSuffix = "";
        }
        this.unlocalizedTypeSuffix = unlocalizedTypeSuffix;
        this.updateType = updateType;
        if(MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>> void update(INetwork network, IPartNetwork partNetwork, P partType, PartTarget target, S state) {
        IAspectVariable variable = ((IPartTypeReader) partType).getVariable(target, (IPartStateReader) state, this);
        variable.invalidate();
    }

    protected String getUnlocalizedType() {
        return "read" + this.unlocalizedTypeSuffix;
    }

    @OnlyIn(Dist.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                ResourceLocation.parse(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

    /**
     * This is only called lazy.
     * @param target The target to get the value for.
     * @param properties The optional properties for this aspect.
     * @return The value that will be inserted into a variable so it can be used elsewhere.
     * @throws EvaluationException If evaluation has gone wrong.
     */
    protected abstract V getValue(PartTarget target, IAspectProperties properties) throws EvaluationException;

    @Override
    public IAspectVariable<V> createNewVariable(Supplier<PartTarget> target) {
        return new LazyAspectVariable<V>(getValueType(), target, this) {
            @Override
            public V getValueLazy() throws EvaluationException {
                return AspectReadBase.this.getValue(getTarget(), getAspectProperties());
            }
        };
    }

    @Override
    public AspectUpdateType getUpdateType() {
        return updateType;
    }
}
