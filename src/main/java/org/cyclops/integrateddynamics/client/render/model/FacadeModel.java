package org.cyclops.integrateddynamics.client.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.cyclopscore.client.model.DelegatingChildDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Dynamic model for facade items.
 * @author rubensworks
 */
public class FacadeModel extends DelegatingChildDynamicItemAndBlockModel {

    public static IBakedModel emptyModel;

    public FacadeModel() {
       super(null);
    }

    public FacadeModel(IBakedModel baseModel) {
        super(baseModel);
    }

    public FacadeModel(IBakedModel baseModel, BlockState blockState, Direction facing, Random rand, IModelData modelData) {
        super(baseModel, blockState, facing, rand, modelData);
    }

    public FacadeModel(IBakedModel baseModel, ItemStack itemStack, World world, LivingEntity entity) {
        super(baseModel, itemStack, world, entity);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        try {
            return baseModel.getQuads(this.blockState, getRenderingSide(), this.rand);
        } catch (Exception e) {
            return emptyModel.getQuads(this.blockState, getRenderingSide(), this.rand);
        }
    }

    @Override
    public IBakedModel handleBlockState(@Nullable BlockState blockState, @Nullable Direction direction,
                                        @Nonnull Random random, @Nonnull IModelData iModelData) {
        return null;
    }

    @Override
    public IBakedModel handleItemState(ItemStack itemStack, World world, LivingEntity entity) {
        BlockState blockState = RegistryEntries.ITEM_FACADE.getFacadeBlock(itemStack);
        if(blockState == null) {
            return new FacadeModel(emptyModel, itemStack, world, entity);
        }
        IBakedModel bakedModel = RenderHelpers.getBakedModel(blockState);
        bakedModel = bakedModel.getOverrides().getOverrideModel(bakedModel,
                RegistryEntries.ITEM_FACADE.getFacadeBlockItem(itemStack), (ClientWorld) world, entity);
        return new FacadeModel(bakedModel, itemStack, world, entity);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return RenderHelpers.getBakedModel(Blocks.STONE.getDefaultState()).getParticleTexture();
    }

    @Override
    public boolean isSideLit() {
        return false; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ModelHelpers.DEFAULT_CAMERA_TRANSFORMS;
    }
}
