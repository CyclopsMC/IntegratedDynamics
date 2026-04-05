package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author rubensworks
 */
public record ItemModelFacade(FacadeModel facadeModel) implements ItemModel {

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int seed) {
        facadeModel.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, seed);
    }

    public static record Unbaked(Identifier modelEmpty) implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelFacade.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Identifier.CODEC.fieldOf("model_empty").forGetter(ItemModelFacade.Unbaked::modelEmpty)
                        )
                        .apply(instance, ItemModelFacade.Unbaked::new)
        );

        @Override
        public MapCodec<ItemModelFacade.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext, org.joml.Matrix4fc matrix) {
            return new ItemModelFacade(new FacadeModel(
                    new CuboidItemModelWrapper.Unbaked(modelEmpty, java.util.Optional.empty(), List.of(new Constant(-1))).bake(bakingContext, matrix),
                    new ModelRenderProperties(false, null, bakingContext.blockModelBaker().getModel(modelEmpty).getTopTransforms())
            ));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(modelEmpty);
        }
    }
}
