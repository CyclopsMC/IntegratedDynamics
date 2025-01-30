package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author rubensworks
 */
public record ItemModelVariableOverlay(BakedModelVariableOverlays variableModelBaked, ItemTransforms itemTransforms) implements ItemModel {

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        new BlockModelWrapper(this.variableModelBaked.getModelForItem(stack, level, entity, itemTransforms), List.of(new Constant(-1)))
            .update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
    }

    public static record Unbaked(ResourceLocation model) implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelVariableOverlay.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                ResourceLocation.CODEC.fieldOf("model").forGetter(ItemModelVariableOverlay.Unbaked::model)
                        )
                        .apply(instance, ItemModelVariableOverlay.Unbaked::new)
        );

        @Override
        public MapCodec<ItemModelVariableOverlay.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext) {
            // Source ItemTransforms from the base item model, similar to DynamicFluidContainerModel
            var baseItemModel = bakingContext.blockModelBaker().getModel(ResourceLocation.withDefaultNamespace("item/generated"));
            if (baseItemModel == null) {
                throw new IllegalStateException("Failed to access item/generated model");
            }

            return new ItemModelVariableOverlay((BakedModelVariableOverlays) bakingContext.bake(model), baseItemModel.getTransforms());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.resolve(this.model);
        }
    }
}
