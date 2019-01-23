package org.cyclops.integrateddynamics.core.ingredient;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Different methods for matching ItemStacks.
 * @author rubensworks
 */
public enum ItemMatchType {

    ITEMMETA(new FlaggedPrototypeHandler(ItemMatch.ITEM | ItemMatch.DAMAGE)),
    ITEM(new FlaggedPrototypeHandler(ItemMatch.ITEM)),
    ITEMMETANBT(new FlaggedPrototypeHandler(ItemMatch.ITEM | ItemMatch.DAMAGE | ItemMatch.NBT)),
    ITEMNBT(new FlaggedPrototypeHandler(ItemMatch.ITEM | ItemMatch.NBT)),
    OREDICT(itemStack -> {
        return getOreDictEquivalent(itemStack).stream()
                .map(ItemStackHelpers::getVariants)
                .flatMap(List::stream)
                .map(stack -> new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, stack, ItemMatch.ITEM | ItemMatch.DAMAGE | ItemMatch.NBT))
                .collect(Collectors.toList());
    });

    private static final LoadingCache<ItemStack, List<ItemStack>> CACHE_OREDICT = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<ItemStack, List<ItemStack>>() {
                @Override
                public List<ItemStack> load(ItemStack key) throws Exception {
                    return Arrays.stream(OreDictionary.getOreIDs(key))
                            .mapToObj(OreDictionary::getOreName)
                            .map(OreDictionary::getOres)
                            .flatMap(List::stream).collect(Collectors.toList());
                }
            });

    protected static List<ItemStack> getOreDictEquivalent(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return Collections.singletonList(itemStack);
        }
        try {
            List<ItemStack> itemStacks = CACHE_OREDICT.get(itemStack);
            if (itemStacks.isEmpty()) {
                return Collections.singletonList(itemStack);
            }
            return itemStacks;
        } catch (ExecutionException e) {
            return Collections.singletonList(itemStack);
        }
    }

    private final IPrototypeHandler prototypeHandler;

    ItemMatchType(IPrototypeHandler prototypeHandler) {
        this.prototypeHandler = prototypeHandler;
    }

    public ItemMatchType next() {
        ItemMatchType[] values = ItemMatchType.values();
        return this.ordinal() == values.length - 1 ? values[0] : values[this.ordinal() + 1];
    }

    public ResourceLocation getSlotSpriteName() {
        return new ResourceLocation(Reference.MOD_ID,
                "slots/" + this.name().toLowerCase(Locale.ENGLISH));
    }

    public IPrototypeHandler getPrototypeHandler() {
        return this.prototypeHandler;
    }

    public static interface IPrototypeHandler {
        /**
         * Create prototypes.
         * @param itemStack An ItemStack to derive prototypes from.
         * @return The list of prototypes.
         */
        public List<IPrototypedIngredient<ItemStack, Integer>> getPrototypesFor(ItemStack itemStack);
    }

    public static class FlaggedPrototypeHandler implements ItemMatchType.IPrototypeHandler {

        private final int flags;

        public FlaggedPrototypeHandler(int flags) {
            this.flags = flags;
        }

        @Override
        public List<IPrototypedIngredient<ItemStack, Integer>> getPrototypesFor(ItemStack itemStack) {
            return ItemStackHelpers.getVariants(itemStack)
                    .stream()
                    .map(stack -> new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, stack, flags))
                    .collect(Collectors.toList());
        }
    }
}
