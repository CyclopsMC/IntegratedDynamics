package org.cyclops.integrateddynamics.component;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class DataComponentFluidContentInOutConfig extends DataComponentConfig<Pair<SimpleFluidContent, SimpleFluidContent>> {
    public DataComponentFluidContentInOutConfig() {
        super(IntegratedDynamics._instance, "fluid_content_in_out", builder -> builder
                .persistent(RecordCodecBuilder.create(rb ->
                        rb.group(
                            SimpleFluidContent.CODEC.fieldOf("in").forGetter(Pair::getLeft),
                            SimpleFluidContent.CODEC.fieldOf("out").forGetter(Pair::getRight)
                    ).apply(rb, Pair::of)
                ))
                .networkSynchronized(StreamCodec.composite(
                        SimpleFluidContent.STREAM_CODEC, Pair::getLeft,
                        SimpleFluidContent.STREAM_CODEC, Pair::getRight,
                        Pair::of
                )));
    }
}
