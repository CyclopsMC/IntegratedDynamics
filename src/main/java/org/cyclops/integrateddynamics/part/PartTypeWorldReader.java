package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
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
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_WORLD_BLOCK,
                Aspects.READ_BOOLEAN_WORLD_WEATHER_CLEAR,
                Aspects.READ_BOOLEAN_WORLD_WEATHER_RAINING,
                Aspects.READ_BOOLEAN_WORLD_WEATHER_THUNDER,
                Aspects.READ_BOOLEAN_WORLD_ISDAY,
                Aspects.READ_BOOLEAN_WORLD_ISNIGHT,
                Aspects.READ_INTEGER_WORLD_RAINCOUNTDOWN,
                Aspects.READ_INTEGER_WORLD_TICKTIME,
                Aspects.READ_INTEGER_WORLD_DAYTIME,
                Aspects.READ_INTEGER_WORLD_LIGHT_LEVEL,
                Aspects.READ_INTEGER_WORLD_PLAYERCOUNT,
                Aspects.READ_INTEGER_WORLD_DIMENSION,
                Aspects.READ_INTEGER_WORLD_POSX,
                Aspects.READ_INTEGER_WORLD_POSY,
                Aspects.READ_INTEGER_WORLD_POSZ,
                Aspects.READ_LONG_WORLD_TIME,
                Aspects.READ_LONG_WORLD_TOTALTIME,
                Aspects.READ_STRING_WORLD_NAME,
                Aspects.READ_OBJECT_BLOCK_WORLD_BLOCK,
                Aspects.READ_LIST_WORLD_ENTITIES
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeWorldReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeWorldReader>();
    }

}
