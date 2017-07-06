package org.cyclops.integrateddynamics.api.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;

import javax.annotation.Nullable;

/**
 * A predicate for variable facades of all types.
 * @author rubensworks
 */
public class VariableFacadePredicate<V extends IVariableFacade> {

    private static final IVariableFacadeHandlerRegistry VARIABLE_FACADE_HANDLER_REGISTRY = IntegratedDynamics._instance
            .getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);

    public static final VariableFacadePredicate ANY = new VariableFacadePredicate<>(IVariableFacade.class);

    private final Class<V> variableClass;

    public VariableFacadePredicate(Class<V> variableClass) {
        this.variableClass = variableClass;
    }

    public final boolean test(IVariableFacade variableFacade) {
        return variableClass.isInstance(variableFacade) && testTyped((V) variableFacade);
    }

    protected boolean testTyped(V variableFacade) {
        return true;
    }

    public static VariableFacadePredicate deserialize(@Nullable JsonElement element) {
        if (element != null && !element.isJsonNull()) {
            JsonObject jsonobject = JsonUtils.getJsonObject(element, "variable_facade");
            IVariableFacadeHandler handler;

            JsonElement typeElement = jsonobject.get("type");
            if (typeElement != null && !typeElement.isJsonNull()) {
                String type = JsonUtils.getString(jsonobject, "type");
                handler = VARIABLE_FACADE_HANDLER_REGISTRY.getHandler(type);
                if (handler == null) {
                    throw new JsonSyntaxException("Unknown variable type '" + type + "', valid types are: "
                            + VARIABLE_FACADE_HANDLER_REGISTRY.getHandlerNames());
                }
                return handler.deserializeVariableFacadePredicate(jsonobject);
            } else {
                return new VariableFacadePredicate<>(IVariableFacade.class);
            }
        }
        return ANY;
    }

}
