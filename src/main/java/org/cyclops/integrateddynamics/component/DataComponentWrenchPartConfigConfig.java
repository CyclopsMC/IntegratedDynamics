package org.cyclops.integrateddynamics.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.helper.Codecs;

/**
 * @author rubensworks
 */
public class DataComponentWrenchPartConfigConfig extends DataComponentConfig<CompoundTag> {

    public DataComponentWrenchPartConfigConfig() {
        super(IntegratedDynamics._instance, "wrench_part_config", builder -> builder
                .persistent(Codecs.COMPOUND_TAG)
                .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
    }
}
