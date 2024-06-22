package org.cyclops.integrateddynamics.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * Config for {@link BlockCable}.
 * @author rubensworks
 */
public class BlockCableConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "If cable shapes should be determined dynamically. Disable this if FPS issues would occur.", minimalValue = 0)
    public static boolean dynamicShape = true;

    public BlockCableConfig() {
        super(
                IntegratedDynamics._instance,
                "cable",
                eConfig -> new BlockCable(Block.Properties.of()
                        .strength(BlockCable.BLOCK_HARDNESS)
                        .forceSolidOn()
                        .sound(SoundType.METAL)
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                (eConfig, block) -> new ItemBlockCable(block, new Item.Properties())
                );
        if (MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getModEventBus().addListener(this::onRegisterColors);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterColors(RegisterColorHandlersEvent.Block event) {
        event.register(new BlockCable.BlockColor(), getInstance());
    }

}
