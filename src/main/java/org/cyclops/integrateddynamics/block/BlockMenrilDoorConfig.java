package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockDoor;
import org.cyclops.cyclopscore.config.extendedconfig.BlockDoorConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * Config for the Menril Door.
 * @author josephcsible
 *
 */
public class BlockMenrilDoorConfig extends BlockDoorConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilDoorConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilDoorConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menril_door",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockDoor initSubInstance() {
        return (ConfigurableBlockDoor) new ConfigurableBlockDoor(this, Material.WOOD).setSoundType(SoundType.WOOD).setHardness(3.0F);
    }
}
