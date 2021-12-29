package org.cyclops.integrateddynamics.block;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityEnergyBatteryRender;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;

import java.util.function.Consumer;

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

    @ConfigurableProperty(category = "machine", comment = "The maximum capacity shown in creative tabs. (Make sure that you do not cross the max int size)")
    public static int maxCreativeTabCapacity = 655360000;

    @ConfigurableProperty(category = "machine", comment = "The maximum capacity visible in the creative tabs. (Make sure that you do not cross the max int size)")
    public static int maxCreativeCapacity = 40960000;

    public BlockEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                eConfig -> new BlockEnergyBattery(Block.Properties.of(Material.HEAVY_METAL)
                        .sound(SoundType.METAL)
                        .strength(5.0F)),
                (eConfig, block) -> new ItemBlockEnergyContainerAutoSupply(block,
                        new Item.Properties().tab(IntegratedDynamics._instance.getDefaultItemGroup())) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
                        consumer.accept(new IItemRenderProperties() {
                            @Override
                            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                                return new ItemStackBlockEntityEnergyBatteryRender();
                            }
                        });
                    }
                }
        );
    }

}
