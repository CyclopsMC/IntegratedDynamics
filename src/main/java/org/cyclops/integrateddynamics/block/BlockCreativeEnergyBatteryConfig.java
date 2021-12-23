package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.ItemStackTileEntityEnergyBatteryRender;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;

/**
 * Config for {@link BlockCreativeEnergyBattery}.
 * @author rubensworks
 */
public class BlockCreativeEnergyBatteryConfig extends BlockConfig {

    public BlockCreativeEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery_creative",
                eConfig -> new BlockCreativeEnergyBattery(Block.Properties.of(Material.HEAVY_METAL)
                        .sound(SoundType.METAL)
                        .strength(5.0F)),
                (eConfig, block) -> new ItemBlockEnergyContainerAutoSupply(block,
                        new Item.Properties()
                                .tab(IntegratedDynamics._instance.getDefaultItemGroup())
                                .setISTER(() -> ItemStackTileEntityEnergyBatteryRender::new))
        );
    }

}
