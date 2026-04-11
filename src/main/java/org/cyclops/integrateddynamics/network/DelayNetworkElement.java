package org.cyclops.integrateddynamics.network;

import net.minecraft.resources.Identifier;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;

/**
 * Network element for delays.
 * @author rubensworks
 */
public class DelayNetworkElement extends ProxyNetworkElement {

    public static final Identifier GROUP = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "delay");

    public DelayNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public Identifier getGroup() {
        return DelayNetworkElement.GROUP;
    }

    @Override
    public int getConsumptionRate() {
        return GeneralConfig.delayerBaseConsumption;
    }

}
