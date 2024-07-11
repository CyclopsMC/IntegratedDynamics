package org.cyclops.integrateddynamics.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentProxyIdConfig extends DataComponentConfig<Integer> {

    public DataComponentProxyIdConfig() {
        super(IntegratedDynamics._instance, "proxy_id", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT));
    }
}
