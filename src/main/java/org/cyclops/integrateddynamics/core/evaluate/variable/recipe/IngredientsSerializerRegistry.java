package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializerRegistry;

import java.util.List;
import java.util.Map;

/**
 * Registry for {@link IOperator}
 * @author rubensworks
 */
public class IngredientsSerializerRegistry implements IIngredientsSerializerRegistry {

    private static IngredientsSerializerRegistry INSTANCE = new IngredientsSerializerRegistry();

    private final List<IIngredientsSerializer<IIngredients>> serializers = Lists.newArrayList();
    private final Map<String, IIngredientsSerializer<IIngredients>> namedSerializers = Maps.newHashMap();
    private final IIngredientsSerializer<IIngredients> DEFAULT_SERIALIZER = IngredientsSerializerDefault.getInstance();

    private IngredientsSerializerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static IngredientsSerializerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerSerializer(IIngredientsSerializer<? extends IIngredients> serializer) {
        serializers.add((IIngredientsSerializer<IIngredients>) serializer);
        namedSerializers.put(serializer.getUniqueName(), (IIngredientsSerializer<IIngredients>) serializer);
    }

    @Override
    public NBTTagCompound serialize(IIngredients value) {
        NBTTagCompound tag = new NBTTagCompound();
        for (IIngredientsSerializer<IIngredients> serializer : serializers) {
            if (serializer.canHandle(value)) {
                tag.setString("ingredientsType", serializer.getUniqueName());
                tag.setTag("value", serializer.serialize(value));
                return tag;
            }
        }
        tag.setTag("value", DEFAULT_SERIALIZER.serialize(value));
        return tag;
    }

    @Override
    public IIngredients deserialize(NBTTagCompound value) throws EvaluationException {
        if (!value.hasKey("value", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            throw new EvaluationException(String.format("The given value is not a valid ingredients value '%s'", value));
        }
        if (value.hasKey("ingredientsType", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
            String serializerName = value.getString("ingredientsType");
            IIngredientsSerializer<IIngredients> serializer = namedSerializers.get(serializerName);
            if (serializer == null) {
                throw new EvaluationException(String.format("No serializer was found to deserialize the ingredients value '%s'", value));
            }
            return serializer.deserialize(value.getCompoundTag("value"));
        }
        return DEFAULT_SERIALIZER.deserialize(value.getCompoundTag("value"));
    }
}
