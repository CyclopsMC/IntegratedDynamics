package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.cyclops.commoncapabilities.api.capability.recipehandler.ItemHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializer;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ingredients where items can have different matching options.
 * @author rubensworks
 */
public class IngredientsRecipeItemMatch extends IngredientsRecipeLists {

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

    private final List<ItemMatchType> itemMatchers;

    public IngredientsRecipeItemMatch(Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists,
                                      List<ItemMatchType> itemMatchers) {
        super(lists);
        this.itemMatchers = itemMatchers;
    }

    public List<ItemMatchType> getItemMatchers() {
        return itemMatchers;
    }

    protected List<ItemStack> getOreDictEquivalent(ItemStack itemStack) {
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

    @Override
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index) {
        if (component == RecipeComponent.ITEMSTACK) {
            ItemMatchType matchType = getItemMatchers().get(index);
            // Special-casing is only needed for oredict
            if (matchType == ItemMatchType.OREDICT) {
                return (List<V>) super.<ValueObjectTypeItemStack.ValueItemStack, ItemStack, ItemHandlerRecipeTarget>
                        getList(RecipeComponent.ITEMSTACK, index).stream()
                        .map(ValueObjectTypeItemStack.ValueItemStack::getRawValue)
                        .map(this::getOreDictEquivalent)
                        .flatMap(List::stream)
                        .map(ValueObjectTypeItemStack.ValueItemStack::of)
                        .collect(Collectors.toList());
            }
        }
        return super.getList(component, index);
    }

    @Override
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index) {
        if (component == RecipeComponent.ITEMSTACK) {
            ItemMatchType matchType = getItemMatchers().get(index);
            Predicate<ValueObjectTypeItemStack.ValueItemStack> predicate;
            List<ValueObjectTypeItemStack.ValueItemStack> rawStacks = super.getList(component, index);
            Stream<ItemStack> stackStream = rawStacks.stream().map(ValueObjectTypeItemStack.ValueItemStack::getRawValue);
            switch (matchType) {
                case ITEM:
                    predicate = valueItemStack -> stackStream.anyMatch(
                            (stack) -> stack.getItem() == valueItemStack.getRawValue().getItem());
                    break;
                case ITEMMETA:
                    predicate = valueItemStack -> stackStream.anyMatch(
                            (stack) -> ItemStack.areItemsEqual(stack, valueItemStack.getRawValue()));
                    break;
                case ITEMNBT:
                    predicate = valueItemStack -> stackStream.anyMatch(
                            (stack) -> stack.getItem() == valueItemStack.getRawValue().getItem()
                                    && ItemStack.areItemStackTagsEqual(stack, valueItemStack.getRawValue()));
                    break;
                default:
                    predicate = super.getPredicate(component, index);
            }
            return (Predicate<V>) predicate;
        }
        return super.getPredicate(component, index);
    }

    public static enum ItemMatchType {

        ITEMMETA,
        ITEM,
        ITEMMETANBT,
        ITEMNBT,
        OREDICT;

        public ItemMatchType next() {
            ItemMatchType[] values = ItemMatchType.values();
            return this.ordinal() == values.length - 1 ? values[0] : values[this.ordinal() + 1];
        }

        public ResourceLocation getSlotSpriteName() {
            return new ResourceLocation(Reference.MOD_ID,
                    "slots/" + this.name().toLowerCase(Locale.ENGLISH));
        }
    }

    public static class Serializer implements IIngredientsSerializer<IngredientsRecipeItemMatch> {

        @Override
        public boolean canHandle(IIngredients ingredients) {
            return ingredients instanceof IngredientsRecipeItemMatch;
        }

        @Override
        public String getUniqueName() {
            return "itemmatch";
        }

        @Override
        public NBTTagCompound serialize(IngredientsRecipeItemMatch ingredients) {
            NBTTagCompound tag = IngredientsSerializerDefault.getInstance().serialize(ingredients);
            tag.setIntArray("matchers", ingredients.getItemMatchers().stream().mapToInt(ItemMatchType::ordinal).toArray());
            return tag;
        }

        @Override
        public IngredientsRecipeItemMatch deserialize(NBTTagCompound value) throws EvaluationException {
            IngredientsRecipeLists ingredientsRecipeLists = IngredientsSerializerDefault.getInstance().deserialize(value);
            List<ItemMatchType> matchers = Arrays.stream(value.getIntArray("matchers"))
                    .mapToObj(index -> ItemMatchType.values()[index]).collect(Collectors.toList());
            return new IngredientsRecipeItemMatch(ingredientsRecipeLists.getRaw(), matchers);
        }
    }

}
