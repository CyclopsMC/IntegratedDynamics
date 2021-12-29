package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import org.cyclops.cyclopscore.client.model.IDynamicModelElement;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;

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
            CompoundTag tag = itemStack.getTag();
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
        CompoundTag tag = itemStack.getOrCreateTag();
        CompoundTag serializedBlockState = BlockHelpers.serializeBlockState(blockState);
        tag.put("block", serializedBlockState);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component suffix = new TranslatableComponent("general.integrateddynamics.info.none")
                .withStyle(ChatFormatting.ITALIC);
        ItemStack itemStackInner = getFacadeBlockItem(itemStack);
        if(itemStackInner != null) {
            suffix = getFacadeBlockItem(itemStack).getHoverName();
        }
        return ((MutableComponent) super.getName(itemStack))
                .append(" - ")
                .append(suffix);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        if(!context.getLevel().isClientSide()) {
            IFacadeable facadeable = BlockEntityHelpers.getCapability(context.getLevel(), context.getClickedPos(), null, FacadeableConfig.CAPABILITY).orElse(null);
            BlockState blockState = getFacadeBlock(itemStack);
            if(facadeable != null && blockState != null) {
                // Add facade to existing cable
                if (!facadeable.hasFacade()) {
                    facadeable.setFacade(blockState);
                    ItemBlockCable.playPlaceSound(context.getLevel(), context.getClickedPos());
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BakedModel createDynamicModel(ModelBakeEvent event) {
        // Don't throw away the original model, but use if for displaying an unbound facade item.
        ModelResourceLocation location = new ModelResourceLocation(getRegistryName(), "inventory");
        FacadeModel.emptyModel = event.getModelRegistry().get(location);
        return new FacadeModel();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Color implements ItemColor {
        @Override
        public int getColor(ItemStack itemStack, int color) {
            BlockState blockstate = ((ItemFacade) itemStack.getItem()).getFacadeBlock(itemStack);
            return Minecraft.getInstance().getBlockColors().getColor(blockstate, null, null, color);
        }
    }

}
