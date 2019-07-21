package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Collection;

/**
 * Aspect exporter.
 */
public class RegistryExportableAspect implements IRegistryExportable {


    @Override
    public JsonObject export() {
        JsonObject element = new JsonObject();

        for (IAspect aspect : Aspects.REGISTRY.getAspects()) {
            JsonObject object = new JsonObject();

            object.addProperty("name", aspect.getTranslationKey());
            object.addProperty("description", aspect.getTranslationKey().substring(0, aspect.getTranslationKey().length() - 5) + ".info");
            object.addProperty("type", aspect instanceof IAspectWrite ? "write" : "read");
            if (aspect instanceof IAspectWrite) {
                object.addProperty("inputValue", aspect.getValueType().getTranslationKey());
            } else {
                object.addProperty("outputValue", aspect.getValueType().getTranslationKey());
            }
            object.addProperty("valueColor", aspect.getValueType().getDisplayColorFormat());
            JsonArray properties = new JsonArray();
            for (IAspectPropertyTypeInstance propertyType : (Collection<IAspectPropertyTypeInstance>) aspect.getPropertyTypes()) {
                properties.add(propertyType.getTranslationKey());
            }
            object.add("properties", properties);

            element.add(aspect.getTranslationKey(), object);
        }

        return element;
    }

    @Override
    public String getName() {
        return "aspect";
    }
}
