package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An fluid reader part.
 * @author rubensworks
 */
public class PartTypeFluidReader extends PartTypeReadBase<PartTypeFluidReader, PartStateReaderBase<PartTypeFluidReader>> {

    public PartTypeFluidReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_FLUID_FULL,
                Aspects.READ_BOOLEAN_FLUID_EMPTY,
                Aspects.READ_BOOLEAN_FLUID_NONEMPTY,
                Aspects.READ_BOOLEAN_FLUID_APPLICABLE,
                Aspects.READ_BOOLEAN_FLUID_GASEOUS,
                Aspects.READ_INTEGER_FLUID_AMOUNT,
                Aspects.READ_INTEGER_FLUID_AMOUNTTOTAL,
                Aspects.READ_INTEGER_FLUID_CAPACITY,
                Aspects.READ_INTEGER_FLUID_CAPACITYTOTAL,
                Aspects.READ_INTEGER_FLUID_TANKS,
                Aspects.READ_INTEGER_FLUID_DENSITY,
                Aspects.READ_INTEGER_FLUID_LUMINOSITY,
                Aspects.READ_INTEGER_FLUID_TEMPERATURE,
                Aspects.READ_INTEGER_FLUID_VISCOSITY,
                Aspects.READ_DOUBLE_FLUID_FILLRATIO,
                Aspects.READ_STRING_FLUID_NAME,
                Aspects.READ_STRING_FLUID_RARITY,
                Aspects.READ_BLOCK_FLUID_BLOCK
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeFluidReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeFluidReader>();
    }

}
