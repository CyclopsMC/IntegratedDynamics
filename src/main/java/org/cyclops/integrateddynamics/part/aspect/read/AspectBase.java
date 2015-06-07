package org.cyclops.integrateddynamics.part.aspect.read;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;

/**
 * Base class for boolean aspects.
 * @author rubensworks
 */
public abstract class AspectBase<V extends IValue, T extends IValueType<V>> implements IAspect<V, T> {

    public AspectBase() {
        if(MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @Override
    public String getUnlocalizedName() {
        return "aspect.aspects." + getModId() + "." + getUnlocalizedType();
    }

    protected abstract String getUnlocalizedType();

    @Override
    public void loadTooltip(List<String> lines) {

    }

    @SideOnly(Side.CLIENT)
    protected void registerModelResourceLocation() {
        Aspects.REGISTRY.registerAspectModel(this,
                new ModelResourceLocation(getModId() + ":aspect/" + getUnlocalizedType().replaceAll("\\.", "/")));
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

}
