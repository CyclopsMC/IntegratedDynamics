package org.cyclops.integrateddynamics.block;


import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorusConfig extends BlockConfig {

    public BlockFluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "block_liquid_chorus",
                eConfig -> new BlockFluidLiquidChorus(Block.Properties.of(Material.WATER)
                        .noCollission()
                        .strength(100.0F)
                        .noDrops()),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }

}
