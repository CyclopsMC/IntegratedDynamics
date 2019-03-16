package org.cyclops.integrateddynamics.api.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

/**
 * Json deserializers.
 * @author rubensworks
 */
public class JsonDeserializers {

    private JsonDeserializers() {

    }

    @Nullable
    public static IPartType deserializePartType(JsonObject jsonObject) {
        JsonElement partTypeElement = jsonObject.get("parttype");
        IPartType partType = null;
        if (partTypeElement != null && !partTypeElement.isJsonNull()) {
            partType = PartTypes.REGISTRY.getPartType(jsonObject.get("parttype").getAsString());
            if (partType == null) {
                throw new JsonSyntaxException("No part type found with name: " + jsonObject.get("parttype").getAsString());
            }
        }
        return partType;
    }

    @Nullable
    public static IAspect deserializeAspect(JsonObject jsonObject) {
        JsonElement aspectElement = jsonObject.get("aspect");
        IAspect aspect = null;
        if (aspectElement != null && !aspectElement.isJsonNull()) {
            aspect = Aspects.REGISTRY.getAspect(jsonObject.get("aspect").getAsString());
            if (aspect == null) {
                throw new JsonSyntaxException("No aspect found with name: " + jsonObject.get("aspect").getAsString());
            }
        }
        return aspect;
    }

    @Nullable
    public static IValueType deserializeValueType(JsonObject jsonobject) {
        JsonElement valueTypeElement = jsonobject.get("valuetype");
        IValueType valueType = null;
        if (valueTypeElement != null && !valueTypeElement.isJsonNull()) {
            valueType = ValueTypes.REGISTRY.getValueType(JsonUtils.getString(jsonobject, "valuetype"));
            if (valueType == null) {
                throw new JsonSyntaxException("Unknown value type '" + JsonUtils.getString(jsonobject, "valuetype") + "', valid types are: "
                        + ValueTypes.REGISTRY.getValueTypes().stream().map(IValueType::getTranslationKey).collect(Collectors.toList()));
            }
        }
        return valueType;
    }

    @Nullable
    public static ValuePredicate deserializeValue(JsonObject jsonObject, @Nullable IValueType valueType) {
        return ValuePredicate.deserialize(jsonObject, valueType);
    }
}
