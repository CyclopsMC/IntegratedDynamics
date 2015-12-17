package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A Minecraft property reader part.
 * @author rubensworks
 */
public class PartTypeMinecraftReader extends PartTypeReadBase<PartTypeMinecraftReader, PartStateReaderBase<PartTypeMinecraftReader>> {

    public PartTypeMinecraftReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_INTEGER_MINECRAFT_RANDOM,
                Aspects.READ_INTEGER_MINECRAFT_PLAYERCOUNT,
                Aspects.READ_INTEGER_MINECRAFT_TICKTIME
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeMinecraftReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeMinecraftReader>();
    }

}
