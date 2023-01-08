package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class ValueDeseralizationContextMocked {

    public static ValueDeseralizationContext get() {
        return new ValueDeseralizationContext(new HolderGetter<Block>() {
            @Override
            public Optional<Holder.Reference<Block>> get(ResourceKey<Block> p_255645_) {
                return Optional.empty();
            }

            @Override
            public Optional<HolderSet.Named<Block>> get(TagKey<Block> p_256283_) {
                return Optional.empty();
            }
        });
    }
}
