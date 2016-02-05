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
                Aspects.Read.Fluid.BOOLEAN_FULL,
                Aspects.Read.Fluid.BOOLEAN_EMPTY,
                Aspects.Read.Fluid.BOOLEAN_NONEMPTY,
                Aspects.Read.Fluid.BOOLEAN_APPLICABLE,
                Aspects.Read.Fluid.INTEGER_AMOUNT,
                Aspects.Read.Fluid.INTEGER_AMOUNTTOTAL,
                Aspects.Read.Fluid.INTEGER_CAPACITY,
                Aspects.Read.Fluid.INTEGER_CAPACITYTOTAL,
                Aspects.Read.Fluid.INTEGER_TANKS,
                Aspects.Read.Fluid.DOUBLE_FILLRATIO,
                Aspects.Read.Fluid.LIST_TANKFLUIDS,
                Aspects.Read.Fluid.LIST_TANKCAPACITIES,
                Aspects.Read.Fluid.FLUIDSTACK
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeFluidReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeFluidReader>();
    }

}
