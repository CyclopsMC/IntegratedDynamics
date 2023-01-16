package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityEnergyBatteryRender;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainerAutoSupply;

import java.util.Collection;
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
                eConfig -> new BlockCreativeEnergyBattery(Block.Properties.of(Material.HEAVY_METAL)
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
    }

    @Override
    protected Collection<ItemStack> defaultCreativeTabEntries() {
        List<ItemStack> itemStacks = Lists.newArrayList();
        ItemStack full = new ItemStack(getInstance());
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) ((ItemBlockEnergyContainer) full.getItem()).getEnergyBattery(full).orElse(null);
        ((BlockCreativeEnergyBattery) getInstance()).fill(energyStorage);
        itemStacks.add(full);
        return itemStacks;
    }
}
