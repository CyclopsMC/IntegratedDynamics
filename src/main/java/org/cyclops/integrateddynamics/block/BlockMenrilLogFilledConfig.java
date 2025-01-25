package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilLogFilledConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "worldgeneration", comment = "The 1/x chance at which Menril Wood will be filled with Menril Resin when generated, the higher this value, the lower the chance.", isCommandable = true, minimalValue = 0)
    public static int filledMenrilLogChance = 10;

    public BlockMenrilLogFilledConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_log_filled",
                (eConfig, properties) -> new BlockMenrilLogFilled(properties
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
