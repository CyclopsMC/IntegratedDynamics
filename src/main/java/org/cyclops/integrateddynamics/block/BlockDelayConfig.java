package org.cyclops.integrateddynamics.block;

import net.minecraft.item.ItemBlock;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockDelay;

/**
 * Config for {@link BlockDelay}.
 * @author rubensworks
 */
public class BlockDelayConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockDelayConfig _instance;

    /**
     * The maximum value history length that can be maintained.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum value history length that can be maintained..", minimalValue = 1)
    public static int maxHistoryCapacity = 1024;

    /**
     * Make a new instance.
     */
    public BlockDelayConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "delay",
            null,
            BlockDelay.class
        );
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockDelay.class;
    }
}
