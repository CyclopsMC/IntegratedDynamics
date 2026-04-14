package org.cyclops.integrateddynamics.block;

import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.List;

/**
 * Client config for the Menril Leaves block.
 * Registers a constant teal/cyan block tint source so that falling leaf particles
 * produced by {@link net.minecraft.world.level.block.TintedParticleLeavesBlock} are
 * coloured to match the Menril leaf texture (average colour #609198).
 *
 * @author rubensworks
 */
public class BlockMenrilLeavesClientConfig extends BlockClientConfig<IntegratedDynamics> {

    // Bright mid-range teal of the Menril leaves texture (#76B4B8 = R118 G180 B184), stored as
    // a signed Java ARGB int (0xFF76B4B8).  The raw average (#609198) was too dark because
    // it was pulled down by shadow pixels; this value matches the dominant lit colour.
    private static final int MENRIL_LEAF_COLOR = -8997704;

    public BlockMenrilLeavesClientConfig(BlockConfigCommon<IntegratedDynamics> blockConfig) {
        super(blockConfig);
        blockConfig.getMod().getModEventBus().addListener(this::onRegisterColors);
    }

    public void onRegisterColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(MENRIL_LEAF_COLOR)), getBlockConfig().getInstance());
    }
}
