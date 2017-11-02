package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeLists;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.List;

/**
 * Value type with values that are ingredients.
 * @author rubensworks
 */
public class ValueObjectTypeIngredients extends ValueObjectTypeBase<ValueObjectTypeIngredients.ValueIngredients> implements
        IValueTypeNamed<ValueObjectTypeIngredients.ValueIngredients>, IValueTypeNullable<ValueObjectTypeIngredients.ValueIngredients> {

    public ValueObjectTypeIngredients() {
        super("ingredients");
    }

    @Override
    public ValueIngredients getDefault() {
        return ValueIngredients.of(null);
    }

    @Override
    public String toCompactString(ValueIngredients value) {
        if (value.getRawValue().isPresent()) {
            StringBuilder sb = new StringBuilder();
            for (List<ValueObjectTypeItemStack.ValueItemStack> valueItemStacks : value.getRawValue().get().getItemStacksRaw()) {
                sb.append(ValueTypes.OBJECT_ITEMSTACK.toCompactString(
                        Iterables.getFirst(valueItemStacks, ValueTypes.OBJECT_ITEMSTACK.getDefault())));
                if (valueItemStacks.size() > 1) sb.append("+");
                sb.append(", ");
            }
            for (List<ValueObjectTypeFluidStack.ValueFluidStack> valueFluidStacks : value.getRawValue().get().getFluidStacksRaw()) {
                sb.append(ValueTypes.OBJECT_FLUIDSTACK.toCompactString(
                        Iterables.getFirst(valueFluidStacks, ValueTypes.OBJECT_FLUIDSTACK.getDefault())));
                if (valueFluidStacks.size() > 1) sb.append("+");
                sb.append(", ");
            }
            for (List<ValueTypeInteger.ValueInteger> valueEnergy : value.getRawValue().get().getEnergiesRaw()) {
                sb.append(ValueTypes.INTEGER.toCompactString(
                        Iterables.getFirst(valueEnergy, ValueTypes.INTEGER.getDefault())));
                sb.append(" " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT));
                if (valueEnergy.size() > 1) sb.append("+");
                sb.append(", ");
            }
            String str = sb.toString();
            return str.length() >= 2 ? str.substring(0, str.length() - 2) : "";
        }
        return "";
    }

    @Override
    public String serialize(ValueIngredients value) {
        if(!value.getRawValue().isPresent()) return "";

        NBTTagCompound tag = new NBTTagCompound();
        IIngredients ingredients = value.getRawValue().get();

        NBTTagList itemStacks = new NBTTagList();
        for (List<ValueObjectTypeItemStack.ValueItemStack> valueItemStacks : ingredients.getItemStacksRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueObjectTypeItemStack.ValueItemStack valueItemStack : valueItemStacks) {
                list.appendTag(new NBTTagString(ValueTypes.OBJECT_ITEMSTACK.serialize(valueItemStack)));
            }
            itemStacks.appendTag(list);
        }
        tag.setTag("items", itemStacks);

        NBTTagList fluidStacks = new NBTTagList();
        for (List<ValueObjectTypeFluidStack.ValueFluidStack> valueFluidStacks : ingredients.getFluidStacksRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack : valueFluidStacks) {
                list.appendTag(new NBTTagString(ValueTypes.OBJECT_FLUIDSTACK.serialize(valueFluidStack)));
            }
            fluidStacks.appendTag(list);
        }
        tag.setTag("fluids", fluidStacks);

        NBTTagList energies = new NBTTagList();
        for (List<ValueTypeInteger.ValueInteger> valueEnergies : ingredients.getEnergiesRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueTypeInteger.ValueInteger valueEnergy : valueEnergies) {
                list.appendTag(new NBTTagString(ValueTypes.INTEGER.serialize(valueEnergy)));
            }
            energies.appendTag(list);
        }
        tag.setTag("energies", energies);

        return tag.toString();
    }

    @Override
    public ValueIngredients deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueIngredients.of(null);

        try {
            List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks = Lists.newArrayList();
            List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks = Lists.newArrayList();
            List<List<ValueTypeInteger.ValueInteger>> energies = Lists.newArrayList();

            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);

            for (NBTBase subTag : tag.getTagList("items", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueObjectTypeItemStack.ValueItemStack> list = Lists.newArrayList();
                itemStacks.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.OBJECT_ITEMSTACK.deserialize(listTag.getStringTagAt(i)));
                }
            }

            for (NBTBase subTag : tag.getTagList("fluids", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueObjectTypeFluidStack.ValueFluidStack> list = Lists.newArrayList();
                fluidStacks.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.OBJECT_FLUIDSTACK.deserialize(listTag.getStringTagAt(i)));
                }
            }

            for (NBTBase subTag : tag.getTagList("energies", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueTypeInteger.ValueInteger> list = Lists.newArrayList();
                energies.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.INTEGER.deserialize(listTag.getStringTagAt(i)));
                }
            }

            return ValueIngredients.of(new IngredientsRecipeLists(
                    itemStacks,
                    fluidStacks,
                    energies
            ));
        } catch (NBTException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Something went wrong while deserializing '%s'.", value));
        }
    }

    @Override
    public String getName(ValueIngredients a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueIngredients a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeIngredientsLPElement();
    }

    @ToString
    public static class ValueIngredients extends ValueOptionalBase<IIngredients> {

        private ValueIngredients(IIngredients recipe) {
            super(ValueTypes.OBJECT_INGREDIENTS, recipe);
        }

        public static ValueIngredients of(IIngredients recipe) {
            return new ValueIngredients(recipe);
        }

        @Override
        protected boolean isEqual(IIngredients a, IIngredients b) {
            return a.equals(b);
        }
    }

}
