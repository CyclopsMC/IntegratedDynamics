package org.cyclops.integrateddynamics.client.render.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.client.model.DelegatingChildDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Dynamic model for facade items.
 * @author rubensworks
 */
public class FacadeModel extends DelegatingChildDynamicItemAndBlockModel {

    public static BakedModel emptyModel;

    public FacadeModel() {
       super(null);
    }

    public FacadeModel(BakedModel baseModel) {
        super(baseModel);
    }

    public FacadeModel(BakedModel baseModel, BlockState blockState, Direction facing, RandomSource rand, ModelData modelData, RenderType renderType) {
        super(baseModel, blockState, facing, rand, modelData, renderType);
    }

    public FacadeModel(BakedModel baseModel, ItemStack itemStack, Level world, LivingEntity entity) {
        super(baseModel, itemStack, world, entity);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        this.rand.setSeed(0); // To avoid weighted blockstates to change each tick
        try {
            return baseModel.getQuads(this.blockState, getRenderingSide(), this.rand);
        } catch (Exception e) {
            return emptyModel.getQuads(this.blockState, getRenderingSide(), this.rand);
        }
    }

    @Override
    public BakedModel handleBlockState(@Nullable BlockState blockState, @Nullable Direction direction,
                                        @Nonnull RandomSource random, @Nonnull ModelData iModelData,
                                       @Nullable RenderType renderType) {
        return null;
    }

    @Override
    public BakedModel handleItemState(ItemStack itemStack, Level world, LivingEntity entity) {
        BlockState blockState = RegistryEntries.ITEM_FACADE.getFacadeBlock(itemStack);
        if(blockState == null) {
            return new FacadeModel(emptyModel, itemStack, world, entity);
        }
        BakedModel bakedModel = RenderHelpers.getBakedModel(blockState);
        bakedModel = bakedModel.getOverrides().resolve(bakedModel,
                RegistryEntries.ITEM_FACADE.getFacadeBlockItem(itemStack), (ClientLevel) world, entity, 0);
        return new FacadeModel(bakedModel, itemStack, world, entity);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return RenderHelpers.getBakedModel(Blocks.STONE.defaultBlockState()).getParticleIcon();
    }

    @Override
    public boolean usesBlockLight() {
        return true; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelHelpers.DEFAULT_CAMERA_TRANSFORMS;
    }
}
