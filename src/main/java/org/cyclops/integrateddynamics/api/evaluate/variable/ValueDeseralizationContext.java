package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.BlockHelpers;


/**
 * @author rubensworks
 */
public record ValueDeseralizationContext(HolderGetter<Block> holderGetter) {
    public static ValueDeseralizationContext of(Level level) {
        if (level == null) {
            return ofAllEnabled();
        }
        return new ValueDeseralizationContext(level.holderLookup(Registries.BLOCK));
    }

    @OnlyIn(Dist.CLIENT)
    public static ValueDeseralizationContext ofClient() {
        return of(Minecraft.getInstance().level);
    }

    public static ValueDeseralizationContext ofAllEnabled() {
        return new ValueDeseralizationContext(BlockHelpers.HOLDER_GETTER_FORGE);
    }
}
