package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Door.
 * @author josephcsible
 *
 */
public class BlockMenrilDoorConfig extends BlockConfigCommon<IntegratedDynamics> {

    public BlockMenrilDoorConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_door",
                (eConfig, properties) -> new DoorBlock(BlockSetType.OAK, properties
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
