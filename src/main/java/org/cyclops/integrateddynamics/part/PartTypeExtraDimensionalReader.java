package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An extra-dimensional property reader part.
 * @author rubensworks
 */
public class PartTypeExtraDimensionalReader extends PartTypeReadBase<PartTypeExtraDimensionalReader, PartStateReaderBase<PartTypeExtraDimensionalReader>> {

    public PartTypeExtraDimensionalReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.ExtraDimensional.INTEGER_RANDOM,
                Aspects.Read.ExtraDimensional.INTEGER_PLAYERCOUNT,
                Aspects.Read.ExtraDimensional.INTEGER_TICKTIME,
                Aspects.Read.ExtraDimensional.DOUBLE_TPS,
                Aspects.Read.ExtraDimensional.LIST_PLAYERS
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeExtraDimensionalReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeExtraDimensionalReader>();
    }
    
    @Override
    public int getConsumptionRate(PartStateReaderBase<PartTypeExtraDimensionalReader> state) {
        return GeneralConfig.extraDimensionalReaderBaseConsumption;
    }

}
