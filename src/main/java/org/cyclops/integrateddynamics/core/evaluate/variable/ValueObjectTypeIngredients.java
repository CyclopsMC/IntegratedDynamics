package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeLists;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.List;
import java.util.Map;

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

            IIngredients ingredients = value.getRawValue().get();
            for (RecipeComponent<?, ?> component : ingredients.getComponents()) {
                IRecipeComponentHandler handler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
                for (List<IValue> values : ingredients.getRaw(component)) {
                    sb.append(handler.toCompactString(values));
                    sb.append(", ");
                }
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
        for (RecipeComponent<?, ?> component : ingredients.getComponents()) {
            IRecipeComponentHandler handler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
            if (handler != null) {
                NBTTagList tagList = new NBTTagList();
                for (List<IValue> values : ingredients.getRaw(component)) {
                    NBTTagList list = new NBTTagList();
                    for (IValue val : values) {
                        list.appendTag(new NBTTagString(handler.getValueType().serialize(val)));
                    }
                    tagList.appendTag(list);
                }
                tag.setTag("list" + handler.getComponent().getName(), tagList);
            }
        }

        return tag.toString();
    }

    @Override
    public ValueIngredients deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueIngredients.of(null);

        try {
            Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists = Maps.newIdentityHashMap();
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);

            for (RecipeComponent<?, ?> component : RecipeComponentHandlers.REGISTRY.getComponents()) {
                IRecipeComponentHandler handler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
                if (handler != null) {
                    List<List<? extends IValue>> list = Lists.newArrayList();
                    lists.put(component, list);
                    for (NBTBase subTag : tag.getTagList("list" + handler.getComponent().getName(), MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                        NBTTagList listTag = ((NBTTagList) subTag);
                        List<IValue> l = Lists.newArrayList();
                        list.add(l);
                        for (int i = 0; i < listTag.tagCount(); i++) {
                            l.add(handler.getValueType().deserialize(listTag.getStringTagAt(i)));
                        }
                    }
                }
            }

            return ValueIngredients.of(new IngredientsRecipeLists(lists));
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
