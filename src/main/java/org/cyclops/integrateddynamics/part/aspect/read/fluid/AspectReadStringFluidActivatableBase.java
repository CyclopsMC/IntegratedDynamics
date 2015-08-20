package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import com.google.common.collect.Sets;
import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

/**
 * Abstract aspect that has an activatable tank
 * @author rubensworks
 */
public abstract class AspectReadStringFluidActivatableBase extends AspectReadStringFluidBase {

    @Override
    protected String getValue(FluidTankInfo[] tankInfo, AspectProperties properties) {
        int i = getActiveTank(properties);
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return "";
    }

    protected int getActiveTank(AspectProperties properties) {
        return properties.getValue(AspectReadIntegerFluidActivatableBase.PROP_TANKID).getRawValue();
    }

    protected abstract String getValue(FluidTankInfo tankInfo);

    @Override
    protected AspectProperties createDefaultProperties() {
        AspectProperties properties = new AspectProperties(Sets.<AspectPropertyTypeInstance>newHashSet(
                AspectReadIntegerFluidActivatableBase.PROP_TANKID
        ));
        properties.setValue(AspectReadIntegerFluidActivatableBase.PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        return properties;
    }
}
