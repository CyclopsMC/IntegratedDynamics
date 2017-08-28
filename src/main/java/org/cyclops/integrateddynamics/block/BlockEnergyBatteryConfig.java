package org.cyclops.integrateddynamics.block;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

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
     * The maximum capacity possible by combining batteries.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum capacity possible by combining batteries. (Make sure that you do not cross the max int size.)")
    public static int maxCapacity = 65536000;
    /**
     * The maximum capacity visible in the creative tabs.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum capacity visible in the creative tabs. (Make sure that you do not cross the max int size.)")
    public static int maxCreativeCapacity = 4096000;

    /**
     * Make a new instance.
     */
    public BlockEnergyBatteryConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "energy_battery",
            null,
            BlockEnergyBattery.class
        );
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockEnergyContainer.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(TileEnergyBattery.class, new RenderTileEntityEnergyBattery());
    }
}
