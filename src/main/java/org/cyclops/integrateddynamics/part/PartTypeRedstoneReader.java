package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.GeneralConfig;
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
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.Redstone.BOOLEAN_LOW,
                Aspects.Read.Redstone.BOOLEAN_NONLOW,
                Aspects.Read.Redstone.BOOLEAN_HIGH,
                Aspects.Read.Redstone.BOOLEAN_CLOCK,
                Aspects.Read.Redstone.INTEGER_VALUE,
                Aspects.Read.Redstone.INTEGER_COMPARATOR
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeRedstoneReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeRedstoneReader>();
    }
    
    @Override
    public int getConsumptionRate(PartStateReaderBase<PartTypeRedstoneReader> state) {
        return GeneralConfig.redstoneReaderBaseConsumption;
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateReaderBase<PartTypeRedstoneReader> state) {
        super.onNetworkAddition(network, partNetwork, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, true);
    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, PartStateReaderBase<PartTypeRedstoneReader> state) {
        super.onNetworkRemoval(network, partNetwork, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, false);
    }

}
