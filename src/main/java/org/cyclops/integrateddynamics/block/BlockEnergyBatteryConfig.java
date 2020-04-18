package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;

/**
 * Config for {@link BlockEnergyBattery}.
 * @author rubensworks
 */
public class BlockEnergyBatteryConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "The default capacity of an energy battery.", minimalValue = 0)
    public static int capacity = 1000000;

    @ConfigurableProperty(category = "machine", comment = "The 1/X fraction of the battery capacity that is allowed to be transfered per tick.", isCommandable = true, minimalValue = 0)
    public static int energyRateCapacityFraction = 2000;

    @ConfigurableProperty(category = "machine", comment = "The minimum energy transfer rate per tick.", isCommandable = true, minimalValue = 0)
    public static int minEnergyRate = 2000;

    @ConfigurableProperty(category = "machine", comment = "The maximum capacity possible by combining batteries. (Make sure that you do not cross the max int size.)")
    public static int maxCapacity = 655360000;

    @ConfigurableProperty(category = "machine", comment = "The maximum capacity visible in the creative tabs. (Make sure that you do not cross the max int size.)")
    public static int maxCreativeCapacity = 40960000;

    public BlockEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                eConfig -> new BlockEnergyBattery(Block.Properties.create(Material.ANVIL)
                        .sound(SoundType.METAL)
                        .hardnessAndResistance(5.0F)),
                (eConfig, block) -> new ItemBlockEnergyContainerAutoSupply(block,
                        (new Item.Properties()).group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
