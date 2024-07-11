package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author rubensworks
 */
public class ValueDeseralizationContextMocked {

    public static ValueDeseralizationContext get() {
        return new ValueDeseralizationContext(new HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
                return Stream.empty();
            }

            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256285_) {
                return Optional.empty();
            }
        });
    }
}
