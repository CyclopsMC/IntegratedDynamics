package org.cyclops.integrateddynamics.block;

import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityEnergyBatteryRender;

/**
 * @author rubensworks
 */
public class BlockEnergyBatteryConfigClient extends BlockClientConfig<IntegratedDynamics> {
    public BlockEnergyBatteryConfigClient(BlockConfigCommon<IntegratedDynamics> blockConfig) {
        super(blockConfig);
        blockConfig.getMod().getModEventBus().addListener((RegisterSpecialModelRendererEvent event) -> event.register(blockConfig.getResourceKey().location(), ItemStackBlockEntityEnergyBatteryRender.Unbaked.MAP_CODEC));
    }
}
