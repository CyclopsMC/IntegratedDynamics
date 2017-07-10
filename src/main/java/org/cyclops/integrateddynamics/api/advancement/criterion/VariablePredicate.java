package org.cyclops.integrateddynamics.api.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

import javax.annotation.Nullable;

/**
 * A predicate for variables of all types.
 * @author rubensworks
 */
public class VariablePredicate<V extends IVariable> {

    private static final IVariableFacadeHandlerRegistry VARIABLE_FACADE_HANDLER_REGISTRY = IntegratedDynamics._instance
            .getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);

    public static final VariablePredicate ANY = new VariablePredicate<>(IVariable.class, null, ValuePredicate.ANY);

    private final Class<V> variableClass;
    private final IValueType valueType;
    private final ValuePredicate valuePredicate;

    public VariablePredicate(Class<V> variableClass, @Nullable IValueType valueType, ValuePredicate valuePredicate) {
        this.variableClass = variableClass;
        this.valueType = valueType;
        this.valuePredicate = valuePredicate;
    }

    public final boolean test(IVariable variable) {
        try {
            return variableClass.isInstance(variable)
                    && (this.valueType == null || ValueHelpers.correspondsTo(this.valueType, variable.getType()))
                    && valuePredicate.test(variable.getValue())
                    && testTyped((V) variable);
        } catch (EvaluationException e) {
            return false;
        }
    }

    protected boolean testTyped(V variable) {
        return true;
    }

    public static VariablePredicate deserialize(@Nullable JsonElement element) {
        if (element != null && !element.isJsonNull()) {
            JsonObject jsonobject = JsonUtils.getJsonObject(element, "variable");
            IVariableFacadeHandler handler;

            IValueType valueType = JsonDeserializers.deserializeValueType(jsonobject);
            ValuePredicate valuePredicate = JsonDeserializers.deserializeValue(jsonobject, valueType);

            JsonElement typeElement = jsonobject.get("type");
            if (typeElement != null && !typeElement.isJsonNull()) {
                String type = JsonUtils.getString(jsonobject, "type");
                handler = VARIABLE_FACADE_HANDLER_REGISTRY.getHandler(type);
                if (handler == null) {
                    throw new JsonSyntaxException("Unknown variable type '" + type + "', valid types are: "
                            + VARIABLE_FACADE_HANDLER_REGISTRY.getHandlerNames());
                }
                return handler.deserializeVariablePredicate(jsonobject, valueType, valuePredicate);
            } else {
                return new VariablePredicate<>(IVariable.class, valueType, valuePredicate);
            }
        }
        return ANY;
    }

}
