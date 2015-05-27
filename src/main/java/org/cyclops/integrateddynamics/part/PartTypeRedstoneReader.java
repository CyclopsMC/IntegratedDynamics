package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.EnumPartType;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A redstone I/O part.
 * @author rubensworks
 */
public class PartTypeRedstoneReader extends PartTypeBase<PartTypeRedstoneReader, DefaultPartState<PartTypeRedstoneReader>>
        implements IPartTypeReader<PartTypeRedstoneReader, DefaultPartState<PartTypeRedstoneReader>> {

    public PartTypeRedstoneReader() {
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_REDSTONE_LOW,
                Aspects.READ_BOOLEAN_REDSTONE_NONLOW,
                Aspects.READ_BOOLEAN_REDSTONE_HIGH
        ));
    }

    @Override
    public EnumPartType getType() {
        return PartTypes.REDSTONE_READER;
    }

    @Override
    public DefaultPartState<PartTypeRedstoneReader> constructDefaultState() {
        return new DefaultPartState<PartTypeRedstoneReader>();
    }

    @Override
    public int getUpdateInterval(DefaultPartState<PartTypeRedstoneReader> state) {
        return 10;
    }

}
