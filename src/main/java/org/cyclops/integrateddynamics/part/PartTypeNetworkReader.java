package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A reader part that can read aspects from the network it is contained in.
 * @author rubensworks
 */
public class PartTypeNetworkReader extends PartTypeReadBase<PartTypeNetworkReader, PartStateReaderBase<PartTypeNetworkReader>> {

    public PartTypeNetworkReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_NETWORK_APPLICABLE,
                Aspects.READ_INTEGER_NETWORK_ELEMENT_COUNT,
                Aspects.READ_INTEGER_NETWORK_ENERGY_BATTERY_COUNT,
                Aspects.READ_INTEGER_NETWORK_ENERGY_STORED,
                Aspects.READ_INTEGER_NETWORK_ENERGY_MAX
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeNetworkReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeNetworkReader>();
    }

}
