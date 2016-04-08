package org.cyclops.integrateddynamics.client.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.client.model.DelegatingChildDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.List;

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

    public FacadeModel(IBakedModel baseModel, IBlockState blockState, EnumFacing facing, long rand) {
        super(baseModel, blockState, facing, rand);
    }

    public FacadeModel(IBakedModel baseModel, ItemStack itemStack, World world, EntityLivingBase entity) {
        super(baseModel, itemStack, world, entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BakedQuad> getGeneralQuads() {
        return baseModel.getQuads(this.blockState, this.facing, this.rand);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state, EnumFacing side, long rand) {
        return null;
    }

    @Override
    public IBakedModel handleItemState(ItemStack itemStack, World world, EntityLivingBase entity) {
        IBlockState blockState = ItemFacade.getInstance().getFacadeBlock(itemStack);
        if(blockState == null) {
            return new FacadeModel(emptyModel, itemStack, world, entity);
        }
        return new FacadeModel(RenderHelpers.getBakedModel(blockState), itemStack, world, entity);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return RenderHelpers.getBakedModel(Blocks.stone.getDefaultState()).getParticleTexture();
    }
}
