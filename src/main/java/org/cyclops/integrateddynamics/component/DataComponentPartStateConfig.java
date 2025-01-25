package org.cyclops.integrateddynamics.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.helper.Codecs;

/**
 * @author rubensworks
 */
public class DataComponentPartStateConfig extends DataComponentConfigCommon<CompoundTag, IntegratedDynamics> {

    public DataComponentPartStateConfig() {
        super(IntegratedDynamics._instance, "part_state", builder -> builder
                .persistent(Codecs.COMPOUND_TAG)
                .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
    }
}
