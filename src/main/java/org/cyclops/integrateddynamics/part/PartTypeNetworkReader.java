package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
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
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.Network.BOOLEAN_APPLICABLE,
                Aspects.Read.Network.INTEGER_ELEMENT_COUNT,
                Aspects.Read.Network.INTEGER_ENERGY_BATTERY_COUNT,
                Aspects.Read.Network.INTEGER_ENERGY_STORED,
                Aspects.Read.Network.INTEGER_ENERGY_MAX,
                Aspects.Read.Network.INTEGER_ENERGY_CONSUMPTION_RATE
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeNetworkReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeNetworkReader>();
    }

}
