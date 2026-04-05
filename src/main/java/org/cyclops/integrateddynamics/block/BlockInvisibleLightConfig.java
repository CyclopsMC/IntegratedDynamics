package org.cyclops.integrateddynamics.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for {@link BlockInvisibleLight}.
 * @author rubensworks
 */
public class BlockInvisibleLightConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "If invisible light should act as full a block", configLocation = ModConfigLocation.SERVER)
    public static boolean invisibleLightBlock = true;

    public BlockInvisibleLightConfig() {
        super(
                IntegratedDynamics._instance,
                "invisible_light",
                (eConfig, properties) -> new BlockInvisibleLight(properties
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .lightLevel((blockState) -> 15)
                        .pushReaction(PushReaction.DESTROY)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public Collection<java.util.function.Supplier<ItemStack>> getDefaultCreativeTabEntries() {
        return Collections.emptyList();
    }
}
