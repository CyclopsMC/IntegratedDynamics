package org.cyclops.integrateddynamics.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentPartEnhancementConfig extends DataComponentConfigCommon<Integer, IntegratedDynamics> {

    public DataComponentPartEnhancementConfig() {
        super(IntegratedDynamics._instance, "part_enhancement", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT));
    }
}
