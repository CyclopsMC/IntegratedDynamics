package org.cyclops.integrateddynamics.api.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Optional;

/**
 * @author rubensworks
 */
public record SidedPathElementParams(String dimension, BlockPos pos, Optional<Direction> side) {
    public static final Codec<SidedPathElementParams> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Codec.STRING.fieldOf("dimension").forGetter(e -> e.dimension),
                            BlockPos.CODEC.fieldOf("pos").forGetter(e -> e.pos),
                            Direction.CODEC.optionalFieldOf("side").forGetter(e -> e.side)
                    )
                    .apply(builder, SidedPathElementParams::new)
    );
}
