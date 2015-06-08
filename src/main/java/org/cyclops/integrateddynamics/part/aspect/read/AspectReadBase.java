package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
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

    @Override
    public <P extends IPartType<P, S>, S extends IPartState<P>>  void update(P partType, PartTarget target, S state) {
        IAspectVariable variable = partType.getVariable(target, state, this);
        if (variable.requiresUpdate()) {
            variable.update();
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

}
