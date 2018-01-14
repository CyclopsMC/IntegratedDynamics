package org.cyclops.integrateddynamics.network;

import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.Reference;

/**
 * Network element for delays.
 * @author rubensworks
 */
public class DelayNetworkElement extends ProxyNetworkElement {

    public static final ResourceLocation GROUP = new ResourceLocation(Reference.MOD_ID, "delay");

    public DelayNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public ResourceLocation getGroup() {
        return DelayNetworkElement.GROUP;
    }

}
