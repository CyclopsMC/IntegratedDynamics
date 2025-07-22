package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.IModHelpers;


/**
 * @author rubensworks
 */
public record ValueDeseralizationContext(HolderLookup.Provider holderLookupProvider) {
    public static ValueDeseralizationContext of(Level level) {
        if (level == null) {
            return new ValueDeseralizationContext(ServerLifecycleHooks.getCurrentServer().registryAccess());
        }
        return new ValueDeseralizationContext(level.registryAccess());
    }

    public static ValueDeseralizationContext of(HolderLookup.Provider holderLookupProvider) {
        return new ValueDeseralizationContext(holderLookupProvider);
    }

    public static ValueDeseralizationContext ofClient() {
        return ValueDeseralizationContextClient.ofClient();
    }

    public static ValueDeseralizationContext ofAllEnabled() {
        if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            return ofClient();
        }
        return new ValueDeseralizationContext(ServerLifecycleHooks.getCurrentServer().registryAccess());
    }
}
