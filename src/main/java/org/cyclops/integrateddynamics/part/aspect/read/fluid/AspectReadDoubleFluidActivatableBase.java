package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

/**
 * Abstract aspect that has an activatable tank
 * @author rubensworks
 */
public abstract class AspectReadDoubleFluidActivatableBase extends AspectReadDoubleFluidBase {

    public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_TANKID =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.tankid.name");

    @Override
    protected double getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        int i = getActiveTank(properties);
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return getDefaultValue();
    }

    protected int getActiveTank(IAspectProperties properties) {
        return properties.getValue(PROP_TANKID).getRawValue();
    }

    protected abstract double getValue(FluidTankInfo tankInfo);

    @Override
    protected IAspectProperties createDefaultProperties() {
        IAspectProperties properties = new AspectProperties(Lists.<IAspectPropertyTypeInstance>newArrayList(
                PROP_TANKID
        ));
        properties.setValue(PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        return properties;
    }
}
