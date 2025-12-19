package org.cyclops.integrateddynamics.block;

import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityEnergyBatteryRender;

/**
 * @author rubensworks
 */
public class BlockCreativeEnergyBatteryConfigClient extends BlockClientConfig<IntegratedDynamics> {
    public BlockCreativeEnergyBatteryConfigClient(BlockConfigCommon<IntegratedDynamics> blockConfig) {
        super(blockConfig);
        blockConfig.getMod().getModEventBus().addListener((RegisterSpecialModelRendererEvent event) -> event.register(blockConfig.getResourceKey().identifier(), ItemStackBlockEntityEnergyBatteryRender.Unbaked.MAP_CODEC));
    }
}
