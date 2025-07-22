package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.client.model.DynamicItemAndBlockModel;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author rubensworks
 */
public record ItemModelCable(DynamicItemAndBlockModel model) implements ItemModel {
    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        ModelRenderProperties modelRenderProperties = new ModelRenderProperties(false, model.particleIcon(), model.getTopTransforms());
        new BlockModelWrapper(List.of(), this.model.handleItemState(stack, level, entity), modelRenderProperties)
                .update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
    }

    public static record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelCable.Unbaked> MAP_CODEC = MapCodec.unit(new ItemModelCable.Unbaked());

        @Override
        public MapCodec<ItemModelCable.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext) {
            return new ItemModelCable(new CableModel());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            // Do nothing
        }
    }
}
