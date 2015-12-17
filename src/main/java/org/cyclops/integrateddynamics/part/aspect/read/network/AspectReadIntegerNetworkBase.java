package org.cyclops.integrateddynamics.part.aspect.read.network;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Base class for integer Network aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerNetworkBase extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "network." + getUnlocalizedIntegerNetworkType();
    }

    protected abstract String getUnlocalizedIntegerNetworkType();

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        int value = 0;
        if(block instanceof INetworkCarrier) {
            INetwork network = ((INetworkCarrier) block).getNetwork(dimPos.getWorld(), dimPos.getBlockPos());
            if(network != null) {
                value = getValue(network);
            }
        }
        return ValueTypeInteger.ValueInteger.of(value);
    }

    protected abstract int getValue(INetwork network);

}
