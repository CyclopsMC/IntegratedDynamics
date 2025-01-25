package org.cyclops.integrateddynamics.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentOmnidirectionalGroupConfig extends DataComponentConfigCommon<Integer, IntegratedDynamics> {

    public DataComponentOmnidirectionalGroupConfig() {
        super(IntegratedDynamics._instance, "omnidirectional_group", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT));
    }
}
