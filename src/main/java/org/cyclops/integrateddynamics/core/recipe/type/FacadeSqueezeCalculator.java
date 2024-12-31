package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer.IngredientChance;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeSqueezeCalculator {

    public static NonNullList<IngredientChance> getOutputItems(ItemStack inputItem) {
        ItemStack facadeItemStack = new ItemStack(RegistryEntries.ITEM_FACADE);

        facadeItemStack.setTag(null);
        Either<ItemStack, ItemStackFromIngredient> facadeEither = Either.left(facadeItemStack);
        IngredientChance facade = new IngredientChance(facadeEither, 1.0F);

        CompoundTag nbt = inputItem.getOrCreateTag();
        ResourceLocation resourceLocation = new ResourceLocation(nbt.getCompound("block").getString("Name"));
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

        if (resourceLocation.toString().equals("minecraft:")) return NonNullList.of(facade, facade); // NBT is either malformed or non-existent

        Either<ItemStack, ItemStackFromIngredient> itemStack = Either.left(new ItemStack(item));
        IngredientChance combinedBlock = new IngredientChance(itemStack, 1.0F);

        return NonNullList.of(facade, facade, combinedBlock);
    }
}
