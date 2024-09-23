package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityEnergyBattery;

/**
 * Config for the {@link BlockEntityEnergyBattery}.
 * @author rubensworks
 *
 */
public class BlockEntityEnergyBatteryConfig extends BlockEntityConfig<BlockEntityEnergyBattery> {

    public BlockEntityEnergyBatteryConfig() {
        super(
                IntegratedDynamics._instance,
                "energy_battery",
                (eConfig) -> new BlockEntityType<>(BlockEntityEnergyBattery::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_ENERGY_BATTERY.get(), RegistryEntries.BLOCK_ENERGY_BATTERY_CREATIVE.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(new BlockEntityEnergyBattery.CapabilityRegistrar(this::getInstance)::register);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityEnergyBattery::new);
    }

}
