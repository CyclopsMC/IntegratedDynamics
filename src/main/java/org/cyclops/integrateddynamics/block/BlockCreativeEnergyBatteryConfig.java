package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityEnergyBatteryRender;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Config for {@link BlockCreativeEnergyBattery}.
 * @author rubensworks
 */
public class BlockCreativeEnergyBatteryConfig extends BlockConfig {

    public BlockCreativeEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery_creative",
                eConfig -> new BlockCreativeEnergyBattery(Block.Properties.of()
                        .sound(SoundType.METAL)
                        .strength(2.0F, 5.0F)),
                (eConfig, block) -> new ItemBlockEnergyContainerAutoSupply(block,
                        new Item.Properties()) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                        consumer.accept(new IClientItemExtensions() {
                            @Override
                            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                                return new ItemStackBlockEntityEnergyBatteryRender();
                            }
                        });
                    }
                }
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
        IntegratedDynamics._instance.getModEventBus().addListener(this::fillCreativeTab);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> ((ItemBlockEnergyContainer) getItemInstance()).createCapability(stack), getInstance());
    }

    protected void fillCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == IntegratedDynamics._instance.getDefaultCreativeTab() || event.getTabKey().equals(CreativeModeTabs.SEARCH)) {
            for (ItemStack itemStack : dynamicCreativeTabEntries()) {
                event.accept(itemStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    @Override
    protected Collection<ItemStack> defaultCreativeTabEntries() {
        // Register items dynamically into tab, because when this is called, capabilities are not initialized yet.
        return Collections.emptyList();
    }

    protected Collection<ItemStack> dynamicCreativeTabEntries() {
        List<ItemStack> itemStacks = Lists.newArrayList();
        ItemStack full = new ItemStack(getInstance());
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) ((ItemBlockEnergyContainer) full.getItem()).getEnergyBattery(full).orElse(null);
        ((BlockCreativeEnergyBattery) getInstance()).fill(energyStorage);
        itemStacks.add(full);
        return itemStacks;
    }
}
