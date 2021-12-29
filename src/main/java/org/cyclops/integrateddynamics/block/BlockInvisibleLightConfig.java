package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockInvisibleLight}.
 * @author rubensworks
 */
public class BlockInvisibleLightConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "If invisible light should act as full a block", configLocation = ModConfig.Type.SERVER)
    public static boolean invisibleLightBlock = true;

    public BlockInvisibleLightConfig() {
        super(
                IntegratedDynamics._instance,
                "invisible_light",
                eConfig -> new BlockInvisibleLight(Block.Properties.of(Material.AIR)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .lightLevel((blockState) -> 15)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
