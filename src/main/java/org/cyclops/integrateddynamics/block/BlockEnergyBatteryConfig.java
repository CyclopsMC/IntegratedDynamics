package org.cyclops.integrateddynamics.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.tileentity.RenderTileEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;
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
    public static int capacity = 1000000;

    /**
     * The 1/X fraction of the battery capacity that is allowed to be transfered per tick.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The 1/X fraction of the battery capacity that is allowed to be transfered per tick.", isCommandable = true, minimalValue = 0)
    public static int energyRateCapacityFraction = 2000;

    /**
     * The minimum energy transfer rate per tick.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The minimum energy transfer rate per tick.", isCommandable = true, minimalValue = 0)
    public static int minEnergyRate = 2000;

    /**
     * The maximum capacity possible by combining batteries.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum capacity possible by combining batteries. (Make sure that you do not cross the max int size.)")
    public static int maxCapacity = 655360000;
    /**
     * The maximum capacity visible in the creative tabs.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum capacity visible in the creative tabs. (Make sure that you do not cross the max int size.)")
    public static int maxCreativeCapacity = 40960000;

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
        return ItemBlockEnergyContainerAutoSupply.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(TileEnergyBattery.class, new RenderTileEntityEnergyBattery());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onInit(Step step) {
        super.onInit(step);
        if(step == Step.INIT) {
            // Handle additional type of dark tank item rendering
            for (int meta = 0; meta < 2; meta++) {
                Item item = Item.getItemFromBlock(getBlockInstance());
                String modId = getMod().getModId();
                String itemName = getModelName(new ItemStack(item, 1, meta));
                ModelResourceLocation modelResourceLocation = new ModelResourceLocation(modId + ":" + itemName, "inventory");
                Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(
                        item, meta, modelResourceLocation);
            }
        }
    }
}
