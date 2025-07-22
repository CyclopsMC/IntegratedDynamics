package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author rubensworks
 */
public record ItemModelFacade(FacadeModel facadeModel) implements ItemModel {
    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        facadeModel.update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
    }

    public static record Unbaked(ResourceLocation modelEmpty) implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelFacade.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                ResourceLocation.CODEC.fieldOf("model_empty").forGetter(ItemModelFacade.Unbaked::modelEmpty)
                        )
                        .apply(instance, ItemModelFacade.Unbaked::new)
        );

        @Override
        public MapCodec<ItemModelFacade.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext) {
            return new ItemModelFacade(new FacadeModel(
                    new BlockModelWrapper.Unbaked(modelEmpty, List.of(new Constant(-1))).bake(bakingContext),
                    new ModelRenderProperties(false, null, bakingContext.blockModelBaker().getModel(modelEmpty).getTopTransforms())
            ));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(modelEmpty);
        }
    }
}
