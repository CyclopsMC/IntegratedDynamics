package org.cyclops.integrateddynamics.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import org.cyclops.cyclopscore.client.model.IDynamicModelElement;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;

import net.minecraft.item.Item.Properties;

/**
 * An item that represents a facade of a certain type.
 * @author rubensworks
 */
public class ItemFacade extends Item implements IDynamicModelElement {

    public ItemFacade(Properties properties) {
        super(properties);
    }

    public BlockState getFacadeBlock(ItemStack itemStack) {
        if(!itemStack.isEmpty() && itemStack.hasTag()) {
            CompoundNBT tag = itemStack.getTag();
            return BlockHelpers.deserializeBlockState(tag.getCompound("block"));
        }
        return null;
    }

    public ItemStack getFacadeBlockItem(ItemStack itemStack) {
        BlockState blockState = getFacadeBlock(itemStack);
        if(blockState != null) {
            return BlockHelpers.getItemStackFromBlockState(blockState);
        }
        return null;
    }

    public void writeFacadeBlock(ItemStack itemStack, BlockState blockState) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        CompoundNBT serializedBlockState = BlockHelpers.serializeBlockState(blockState);
        tag.put("block", serializedBlockState);
    }

    @Override
    public ITextComponent getName(ItemStack itemStack) {
        ITextComponent suffix = new TranslationTextComponent("general.integrateddynamics.info.none")
                .withStyle(TextFormatting.ITALIC);
        ItemStack itemStackInner = getFacadeBlockItem(itemStack);
        if(itemStackInner != null) {
            suffix = getFacadeBlockItem(itemStack).getHoverName();
        }
        return ((IFormattableTextComponent) super.getName(itemStack))
                .append(" - ")
                .append(suffix);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack itemStack = context.getItemInHand();
        if(!context.getLevel().isClientSide()) {
            IFacadeable facadeable = TileHelpers.getCapability(context.getLevel(), context.getClickedPos(), null, FacadeableConfig.CAPABILITY).orElse(null);
            BlockState blockState = getFacadeBlock(itemStack);
            if(facadeable != null && blockState != null) {
                // Add facade to existing cable
                if (!facadeable.hasFacade()) {
                    facadeable.setFacade(blockState);
                    ItemBlockCable.playPlaceSound(context.getLevel(), context.getClickedPos());
                    itemStack.shrink(1);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IBakedModel createDynamicModel(ModelBakeEvent event) {
        // Don't throw away the original model, but use if for displaying an unbound facade item.
        ModelResourceLocation location = new ModelResourceLocation(getRegistryName(), "inventory");
        FacadeModel.emptyModel = event.getModelRegistry().get(location);
        return new FacadeModel();
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemColor implements IItemColor {
        @Override
        public int getColor(ItemStack itemStack, int color) {
            BlockState blockstate = ((ItemFacade) itemStack.getItem()).getFacadeBlock(itemStack);
            return Minecraft.getInstance().getBlockColors().getColor(blockstate, null, null, color);
        }
    }

}
