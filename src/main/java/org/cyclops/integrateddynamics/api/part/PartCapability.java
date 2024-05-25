package org.cyclops.integrateddynamics.api.part;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class PartCapability<T> extends BaseCapability<T, PartTarget> {

    public static <T> PartCapability<T> create(ResourceLocation name, Class<T> typeClass) {
        return (PartCapability<T>) registry.create(name, typeClass, PartTarget.class);
    }

    public static synchronized List<PartCapability<?>> getAll() {
        return registry.getAll();
    }

    // INTERNAL

    // Requires explicitly-typed constructor due to ECJ inference failure.
    private static final CapabilityRegistry<PartCapability<?>> registry = new CapabilityRegistry<PartCapability<?>>((name, type, clazz) -> new PartCapability<>(name, type));

    private PartCapability(ResourceLocation name, Class<T> typeClass) {
        super(name, typeClass, PartTarget.class);
    }

    final Map<IPartType<?, ?>, List<ICapabilityProvider<IPartType<?, ?>, PartTarget, T>>> providers = new IdentityHashMap<>();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(IPartType<?, ?> partType, PartTarget context) {
        for (var provider : providers.getOrDefault(partType, List.of())) {
            var ret = provider.getCapability(partType, context);
            if (ret != null)
                return ret;
        }
        return null;
    }

}
