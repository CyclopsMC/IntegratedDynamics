package org.cyclops.integrateddynamics.core.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.cyclops.integrateddynamics.api.path.SidedPathElementParams;

import java.util.List;

/**
 * @author rubensworks
 */
public record NetworkParams(List<SidedPathElementParams> pathElements, boolean crashed) {
    public static final Codec<NetworkParams> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Codec.list(SidedPathElementParams.CODEC).fieldOf("pathElements").forGetter(e -> e.pathElements),
                            Codec.BOOL.fieldOf("crashed").forGetter(e -> e.crashed)
                    )
                    .apply(builder, NetworkParams::new)
    );
}
