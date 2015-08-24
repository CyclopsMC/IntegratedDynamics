package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.IReadRedstoneComponent;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.ReadRedstoneComponent;

/**
 * A redstone reader part.
 * @author rubensworks
 */
public class PartTypeRedstoneReader extends PartTypeReadBase<PartTypeRedstoneReader, PartStateReaderBase<PartTypeRedstoneReader>> {

    private static final IReadRedstoneComponent READ_REDSTONE_COMPONENT = new ReadRedstoneComponent();

    public PartTypeRedstoneReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_REDSTONE_LOW,
                Aspects.READ_BOOLEAN_REDSTONE_NONLOW,
                Aspects.READ_BOOLEAN_REDSTONE_HIGH,
                Aspects.READ_INTEGER_REDSTONE
        ));
    }

    @Override
    public boolean isSolid(PartStateReaderBase<PartTypeRedstoneReader> state) {
        return true;
    }

    @Override
    public PartStateReaderBase<PartTypeRedstoneReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeRedstoneReader>();
    }

    @Override
    public void onNetworkAddition(Network network, PartTarget target, PartStateReaderBase<PartTypeRedstoneReader> state) {
        super.onNetworkAddition(network, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, true);
    }

    @Override
    public void onNetworkRemoval(Network network, PartTarget target, PartStateReaderBase<PartTypeRedstoneReader> state) {
        super.onNetworkRemoval(network, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, false);
    }

}
