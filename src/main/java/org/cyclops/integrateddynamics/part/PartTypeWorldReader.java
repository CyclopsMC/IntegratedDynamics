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
                Aspects.Read.World.BOOLEAN_BLOCK,
                Aspects.Read.World.BOOLEAN_WEATHER_CLEAR,
                Aspects.Read.World.BOOLEAN_WEATHER_RAINING,
                Aspects.Read.World.BOOLEAN_WEATHER_THUNDER,
                Aspects.Read.World.BOOLEAN_ISDAY,
                Aspects.Read.World.BOOLEAN_ISNIGHT,
                Aspects.Read.World.INTEGER_RAINCOUNTDOWN,
                Aspects.Read.World.INTEGER_TICKTIME,
                Aspects.Read.World.INTEGER_DAYTIME,
                Aspects.Read.World.INTEGER_LIGHTLEVEL,
                Aspects.Read.World.INTEGER_PLAYERCOUNT,
                Aspects.Read.World.INTEGER_DIMENSION,
                Aspects.Read.World.INTEGER_POSX,
                Aspects.Read.World.INTEGER_POSY,
                Aspects.Read.World.INTEGER_POSZ,
                Aspects.Read.World.INTEGER_ITEMFRAMEROTATION,
                Aspects.Read.World.LONG_TIME,
                Aspects.Read.World.LONG_TOTALTIME,
                Aspects.Read.World.STRING_NAME,
                Aspects.Read.World.BLOCK,
                Aspects.Read.World.LIST_ENTITIES,
                Aspects.Read.World.LIST_PLAYERS,
                Aspects.Read.World.ITEMSTACK_ITEMFRAMECONTENTS
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeWorldReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeWorldReader>();
    }

}
