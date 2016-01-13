package org.cyclops.integrateddynamics.part.aspect.read.network;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;

/**
 * Base class for boolean Network aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanNetworkBase extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "network." + getUnlocalizedBooleanNetworkType();
    }

    protected abstract String getUnlocalizedBooleanNetworkType();

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        boolean value = false;
        if(block instanceof INetworkCarrier) {
            INetwork network = ((INetworkCarrier) block).getNetwork(dimPos.getWorld(), dimPos.getBlockPos());
            if(network != null) {
                value = getValue(network);
            }
        }
        return ValueTypeBoolean.ValueBoolean.of(value);
    }

    protected abstract boolean getValue(INetwork network);

}
