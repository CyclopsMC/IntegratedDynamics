package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;

import java.util.List;
import java.util.Map;

/**
 * Default ingredients serializer that uses the internal raw lists.
 * All other information such as predicates will be lost.
 * @author rubensworks
 */
public class IngredientsSerializerDefault implements IIngredientsSerializer<IIngredients> {

    private static final IngredientsSerializerDefault INSTANCE = new IngredientsSerializerDefault();

    private IngredientsSerializerDefault() {

    }

    public static IngredientsSerializerDefault getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean canHandle(IIngredients ingredients) {
        return true;
    }

    @Override
    public String getUniqueName() {
        return null; // Only the default serializer can have name null
    }

    @Override
    public NBTTagCompound serialize(IIngredients ingredients) {
        NBTTagCompound tag = new NBTTagCompound();
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
        return tag;
    }

    @Override
    public IngredientsRecipeLists deserialize(NBTTagCompound value) throws EvaluationException {
        Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists = Maps.newIdentityHashMap();
        for (RecipeComponent<?, ?> component : RecipeComponentHandlers.REGISTRY.getComponents()) {
            IRecipeComponentHandler handler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
            if (handler != null) {
                List<List<? extends IValue>> list = Lists.newArrayList();
                lists.put(component, list);
                for (NBTBase subTag : value.getTagList("list" + handler.getComponent().getName(), MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                    NBTTagList listTag = ((NBTTagList) subTag);
                    List<IValue> l = Lists.newArrayList();
                    list.add(l);
                    for (int i = 0; i < listTag.tagCount(); i++) {
                        l.add(handler.getValueType().deserialize(listTag.getStringTagAt(i)));
                    }
                }
            }
        }

        return new IngredientsRecipeLists(lists);
    }
}
