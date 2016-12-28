package org.cyclops.integrateddynamics.block;

import net.minecraft.item.ItemBlock;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * Config for {@link BlockEnergyBattery}.
 * @author rubensworks
 */
public class BlockEnergyBatteryConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockEnergyBatteryConfig _instance;

    /**
     * The default capacity of an energy battery.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The default capacity of an energy battery.", minimalValue = 0)
    public static int capacity = 100000;

    /**
     * How much energy per tick it emits when activated.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "How much energy per tick it emits when activated.", isCommandable = true, minimalValue = 0)
    public static int energyPerTick = 2000;

    /**
     * Make a new instance.
     */
    public BlockEnergyBatteryConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "energyBattery",
            null,
            BlockEnergyBattery.class
        );
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockEnergyContainer.class;
    }
}
