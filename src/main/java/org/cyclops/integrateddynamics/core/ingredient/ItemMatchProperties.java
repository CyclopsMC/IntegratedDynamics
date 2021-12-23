package org.cyclops.integrateddynamics.core.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesItemStackTag;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesList;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.network.PacketCodec;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * @author rubensworks
 */
public class ItemMatchProperties {

    static {
        PacketCodec.addCodedAction(ItemMatchProperties.class, new PacketCodec.ICodecAction() {
            @Override
            public void encode(Object o, PacketBuffer packetBuffer) {
                ItemMatchProperties props = ((ItemMatchProperties) o);
                PacketCodec.getAction(ItemStack.class).encode(props.itemStack, packetBuffer);
                packetBuffer.writeBoolean(props.nbt);
                packetBuffer.writeUtf(props.itemTag != null ? props.itemTag : "");
                packetBuffer.writeInt(props.tagQuantity);
            }

            @Override
            public Object decode(PacketBuffer packetBuffer) {
                ItemStack itemStack = (ItemStack) PacketCodec.getAction(ItemStack.class).decode(packetBuffer);
                boolean nbt = packetBuffer.readBoolean();
                String itemTag = packetBuffer.readUtf(32767);
                int tagQuantity = packetBuffer.readInt();
                return new ItemMatchProperties(itemStack, nbt, itemTag.isEmpty() ? null : itemTag, tagQuantity);
            }
        });
    }

    private final ItemStack itemStack;
    private boolean nbt;
    @Nullable
    private String itemTag;
    private int tagQuantity;

    public ItemMatchProperties(ItemStack itemStack) {
        this(itemStack, false, null, 1);
    }

    public ItemMatchProperties(ItemStack itemStack, boolean nbt, @Nullable String itemTag, int tagQuantity) {
        this.itemStack = itemStack;
        this.nbt = nbt;
        this.itemTag = itemTag;
        this.tagQuantity = tagQuantity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isNbt() {
        return nbt;
    }

    public void setNbt(boolean nbt) {
        this.nbt = nbt;
    }

    @Nullable
    public String getItemTag() {
        return itemTag;
    }

    public void setItemTag(@Nullable String itemTag) {
        this.itemTag = itemTag;
    }

    public int getTagQuantity() {
        return tagQuantity;
    }

    public void setTagQuantity(int tagQuantity) {
        this.tagQuantity = tagQuantity;
    }

    public boolean isValid() {
        return getItemTag() != null || !getItemStack().isEmpty();
    }

    public IPrototypedIngredientAlternatives<ItemStack, Integer> createPrototypedIngredient() {
        if (getItemTag() == null) {
            int flags = isNbt() ? ItemMatch.ITEM | ItemMatch.NBT : ItemMatch.ITEM;
            return new PrototypedIngredientAlternativesList<>(
                    Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, itemStack, flags)));
        } else {
            return new PrototypedIngredientAlternativesItemStackTag(Collections.singletonList(getItemTag()),
                    ItemMatch.ITEM | ItemMatch.NBT, getTagQuantity());
        }
    }
}
