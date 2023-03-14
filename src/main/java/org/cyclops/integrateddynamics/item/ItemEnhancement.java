package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.List;

/**
 * An enhancement item.
 * @author rubensworks
 */
public class ItemEnhancement extends Item {

    private final Type type;

    public ItemEnhancement(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public <P extends IPartType<P, S>, S extends IPartState<P>> InteractionResult applyEnhancement(IPartType<P, S> partType, IPartState<P> partState, ItemStack itemStack, Player player, InteractionHand hand) {
        switch (this.type) {
            case OFFSET -> {
                if (partType.supportsOffsets()) {
                    int value = getEnhancementValue(itemStack);
                    int newValue = partState.getMaxOffset() + value;
                    if (newValue < GeneralConfig.maxPartOffset) {
                        if (!player.level.isClientSide()) {
                            partState.setMaxOffset(newValue);
                        }
                        itemStack.shrink(1);
                        player.sendSystemMessage(Component.translatable("item.integrateddynamics.enhancement_offset.increased", newValue));
                        return InteractionResult.SUCCESS;
                    }
                    player.sendSystemMessage(Component.translatable("item.integrateddynamics.enhancement_offset.limit", GeneralConfig.maxPartOffset));
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public int getEnhancementValue(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("value");
    }

    public void setEnhancementValue(ItemStack itemStack, int value) {
        itemStack.getOrCreateTag().putInt("value", value);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("item.integrateddynamics.enhancement_offset.tooltip", getEnhancementValue(itemStack)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(itemStack, world, list, flag);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowedIn(tab)) {
            ItemStack itemStack = new ItemStack(this);
            setEnhancementValue(itemStack, 4);
            items.add(itemStack);
        }
    }

    public static enum Type {
        OFFSET
    }

}
