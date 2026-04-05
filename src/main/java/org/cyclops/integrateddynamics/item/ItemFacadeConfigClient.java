package org.cyclops.integrateddynamics.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.model.ItemModelFacade;
import org.jetbrains.annotations.Nullable;

/**
 * @author rubensworks
 */
public class ItemFacadeConfigClient extends ItemClientConfig<IntegratedDynamics> {
    public ItemFacadeConfigClient(ItemConfigCommon<IntegratedDynamics> itemConfig) {
        super(itemConfig);
        IntegratedDynamics._instance.getModEventBus().addListener(this::onRegisterColors);
        itemConfig.getMod().getModEventBus().addListener((RegisterItemModelsEvent event) -> event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "facade"), ItemModelFacade.Unbaked.MAP_CODEC));
    }

    public void onRegisterColors(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(getItemConfig().getResourceKey().identifier(), Color.MAP_CODEC);
    }

    public static class Color implements ItemTintSource {
        public static final MapCodec<Color> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.point(new Color()));

        @Override
        public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            BlockState blockstate = ((ItemFacade) itemStack.getItem()).getFacadeBlock(itemStack);
            if (blockstate == null) {
                return -1;
            }
            net.minecraft.client.color.block.BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(blockstate, 0);
            if (tintSource != null) {
                return tintSource.color(blockstate);
            }
            return -1;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }
}
