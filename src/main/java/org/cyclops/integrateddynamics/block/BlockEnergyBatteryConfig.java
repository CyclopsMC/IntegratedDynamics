package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Lists;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Config for {@link BlockEnergyBattery}.
 * @author rubensworks
 */
public class BlockEnergyBatteryConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The default capacity of an energy battery.", minimalValue = 0)
    public static int capacity = 1000000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The 1/X fraction of the battery capacity that is allowed to be transfered per tick.", isCommandable = true, minimalValue = 0)
    public static int energyRateCapacityFraction = 2000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The minimum energy transfer rate per tick.", isCommandable = true, minimalValue = 0)
    public static int minEnergyRate = 2000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum capacity shown in creative tabs. (Make sure that you do not cross the max int size)")
    public static int maxCreativeTabCapacity = 655360000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum capacity visible in the creative tabs. (Make sure that you do not cross the max int size)")
    public static int maxCreativeCapacity = 40960000;

    public BlockEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                (eConfig, properties) -> new BlockEnergyBattery(properties
                        .sound(SoundType.METAL)
                        .strength(2.0F, 5.0F)),
                (eConfig, block) -> new ItemBlockEnergyContainerAutoSupply(block, eConfig.createDefaultItemProperties())
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
        IntegratedDynamics._instance.getModEventBus().addListener(this::fillCreativeTab);
    }
    protected void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM, (stack, context) -> ((ItemBlockEnergyContainer) getItemInstance()).createCapability(stack, context), getInstance());
    }

    protected void fillCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == IntegratedDynamics._instance.getDefaultCreativeTab()) {
            for (ItemStack itemStack : dynamicCreativeTabEntries()) {
                event.accept(itemStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    @Override
    public Collection<java.util.function.Supplier<ItemStack>> getDefaultCreativeTabEntries() {
        // Register items dynamically into tab, because when this is called, capabilities are not initialized yet.
        return Collections.emptyList();
    }

    protected Collection<ItemStack> dynamicCreativeTabEntries() {
        List<ItemStack> itemStacks = Lists.newArrayList();

        ItemStack itemStack = new ItemStack(getInstance());

        int capacityOriginal = BlockEnergyBatteryConfig.capacity;
        int capacity = capacityOriginal;
        int lastCapacity;
        do {
            ItemStack currentStack = itemStack.copy();
            IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) ((ItemBlockEnergyContainer) currentStack.getItem()).getEnergyBattery(currentStack).orElse(null);
            energyStorage.setCapacity(capacity);
            itemStacks.add(currentStack.copy());
            ((BlockEnergyBattery) getInstance()).fill(energyStorage);
            itemStacks.add(currentStack.copy());
            lastCapacity = capacity;
            capacity = capacity << 2;
        } while (capacity < Math.min(BlockEnergyBatteryConfig.maxCreativeCapacity, BlockEnergyBatteryConfig.maxCreativeTabCapacity) && capacity > lastCapacity);

        return itemStacks;
    }

    @Override
    public @Nullable BlockClientConfig<IntegratedDynamics> constructBlockClientConfig() {
        return new BlockEnergyBatteryConfigClient(this);
    }
}
