package org.cyclops.integrateddynamics.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.cyclops.cyclopscore.metadata.IRegistryExportable;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

/**
 * Operator exporter.
 */
public class RegistryExportableOperator implements IRegistryExportable {


    @Override
    public JsonObject export() {
        JsonObject element = new JsonObject();

        for (IOperator operator : Operators.REGISTRY.getOperators()) {
            JsonObject object = new JsonObject();

            object.addProperty("name", operator.getTranslationKey());
            object.addProperty("description", operator.getTranslationKey().substring(0, operator.getTranslationKey().length() - 5) + ".info");
            object.addProperty("symbol", operator.getSymbol());
            JsonArray inputs = new JsonArray();
            for (IValueType inputType : operator.getInputTypes()) {
                inputs.add(serializeValueType(inputType));
            }
            object.add("inputs", inputs);
            object.add("output", serializeValueType(operator.getOutputType()));

            element.add(operator.getTranslationKey(), object);
        }

        return element;
    }

    @Override
    public String getName() {
        return "operator";
    }

    protected static JsonObject serializeValueType(IValueType valueType) {
        JsonObject object = new JsonObject();
        object.addProperty("name", valueType.getTranslationKey());
        object.addProperty("color", valueType.getDisplayColorFormat());
        return object;
    }

}
