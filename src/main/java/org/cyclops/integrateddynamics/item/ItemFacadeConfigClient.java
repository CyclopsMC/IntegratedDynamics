package org.cyclops.integrateddynamics.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.model.IDynamicModelElementCommon;
import org.cyclops.cyclopscore.config.extendedconfig.ItemClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class ItemFacadeConfigClient extends ItemClientConfig<IntegratedDynamics> {
    public ItemFacadeConfigClient(ItemConfigCommon<IntegratedDynamics> itemConfig) {
        super(itemConfig);
        IntegratedDynamics._instance.getModEventBus().addListener(this::onRegisterColors);
    }

    @Override
    public @Nullable IDynamicModelElementCommon getDynamicModelElement() {
        return new IDynamicModelElementCommon() {
            @Override
            public BakedModel createDynamicModel(Consumer<Pair<ModelResourceLocation, BakedModel>> modelConsumer, Function<ModelResourceLocation, BakedModel> modelRetriever) {
                // Don't throw away the original model, but use if for displaying an unbound facade item.
                ModelResourceLocation location = new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(getItemConfig().getInstance()), "inventory");
                FacadeModel.emptyModel = modelRetriever.apply(location);
                return new FacadeModel();
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterColors(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(getItemConfig().getResourceKey().location(), Color.MAP_CODEC);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Color implements ItemTintSource {
        public static final MapCodec<Color> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.point(new Color()));

        @Override
        public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            BlockState blockstate = ((ItemFacade) itemStack.getItem()).getFacadeBlock(itemStack);
            return Minecraft.getInstance().getBlockColors().getColor(blockstate, null, null);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }
}
