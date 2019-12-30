package org.cyclops.integrateddynamics.block;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockFluidMenrilResin}.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResinConfig extends BlockConfig {

    public BlockFluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "block_menril_resin",
                eConfig -> new BlockFluidMenrilResin(Block.Properties.create(Material.WATER)
                        .doesNotBlockMovement()
                        .hardnessAndResistance(100.0F)
                        .noDrops()),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }
    
}
