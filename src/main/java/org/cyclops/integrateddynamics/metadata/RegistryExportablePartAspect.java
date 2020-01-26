package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Part aspect exporter.
 */
public class RegistryExportablePartAspect implements IRegistryExportable {


    @Override
    public JsonObject export() {
        JsonObject element = new JsonObject();

        for (IPartType partType : PartTypes.REGISTRY.getPartTypes()) {
            JsonArray array = new JsonArray();
            for (IAspect aspect : Aspects.REGISTRY.getAspects(partType)) {
                array.add(aspect.getUniqueName().toString());
            }
            element.add(partType.getTranslationKey(), array);
        }

        return element;
    }

    @Override
    public String getName() {
        return "part_aspect";
    }
}
