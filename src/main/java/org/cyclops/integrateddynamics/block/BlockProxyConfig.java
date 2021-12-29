package org.cyclops.integrateddynamics.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockProxy;

/**
 * Config for {@link BlockProxy}.
 * @author rubensworks
 */
public class BlockProxyConfig extends BlockConfig {

    public BlockProxyConfig() {
        super(
                IntegratedDynamics._instance,
                "proxy",
                eConfig -> new BlockProxy(Block.Properties.of(Material.HEAVY_METAL)
                        .strength(5.0F)
                        .sound(SoundType.METAL)),
                (eConfig, block) -> new ItemBlockProxy(block, new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
