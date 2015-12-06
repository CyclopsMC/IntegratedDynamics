package org.cyclops.integrateddynamics.client.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.client.model.DynamicModel;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.List;

/**
 * Dynamic model for facade items.
 * @author rubensworks
 */
public class FacadeModel extends DynamicModel {

    public static IBakedModel emptyModel;

    private final IBakedModel baseModel;

    public FacadeModel() {
        this.baseModel = null;
    }

    public FacadeModel(IBakedModel baseModel) {
        this.baseModel = baseModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing side) {
        return baseModel.getFaceQuads(side);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BakedQuad> getGeneralQuads() {
        return baseModel.getGeneralQuads();
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        return null;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        IBlockState blockState = ItemFacade.getInstance().getFacadeBlock(stack);
        if(blockState == null) {
            return new FacadeModel(emptyModel);
        }
        return new FacadeModel(RenderHelpers.getBakedModel(blockState));
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return RenderHelpers.getBakedModel(Blocks.stone.getDefaultState()).getParticleTexture();
    }
}
