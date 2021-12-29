package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An world reader part.
 * @author rubensworks
 */
public class PartTypeWorldReader extends PartTypeReadBase<PartTypeWorldReader, PartStateReaderBase<PartTypeWorldReader>> {

    public PartTypeWorldReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.World.BOOLEAN_WEATHER_CLEAR,
                Aspects.Read.World.BOOLEAN_WEATHER_RAINING,
                Aspects.Read.World.BOOLEAN_WEATHER_THUNDER,
                Aspects.Read.World.BOOLEAN_ISDAY,
                Aspects.Read.World.BOOLEAN_ISNIGHT,
                Aspects.Read.World.INTEGER_RAINCOUNTDOWN,
                Aspects.Read.World.INTEGER_TICKTIME,
                Aspects.Read.World.INTEGER_DAYTIME,
                Aspects.Read.World.INTEGER_LIGHTLEVEL,
                Aspects.Read.World.DOUBLE_TPS,
                Aspects.Read.World.LONG_TIME,
                Aspects.Read.World.LONG_TOTALTIME,
                Aspects.Read.World.STRING_NAME,
                Aspects.Read.World.LIST_PLAYERS
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeWorldReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeWorldReader>();
    }
    
    @Override
    public int getConsumptionRate(PartStateReaderBase<PartTypeWorldReader> state) {
        return GeneralConfig.worldReaderBaseConsumption;
    }

}
