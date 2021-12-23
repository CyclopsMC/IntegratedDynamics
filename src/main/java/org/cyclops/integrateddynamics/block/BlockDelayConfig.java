package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockDelay;

/**
 * Config for {@link BlockDelay}.
 * @author rubensworks
 */
public class BlockDelayConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "The maximum value history length that can be maintained..", minimalValue = 1)
    public static int maxHistoryCapacity = 1024;

    public BlockDelayConfig() {
        super(
            IntegratedDynamics._instance,
            "delay",
            eConfig -> new BlockDelay(Block.Properties.of(Material.HEAVY_METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)),
                (eConfig, block) -> new ItemBlockDelay(block, new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
