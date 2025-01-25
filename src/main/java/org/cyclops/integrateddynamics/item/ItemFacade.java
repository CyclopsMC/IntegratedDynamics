package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.client.model.IDynamicModelElementCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.block.IFacadeable;

/**
 * An item that represents a facade of a certain type.
 * @author rubensworks
 */
public class ItemFacade extends Item implements IDynamicModelElementCommon {

    public ItemFacade(Properties properties) {
        super(properties);
    }

    public BlockState getFacadeBlock(ItemStack itemStack) {
        return itemStack.get(RegistryEntries.DATACOMPONENT_FACADE_BLOCK);
    }

    public ItemStack getFacadeBlockItem(ItemStack itemStack) {
        BlockState blockState = getFacadeBlock(itemStack);
        if(blockState != null) {
            return IModHelpers.get().getBlockHelpers().getItemStackFromBlockState(blockState);
        }
        return null;
    }

    public void writeFacadeBlock(ItemStack itemStack, BlockState blockState) {
        itemStack.set(RegistryEntries.DATACOMPONENT_FACADE_BLOCK, blockState);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component suffix = Component.translatable("general.integrateddynamics.info.none")
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
            IFacadeable facadeable = IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(context.getLevel(), context.getClickedPos(), null, Capabilities.Facadeable.BLOCK).orElse(null);
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

}
