package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilLogFilledConfig extends BlockConfig {

    @ConfigurableProperty(category = "worldgeneration", comment = "The 1/x chance at which Menril Wood will be filled with Menril Resin when generated, the higher this value, the lower the chance.", isCommandable = true, minimalValue = 0)
    public static int filledMenrilLogChance = 10;

    public BlockMenrilLogFilledConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_log_filled",
                eConfig -> new BlockMenrilLogFilled(Block.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
