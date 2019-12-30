package org.cyclops.integrateddynamics.block;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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
                eConfig -> new BlockFluidLiquidChorus(Block.Properties.create(Material.WATER)
                        .doesNotBlockMovement()
                        .hardnessAndResistance(100.0F)
                        .noDrops()),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }
    
}
