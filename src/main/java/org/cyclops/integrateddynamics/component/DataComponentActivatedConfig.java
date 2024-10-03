package org.cyclops.integrateddynamics.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentActivatedConfig extends DataComponentConfig<Boolean> {
    public DataComponentActivatedConfig() {
        super(IntegratedDynamics._instance, "activated", builder -> builder
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL));
    }
}
