package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

/**
 * Abstract aspect that has an activatable tank
 * @author rubensworks
 */
public abstract class AspectReadIntegerFluidActivatableBase extends AspectReadIntegerFluidBase {

    public static final AspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_TANKID =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.tankid.name");

    @Override
    protected int getValue(FluidTankInfo[] tankInfo, AspectProperties properties) {
        int i = getActiveTank(properties);
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return getDefaultValue();
    }

    protected int getActiveTank(AspectProperties properties) {
        return properties.getValue(PROP_TANKID).getRawValue();
    }

    protected abstract int getValue(FluidTankInfo tankInfo);

    @Override
    protected AspectProperties createDefaultProperties() {
        AspectProperties properties = new AspectProperties(Lists.<AspectPropertyTypeInstance>newArrayList(
                PROP_TANKID
        ));
        properties.setValue(PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        return properties;
    }
}
