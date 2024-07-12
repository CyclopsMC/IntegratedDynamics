package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;


/**
 * @author rubensworks
 */
public record ValueDeseralizationContext(HolderLookup.Provider holderLookupProvider) {
    public static ValueDeseralizationContext of(Level level) {
        if (level == null) {
            return ofAllEnabled();
        }
        return new ValueDeseralizationContext(level.registryAccess());
    }

    public static ValueDeseralizationContext of(HolderLookup.Provider holderLookupProvider) {
        return new ValueDeseralizationContext(holderLookupProvider);
    }

    @OnlyIn(Dist.CLIENT)
    public static ValueDeseralizationContext ofClient() {
        return of(Minecraft.getInstance().level);
    }

    public static ValueDeseralizationContext ofAllEnabled() {
        if (MinecraftHelpers.isClientSide()) {
            return ofClient();
        }
        return new ValueDeseralizationContext(ServerLifecycleHooks.getCurrentServer().registryAccess());
    }
}
